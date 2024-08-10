package com.hci.loopsns.utils

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.TextViewCompat
import java.util.regex.Pattern
import kotlin.math.roundToInt


object EditTextAutoSizeUtil {

    /**
     * This function adds font autosizing to the provided `[editText]` by utilizing an invisible
     * autosizing `TextView`. During this process, the `EditText` is replaced within the layout
     * hierarchy with `FrameLayout`, which contains both the original `EditText` and the autosizing
     * `TextView`.
     *
     * @param`EditText`... the `EditText` you intend to make autosizing.
     * @param context ... your active context, used to create the `FrameLayout` and the `TextView`.
     * @return ... the newly created `Framelayout`, just in case you need it.
     */
    fun setupAutoResize(editText: HashTagEditText, context: Context): FrameLayout {
        // Step 1 — Create `FrameLayout` and put the `EditText` into it.
        val container = FrameLayout(context)
        val orgLayoutParams = editText.layoutParams

        (editText.parent as? ViewGroup)?.let { editParent ->
            editParent.indexOfChild(editText).let { index ->
                editParent.removeViewAt(index)
                container.addView(
                    editText,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                editParent.addView(container, index, orgLayoutParams)
            }
        }

        // Step 2 — Create the invisible autosizing `TextView` and add it to the `FrameLayout`.
        val textView = createAutoSizeHelperTextView(editText, context)
        container.addView(textView, 0, editText.layoutParams)

        editText.filters = arrayOf<InputFilter>(
            InputFilter { source, start, end, dst, dstart, dend ->
                //val ps = Pattern.compile("^[a-zA-Z0-9ㄱ-ㅎ가-흐]+$") //영문 숫자 한글
                //영문 숫자 한글 천지인 middle dot[ᆞ]
                val ps =
                    Pattern.compile("^[a-zA-Z0-9ㄱ-ㅎ가-흐ㄱ-ㅣ가-힣#ᆢᆞ\\u318d\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\s?!]+$")
                if (!ps.matcher(source).matches()) {
                    return@InputFilter ""
                } else {
                    return@InputFilter null
                }
            },
            InputFilter.LengthFilter(21)
        )

        // Step 3 — Install a listener to keep `TextView` and `EditText` in sync.
        editText.addTextChangedListener(object : TextWatcher {
            val originalTextSize = editText.textSize
            var isUpdating = false
            // Apply the changed text to the `TextView` and its new calculated `textSize` to the `EditText`.
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textView.setText(s?.toString(), TextView.BufferType.EDITABLE)
                // `textView` lays itself out again, so delay the query of the new `textSize` by using `post{ ... }`.

                editText.post {
                    val optimalSize =
                        if (s.isNullOrBlank())
                            originalTextSize
                        else {
                            val autosize = textView.textSize
                            autosize
                        }
                    editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, optimalSize)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isUpdating && s != null) {
                    isUpdating = true

//                    if (!s.startsWith("#")) {
//                        editText.text = s.insert(0, "#")
//                        editText.setSelection(editText.text.length)
//                    }

                    isUpdating = false
                }
            }
        })

        return container
    }

    /**
     * Creates the invisible `TextView` we use for the `textSize` calculation. It uses the same
     * padding as the `EditText`, since we need both with matching sizes to yield the best possible
     * `textSize` results.
     */
    private fun createAutoSizeHelperTextView(editText: EditText, context: Context): TextView =
        TextView(context).apply {
            maxLines = 1
            visibility = View.INVISIBLE
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                spToPx(context, AUTOSIZE_EDITTEXT_MINTEXTSIZE_SP),
                // It's a good idea to set the helper's max `textSize` to the initial text size of the `EditText` to avoid excessively inflating the font size.
                editText.textSize.roundToInt(),
                spToPx(context, AUTOSIZE_EDITTEXT_STEPSIZE_GRANULARITY_SP),
                TypedValue.COMPLEX_UNIT_PX
            )
            // Ensure `autosizeHelper` has the same layout parameters as the `EditText`.
            setPadding(
                editText.paddingLeft,
                editText.paddingTop,
                editText.paddingRight,
                editText.paddingBottom
            )
        }

    private const val AUTOSIZE_EDITTEXT_MINTEXTSIZE_SP = 6f
    private const val AUTOSIZE_EDITTEXT_STEPSIZE_GRANULARITY_SP = 1f

    fun spToPx(context: Context, sp: Float) =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics
        ).toInt()
}

class HashTagEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val inputConnection = super.onCreateInputConnection(outAttrs)

        // 새로운 InputConnection을 생성하여 반환
        return CustomInputConnection(this, inputConnection, true)
    }

    class CustomInputConnection(
        private val targetView: View,
        inputConnection: InputConnection?,
        mutable: Boolean
    ) : InputConnectionWrapper(inputConnection, mutable) {

        override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
            // 여기서 텍스트 조합 상태를 관리할 수 있습니다.
            val editText= (targetView as EditText).text
            if (text != null) {
                if(editText.isNullOrBlank()) {
                    val newText = "#$text"
                    super.setComposingText(newText, newCursorPosition + 1)
                    setComposingRegion(1, 2)
                    return true
                }
                super.setComposingText(text, newCursorPosition)
            } else {
                super.setComposingText(null, newCursorPosition)
            }
            return true
        }
    }
}

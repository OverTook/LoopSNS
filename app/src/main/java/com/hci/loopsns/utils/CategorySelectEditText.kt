package com.hci.loopsns.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View

class CategorySelectEditText : androidx.appcompat.widget.AppCompatEditText {
    /** Standard Constructors  */
    constructor(context: Context) : super(context)

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet, defStyle: Int
    ) : super(context, attrs, defStyle)

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        //setSelection(this.length())
    }

    fun isTextFullyFilled(): Boolean {
        val availableWidth = width - paddingLeft - paddingRight

        val text = text.toString()
        val textPaint = paint
        val textWidth = textPaint.measureText(text)

        return textWidth >= availableWidth
    }

    fun addEventListener() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (!s.toString().startsWith("#")) {
                    removeTextChangedListener(this)
                    text = s.insert(0, "#")
                    //setSelection(s.length)
                    addTextChangedListener(this)
                }
                resizeText()
            }
        })
    }

    private val minTextSize = 12f // 최소 텍스트 크기 (sp)
    private val maxTextSize = 100f // 최대 텍스트 크기 (sp)
    private val stepGranularity = 2f // 텍스트 크기 조정 단계 (sp)

    private fun resizeText() {
        val textLength = text?.length ?: 0
        val newSize = maxTextSize - stepGranularity * textLength

        // 텍스트 크기를 최소/최대 범위 내에서 설정
        val textSizeToSet = maxOf(minTextSize, minOf(maxTextSize, newSize))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeToSet)
    }
}
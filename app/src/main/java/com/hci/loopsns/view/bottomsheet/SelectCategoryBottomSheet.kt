package com.hci.loopsns.view.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R
import com.hci.loopsns.utils.CategorySelectEditText
import com.txusballesteros.AutoscaleEditText
import java.util.regex.Pattern
import kotlin.reflect.KFunction2

class SelectCategoryBottomSheet : BottomSheetDialogFragment() {

    private lateinit var categories: List<String>
    private lateinit var keywords: List<String>
    private lateinit var onSubmitAction: KFunction2<List<String>, List<String>, Unit>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_select_category, container, false)

        viewOfLayout.findViewById<TextView>(R.id.tag_1_article).text = categories[0]
        viewOfLayout.findViewById<TextView>(R.id.tag_2_article).text = categories[1]

        val keywordsList = listOf(
            viewOfLayout.findViewById<EditText>(R.id.keyword_1_article),
            viewOfLayout.findViewById<EditText>(R.id.keyword_2_article),
            viewOfLayout.findViewById<EditText>(R.id.keyword_3_article),
            viewOfLayout.findViewById<EditText>(R.id.keyword_4_article)
        )
        for(i in keywords.indices) {
            keywordsList[i].setText(buildString {
                append("#")
                append(keywords[i])
            })
            //keywordsList[i].addEventListener()
            keywordsList[i].filters = arrayOf(
                InputFilter { src, start, end, dst, dstart, dend ->
                    //val ps = Pattern.compile("^[a-zA-Z0-9ㄱ-ㅎ가-흐]+$") //영문 숫자 한글
                    //영문 숫자 한글 천지인 middle dot[ᆞ]
                    val ps =
                        Pattern.compile("^[a-zA-Z0-9ㄱ-ㅎ가-흐ㄱ-ㅣ가-힣#ᆢᆞ\\u318d\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\s?!]+$")
                    if (!ps.matcher(src).matches()) {
                        return@InputFilter ""
                    } else {
                        return@InputFilter null
                    }
                }
            )
        }

        viewOfLayout.findViewById<Button>(R.id.cancel).setOnClickListener {
            dismiss()
        }

        viewOfLayout.findViewById<Button>(R.id.submit).setOnClickListener {
            if(keywordsList.all { it.text.isNullOrBlank() }) {
                Toast.makeText(requireContext(), "키워드는 비워둘 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(keywordsList.any { (it.text?.length ?: 0) > 8 }) {
                Toast.makeText(requireContext(), "키워드의 최대 길이는 8글자 입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            this.onSubmitAction(
                categories,
                listOf(
                    keywordsList[0].text.toString(),
                    keywordsList[1].text.toString(),
                    keywordsList[2].text.toString(),
                    keywordsList[3].text.toString()
                )
            )

            dismiss()
        }
        return viewOfLayout
    }

    fun setData(categories: List<String>, keywords: List<String>, onSubmitAction: KFunction2<List<String>, List<String>, Unit>): SelectCategoryBottomSheet {
        this.categories = categories
        this.keywords = keywords
        this.onSubmitAction = onSubmitAction
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), theme)
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet =
                (dialog as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.from(bottomSheet).skipCollapsed = true
            BottomSheetBehavior.from(bottomSheet).isHideable = true
        }
        return bottomSheetDialog
    }
}
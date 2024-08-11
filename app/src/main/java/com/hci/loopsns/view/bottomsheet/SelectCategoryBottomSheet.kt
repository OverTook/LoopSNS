package com.hci.loopsns.view.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R
import com.hci.loopsns.utils.EditTextAutoSizeUtil
import com.hci.loopsns.utils.HashTagEditText
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
            viewOfLayout.findViewById<HashTagEditText>(R.id.keyword_1_article),
            viewOfLayout.findViewById<HashTagEditText>(R.id.keyword_2_article),
            viewOfLayout.findViewById<HashTagEditText>(R.id.keyword_3_article),
            viewOfLayout.findViewById<HashTagEditText>(R.id.keyword_4_article)
        )
        for(i in keywords.indices) {
            EditTextAutoSizeUtil.setupAutoResize(keywordsList[i], requireContext())

            if(keywords[i].isNotBlank()) {
                keywordsList[i].setText(buildString {
                    append("#")
                    append(keywords[i])
                })
            }

            //keywordsList[i].addEventListener()
        }

        viewOfLayout.findViewById<Button>(R.id.cancel).setOnClickListener {
            dismiss()
        }

        viewOfLayout.findViewById<Button>(R.id.submit).setOnClickListener {
            if(keywordsList.all { it.text.isNullOrBlank() }) {
                Toast.makeText(requireContext(), getString(R.string.keyword_cannot_be_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(keywordsList.any { (it.text?.length ?: 0) > 21 }) {
                Toast.makeText(requireContext(), getString(R.string.maximum_length_keyword_20), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            this.onSubmitAction(
                categories,
                listOf(
                    if ((keywordsList[0].text?.length ?: "".length) > 0) {
                        keywordsList[0].text.toString().substring(1)
                    } else {
                        ""
                    },
                    if ((keywordsList[1].text?.length ?: "".length) > 0) {
                        keywordsList[1].text.toString().substring(1)
                    } else {
                        ""
                    },
                    if ((keywordsList[2].text?.length ?: "".length) > 0) {
                        keywordsList[2].text.toString().substring(1)
                    } else {
                        ""
                    },
                    if ((keywordsList[3].text?.length ?: "".length) > 0) {
                        keywordsList[3].text.toString().substring(1)
                    } else {
                        ""
                    }
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
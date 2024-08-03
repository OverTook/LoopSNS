package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R
import kotlin.reflect.KFunction2

class SelectCategoryBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewOfLayout: View

    private lateinit var categories: List<String>
    private lateinit var keywords: List<String>
    private lateinit var onSubmitAction: KFunction2<List<String>, List<String>, Unit>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewOfLayout = inflater.inflate(R.layout.bottom_sheet_select_category, container, false)

        viewOfLayout.findViewById<TextView>(R.id.tag_1_article).text = categories[0]
        viewOfLayout.findViewById<TextView>(R.id.tag_2_article).text = categories[1]

        val keywordsList = listOf(
            viewOfLayout.findViewById<TextView>(R.id.keyword_1_article),
            viewOfLayout.findViewById<TextView>(R.id.keyword_2_article),
            viewOfLayout.findViewById<TextView>(R.id.keyword_3_article),
            viewOfLayout.findViewById<TextView>(R.id.keyword_4_article)
        )
        for(i in keywords.indices) {
            keywordsList[i].text = keywords[i]
        }

        viewOfLayout.findViewById<Button>(R.id.cancel).setOnClickListener {
            dismiss()
        }

        viewOfLayout.findViewById<Button>(R.id.submit).setOnClickListener {
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
}
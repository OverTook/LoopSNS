package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.ArticleSearchActivity
import com.hci.loopsns.R
import com.hci.loopsns.recyclers.search.category.CategoryAdapter

class SearchSelectCategoryBottomSheet(private val activity: ArticleSearchActivity, private val dataSet: List<String>, private val onClick: (String) -> Unit) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_search_select_category, container, false)

        val recyclerView: RecyclerView = viewOfLayout.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = CategoryAdapter(dataSet, onClick) {
            dismiss()
        }

        return viewOfLayout
    }
}
package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R

class ArticleOptionBottomSheet : BottomSheetDialogFragment() {

    private var canDelete = false
    private var deleteAction: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_article_option, container, false)

        viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).setOnClickListener {
            deleteAction?.invoke()
        }

        if(!canDelete) {
            viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).visibility = View.GONE
        }


        return viewOfLayout
    }

    public fun setData(canDelete: Boolean, deleteAction: () -> Unit): ArticleOptionBottomSheet {
        this.canDelete = canDelete
        this.deleteAction = deleteAction
        return this
    }
}
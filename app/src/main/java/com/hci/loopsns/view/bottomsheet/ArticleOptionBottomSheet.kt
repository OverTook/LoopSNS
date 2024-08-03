package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R
import kotlin.reflect.KFunction0

class ArticleOptionBottomSheet : BottomSheetDialogFragment() {

    private var canDelete = false
    private lateinit var deleteAction: KFunction0<Unit>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_article_option, container, false)

        viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).setOnClickListener {
            deleteAction.invoke()
        }

        if(!canDelete) {
            viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).visibility = View.GONE
        }


        return viewOfLayout
    }

    public fun setData(canDelete: Boolean, deleteAction: KFunction0<Unit>): ArticleOptionBottomSheet {
        this.canDelete = canDelete
        this.deleteAction = deleteAction
        return this
    }
}
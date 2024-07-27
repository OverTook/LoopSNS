package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R

class CommentOptionBottomSheet : BottomSheetDialogFragment() {

    private var canDelete = false
    private var deleteAction: ((String) -> Unit)? = null
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_article_option, container, false)

        viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).setOnClickListener {
            deleteAction?.invoke(uid)
        }

        if(!canDelete) {
            viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).visibility = View.GONE
        }


        return viewOfLayout
    }

    public fun setData(canDelete: Boolean, uid: String, deleteAction: (String) -> Unit): CommentOptionBottomSheet {
        this.canDelete = canDelete
        this.deleteAction = deleteAction
        this.uid = uid
        return this
    }
}
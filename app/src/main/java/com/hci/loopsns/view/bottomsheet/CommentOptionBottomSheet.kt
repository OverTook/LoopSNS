package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R
import kotlin.reflect.KFunction0

class CommentOptionBottomSheet : BottomSheetDialogFragment() {

    private var canDelete = false
    private lateinit var deleteAction: () -> Unit
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_article_option, container, false)

        viewOfLayout.findViewById<TextView>(R.id.deleteText).text = "댓글 삭제하기"
        viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).setOnClickListener {
            deleteAction.invoke()
            dismiss()
        }

        if(!canDelete) {
            viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).visibility = View.GONE
        }


        return viewOfLayout
    }

    public fun setData(canDelete: Boolean, uid: String, deleteAction: () -> Unit): CommentOptionBottomSheet {
        this.canDelete = canDelete
        this.deleteAction = deleteAction
        this.uid = uid
        return this
    }
}
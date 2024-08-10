package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R
import kotlin.reflect.KFunction0

class CommentOptionBottomSheet : BottomSheetDialogFragment() {

    private var canDelete = false
    private lateinit var deleteAction: () -> Unit
    private lateinit var shareAction: () -> Unit
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_article_option, container, false)

        viewOfLayout.findViewById<TextView>(R.id.deleteText).text = "댓글 삭제하기"
        viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.comment_delete_head)
                message(R.string.comment_delete_body)
                positiveButton(R.string.comment_delete_yes) { _ ->
                    this@CommentOptionBottomSheet.dismiss()
                    deleteAction.invoke()
                }
                negativeButton(R.string.comment_delete_no) { dialog ->
                    dialog.dismiss()
                }
            }
        }
        viewOfLayout.findViewById<ConstraintLayout>(R.id.shareItem).setOnClickListener {
            dismiss()
            shareAction.invoke()
        }

        if(!canDelete) {
            viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).visibility = View.GONE
        }


        return viewOfLayout
    }

    public fun setData(canDelete: Boolean, uid: String, deleteAction: () -> Unit, shareAction: () -> Unit): CommentOptionBottomSheet {
        this.canDelete = canDelete
        this.deleteAction = deleteAction
        this.shareAction = shareAction
        this.uid = uid
        return this
    }
}
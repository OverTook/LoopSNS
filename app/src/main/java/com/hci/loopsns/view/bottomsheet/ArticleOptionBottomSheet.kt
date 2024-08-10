package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R

class ArticleOptionBottomSheet : BottomSheetDialogFragment() {

    private var canDelete = false
    private lateinit var deleteAction: () -> Unit
    private lateinit var shareAction: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_article_option, container, false)

        viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.article_delete_head)
                message(R.string.article_delete_body)
                positiveButton(R.string.article_delete_yes) { _ ->
                    this@ArticleOptionBottomSheet.dismiss()
                    deleteAction.invoke()
                }
                negativeButton(R.string.article_delete_no) { dialog ->
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

    public fun setData(canDelete: Boolean, deleteAction: () -> Unit, shareAction: () -> Unit): ArticleOptionBottomSheet {
        this.canDelete = canDelete
        this.deleteAction = deleteAction
        this.shareAction = shareAction
        return this
    }
}
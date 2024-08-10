package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R
import com.hci.loopsns.network.NetworkInterface
import com.hci.loopsns.network.NetworkManager

class ArticleOptionBottomSheet : BottomSheetDialogFragment() {

    private var canDelete = false
    private lateinit var deleteAction: () -> Unit
    private lateinit var shareAction: () -> Unit
    private lateinit var reportAction: () -> Unit

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
        viewOfLayout.findViewById<ConstraintLayout>(R.id.reportArticle).setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.article_report_head)
                message(R.string.article_report_body)
                positiveButton(R.string.article_report_yes) { _ ->
                    this@ArticleOptionBottomSheet.dismiss()
                    reportAction()
                }
                negativeButton(R.string.article_report_no) { dialog ->
                    dialog.dismiss()
                }
            }
        }

        if(!canDelete) {
            viewOfLayout.findViewById<ConstraintLayout>(R.id.deleteItem).visibility = View.GONE
        }


        return viewOfLayout
    }

    public fun setData(canDelete: Boolean, deleteAction: () -> Unit, shareAction: () -> Unit, reportAction: () -> Unit): ArticleOptionBottomSheet {
        this.canDelete = canDelete
        this.deleteAction = deleteAction
        this.shareAction = shareAction
        this.reportAction = reportAction
        return this
    }
}
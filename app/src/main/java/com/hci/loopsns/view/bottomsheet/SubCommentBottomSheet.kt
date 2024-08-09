package com.hci.loopsns.view.bottomsheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.event.AutoRefresherInterface
import com.hci.loopsns.R
import com.hci.loopsns.event.CommentManager
import com.hci.loopsns.network.Comment
import com.hci.loopsns.network.CommentCreateResponse
import com.hci.loopsns.network.CommentListResponse
import com.hci.loopsns.network.CreateSubCommentRequest
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.comments.SubCommentsRecyclerViewAdapter
import com.hci.loopsns.utils.registerAutoRefresh
import com.hci.loopsns.utils.requestEnd
import com.skydoves.androidveil.VeilRecyclerFrameView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubCommentBottomSheet(private val activity: ArticleDetailActivity) : BottomSheetDialogFragment(), TextView.OnEditorActionListener,
    AutoRefresherInterface {

    private lateinit var adapter: SubCommentsRecyclerViewAdapter
    private lateinit var recyclerView: VeilRecyclerFrameView

    override var requested: Boolean = false
    override var noMoreData: Boolean = false
    override lateinit var requestAnimationView: View

    private var articleUid: String = ""
    private var parentComment: Comment? = null
    private var comments: ArrayList<Comment>? = null
    private var highlightComment: Comment? = null

    private var showKeyboard = false
    override fun getRequestAnimation(): View = requestAnimationView

    override fun requestMoreData() {
        if(comments.isNullOrEmpty()) {
            this.requestEnd(true)
            return
        }

        NetworkManager.apiService.retrieveSubCommentList(articleUid, parentComment!!.uid, comments!![comments!!.size - 1].uid).enqueue(object : Callback<CommentListResponse> {
            override fun onResponse(
                call: Call<CommentListResponse>,
                response: Response<CommentListResponse>
            ) {
                if (!response.isSuccessful) return

                val result = response.body()!!

                if(result.comments.isEmpty()) {
                    this@SubCommentBottomSheet.requestEnd(true)
                    return
                }

                if(highlightComment != null) {
                    if(result.comments.size == 1 && result.comments[0].uid == highlightComment!!.uid) {
                        this@SubCommentBottomSheet.requestEnd(true)
                    } else {
                        this@SubCommentBottomSheet.requestEnd(false)
                    }
                } else {
                    this@SubCommentBottomSheet.requestEnd(false)
                }

                adapter.addComments(result.comments as ArrayList<Comment>)
            }

            override fun onFailure(call: Call<CommentListResponse>, err: Throwable) {
                this@SubCommentBottomSheet.requestEnd(true)

                Snackbar.make(requireView().findViewById(R.id.main), "댓글을 불러오는 중 오류가 발생했습니다.", Snackbar.LENGTH_SHORT).show()
                Log.e("Request More Comment Failed", err.toString())
            }
        })
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_sub_comment, container, false)

        val input = viewOfLayout.findViewById<EditText>(R.id.comment_input)
        input.setOnEditorActionListener(this)

        if(showKeyboard) {
            input.requestFocus()
        }

        requestAnimationView = viewOfLayout.findViewById(R.id.requestProgressBar)
        adapter = SubCommentsRecyclerViewAdapter(activity)

        recyclerView = viewOfLayout.findViewById(R.id.article_recycler_view)
        recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        recyclerView.addVeiledItems(1) // 스켈레톤 추가
        recyclerView.veil()

        return viewOfLayout
    }

    fun initRecyclerView() {
        recyclerView.setAdapter(adapter)
        recyclerView.getRecyclerView().registerAutoRefresh(this)
    }

    fun requestSubComments() {
        NetworkManager.apiService.retrieveSubCommentList(articleUid, parentComment!!.uid).enqueue(object : Callback<CommentListResponse> {
            override fun onResponse(
                call: Call<CommentListResponse>,
                response: Response<CommentListResponse>
            ) {
                if(!response.isSuccessful) {
                    Log.e("SubCommentList Get Failed", "HTTP Code " + response.code())
                    Toast.makeText(requireContext(), "댓글 정보 응답이 성공적이지 않습니다.", Toast.LENGTH_SHORT).show()
                    dismiss()
                    return
                }

                val result = response.body()!!
                comments = result.comments as ArrayList<Comment>
                comments!!.forEach { comment ->
                    if(comment.writer == null) {
                        comment.writer = "알 수 없는 사용자"
                        comment.userImg = ""
                    }
                }

                initDataEnd()
            }

            override fun onFailure(call: Call<CommentListResponse>, err: Throwable) {
                Log.e("SubCommentList Get Failed", err.toString())
                Toast.makeText(requireContext(), "댓글 정보 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        })
    }

    fun initDataEnd() {
        if(comments == null || parentComment == null) {
            return
        }

        recyclerView.unVeil() //로딩이 끝났으므로 애니메이션 종료

        if (comments!!.isEmpty()) {
            adapter.resetData(parentComment!!, ArrayList())
            initRecyclerView()
            return
        }

        adapter.setHighlight(highlightComment)
        adapter.resetData(parentComment!!, comments!!)
        initRecyclerView()
    }

    fun setData(articleUid: String, parentComment: Comment): SubCommentBottomSheet {
        this.articleUid = articleUid
        this.parentComment = parentComment

        initDataEnd()

        return this
    }

    fun showKeyboard(showKeyboard: Boolean): SubCommentBottomSheet {
        this.showKeyboard = showKeyboard
        return this
    }

    fun setHighlight(comment: Comment?): SubCommentBottomSheet {
        this.highlightComment = comment
        return this
    }

    override fun onEditorAction(inputView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if(comments == null || parentComment == null) {
            Log.e("NULL", (comments == null).toString())
            Log.e("NULL", (parentComment == null).toString())
            return false
        }

        if (actionId == EditorInfo.IME_ACTION_SEND ||
            (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

            inputView?.clearFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(inputView?.windowToken, 0)

            createComment(inputView?.text.toString())
            (inputView!! as EditText).text.clear()
            return true
        }
        return false
    }

    private fun createComment(comment: String) {
        if(comments == null || parentComment == null) {
            return
        }

        NetworkManager.apiService.createSubComment(
            CreateSubCommentRequest(
                articleUid,
                parentComment!!.uid,
                comment
            )
        ).enqueue(object : Callback<CommentCreateResponse> {
            override fun onResponse(call: Call<CommentCreateResponse>, response: Response<CommentCreateResponse>) {
                if (!response.isSuccessful) return

                CommentManager.getInstance().onSubCommentCreated(
                    parentComment!!.uid,
                    Comment(
                        response.body()!!.uid,
                        FirebaseAuth.getInstance().currentUser!!.displayName,
                        comment,
                        response.body()!!.time,
                        FirebaseAuth.getInstance().currentUser!!.photoUrl!!.toString(),
                        true,
                        false,
                        0
                    )
                )

                //smoothScrollToPosition(recyclerView.getRecyclerView(), 1)
                Snackbar.make(dialog?.window?.decorView!!, "댓글이 작성되었습니다.", Snackbar.LENGTH_SHORT)
                    .setAnchorView(view?.findViewById(R.id.comment_input_layout))
                    .show()
            }

            override fun onFailure(call: Call<CommentCreateResponse>, err: Throwable) {
                Log.e("CreateSubComment Failed", err.toString())
            }

        })
    }

    override fun dismiss() {
        super.dismiss()

        adapter.onDestroy()
    }
}
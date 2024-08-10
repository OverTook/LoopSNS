package com.hci.loopsns

import android.R.attr.label
import android.R.attr.text
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hci.loopsns.event.ArticleManager
import com.hci.loopsns.event.AutoRefresherInterface
import com.hci.loopsns.event.CommentManager
import com.hci.loopsns.event.FavoriteManager
import com.hci.loopsns.network.ArticleDeleteResponse
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.ArticleDetailResponse
import com.hci.loopsns.network.Comment
import com.hci.loopsns.network.CommentCreateResponse
import com.hci.loopsns.network.CommentDeleteResponse
import com.hci.loopsns.network.CommentListResponse
import com.hci.loopsns.network.CommentResponse
import com.hci.loopsns.network.CreateCommentRequest
import com.hci.loopsns.network.LikeArticleRequest
import com.hci.loopsns.network.LikeResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.ReportResponse
import com.hci.loopsns.recyclers.article.ArticleRecyclerViewAdapter
import com.hci.loopsns.storage.SettingManager
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.utils.createDeepLink
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.registerAutoRefresh
import com.hci.loopsns.utils.requestEnd
import com.hci.loopsns.utils.showDarkOverlay
import com.hci.loopsns.view.bottomsheet.SubCommentBottomSheet
import com.skydoves.androidveil.VeilRecyclerFrameView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ArticleDetailActivity() : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener,
    View.OnClickListener, AutoRefresherInterface {

    private lateinit var adapter: ArticleRecyclerViewAdapter
    private lateinit var recyclerView: VeilRecyclerFrameView
    private lateinit var toolbar: AppBarLayout

    private val commentManager = CommentManager.getInstance()

    override var requested: Boolean = false
    override var noMoreData: Boolean = false
    override lateinit var requestAnimationView: View

    var article: ArticleDetail? = null
    var comments: ArrayList<Comment>? = null
    var highlightComment: Pair<Boolean, Comment?> = Pair(false, null)
    var highlightSubComment: Pair<Boolean, Comment?> = Pair(false, null)

    val bottomSheet = SubCommentBottomSheet(this)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_article_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestAnimationView = findViewById(R.id.requestProgressBar)
        toolbar = findViewById(R.id.appBarLayout)

        adapter = ArticleRecyclerViewAdapter(this)

        recyclerView = findViewById(R.id.article_recycler_view)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        recyclerView.addVeiledItems(1) // 스켈레톤 추가
        recyclerView.veil()

        findViewById<EditText>(R.id.comment_input).setOnEditorActionListener(this)
        findViewById<ImageButton>(R.id.backButton).setOnClickListener(this)
        findViewById<SwipeRefreshLayout>(R.id.swipeLayout).setOnRefreshListener(this)
        
        parseIntentData()
    }

    fun parseIntentData() {
        val articleId = intent.getStringExtra("articleId")

        if(articleId == null) {
            //Create 액티비티에서 넘어왔다면
            val article = IntentCompat.getParcelableExtra(intent, "article", ArticleDetail::class.java)
            if(article != null) {
                initFromCreateActivity(article)
                return
            }
            
            Toast.makeText(this, getString(R.string.post_information_invalid), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        requestArticle(articleId)
    }
    
    fun requestArticle(articleId: String) {
        NetworkManager.apiService.retrieveArticle(articleId).enqueue(object : Callback<ArticleDetailResponse> {
            override fun onResponse(
                call: Call<ArticleDetailResponse>,
                response: Response<ArticleDetailResponse>
            ) {
                if(!response.isSuccessful) {
                    if (response.code() == 404) {
                        Toast.makeText(this@ArticleDetailActivity, getString(R.string.deleted_post), Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                    Log.e("ArticleDetail Get Failed", "HTTP Code " + response.code())
                    Toast.makeText(this@ArticleDetailActivity, getString(R.string.post_information_response_not_successful), Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                val result = response.body()!!
                article = result.article
                if(article!!.writer == null) {
                    article!!.writer = getString(R.string.unknown_user_name)
                    article!!.userImg = ""
                }

                initNetworkTaskEnd()
            }

            override fun onFailure(call: Call<ArticleDetailResponse>, err: Throwable) {
                Log.e("ArticleDetail Get Failed", err.toString())
                Toast.makeText(this@ArticleDetailActivity, getString(R.string.fail_request_post_information), Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        NetworkManager.apiService.retrieveCommentList(articleId).enqueue(object : Callback<CommentListResponse> {
            override fun onResponse(
                call: Call<CommentListResponse>,
                response: Response<CommentListResponse>
            ) {
                if(!response.isSuccessful) {
                    if (response.code() == 404) {
                        return
                    }
                    Log.e("CommentList Get Failed", "HTTP Code " + response.code())
                    Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_comment_information_response), Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                val result = response.body()!!
                comments = result.comments as ArrayList<Comment>
                comments!!.forEach { comment ->
                    if(comment.writer == null) {
                        comment.writer = getString(R.string.unknown_user_name)
                        comment.userImg = ""
                    }
                }

                if(comments.isNullOrEmpty()) {
                    noMoreData = true
                }

                initNetworkTaskEnd()
            }

            override fun onFailure(call: Call<CommentListResponse>, err: Throwable) {
                Log.e("CommentList Get Failed", err.toString())
                Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_bring_comments), Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        val highlightComment = IntentCompat.getParcelableExtra(intent, "highlight", NotificationComment::class.java) ?: return
        Log.e("highlight", highlightComment.toString())

        this.highlightComment = Pair(true, null)

        if(highlightComment.subCommentId.isNotBlank()) {
            this.highlightSubComment = Pair(true, null)

            bottomSheet.showKeyboard(false).show(supportFragmentManager, "SubCommentBottomSheet")

            //대댓글 하이라이트로 이동
            NetworkManager.apiService.retrieveComment(articleId, highlightComment.commentId).enqueue(object : Callback<CommentResponse> {
                override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                    if(!response.isSuccessful) {
                        if (response.code() == 404) {
                            return
                        }
                        Log.e("Retrieve Comment Get Failed", "HTTP Code " + response.code())
                        Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_comment_information_response), Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }

                    this@ArticleDetailActivity.highlightComment = Pair(true, response.body()!!.comment)

                    NetworkManager.apiService.retrieveSubComment(articleId, highlightComment.commentId, highlightComment.subCommentId).enqueue(object : Callback<CommentResponse> {
                        override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                            if(!response.isSuccessful) {
                                Log.e("Retrieve Comment Get Failed", "HTTP Code " + response.code())
                                Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_comment_information_response), Toast.LENGTH_SHORT).show()
                                finish()
                                return
                            }

                            this@ArticleDetailActivity.highlightSubComment = Pair(true, response.body()!!.comment)
                            initNetworkTaskEnd()
                            //openSubComment(this@ArticleDetailActivity.highlightComment.second!!, false)
                        }

                        override fun onFailure(call: Call<CommentResponse>, err: Throwable) {
                            Log.e("Retrieve Comment Get Failed", err.toString())
                            Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_bring_comments), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    })

                }

                override fun onFailure(call: Call<CommentResponse>, err: Throwable) {
                    Log.e("Retrieve Comment Get Failed", err.toString())
                    Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_bring_comments), Toast.LENGTH_SHORT).show()
                    finish()
                }
            })

            return
        }

        highlightSubComment = Pair(false, null)

        NetworkManager.apiService.retrieveComment(articleId, highlightComment.commentId).enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if(!response.isSuccessful) {
                    Log.e("Retrieve Comment Get Failed", "HTTP Code " + response.code())
                    Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_comment_information_response), Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                this@ArticleDetailActivity.highlightComment = Pair(true, response.body()!!.comment)
                initNetworkTaskEnd()
            }

            override fun onFailure(call: Call<CommentResponse>, err: Throwable) {
                Log.e("Retrieve Comment Get Failed", err.toString())
                Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_bring_comments), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    fun initFromCreateActivity(article: ArticleDetail) {
        recyclerView.unVeil()

        this.article = article
        this.comments = ArrayList()

        adapter.resetData(article, comments!!)

        initRecyclerView()
    }

    fun initRecyclerView() {
        recyclerView.setAdapter(adapter)
        recyclerView.getRecyclerView().registerAutoRefresh(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingManager.getInstance().getCurrentLocaleContext(base))
    }

    override fun getRequestAnimation(): View = requestAnimationView

    override fun requestMoreData() {
        if(comments.isNullOrEmpty()) {
            this@ArticleDetailActivity.requestEnd(true)
            return
        }

        NetworkManager.apiService.retrieveCommentList(article!!.uid, comments!![0].uid).enqueue(object : Callback<CommentListResponse> {
            override fun onResponse(
                call: Call<CommentListResponse>,
                response: Response<CommentListResponse>
            ) {
                if (!response.isSuccessful) return

                val result = response.body()!!

                if(result.comments.isEmpty()) {
                    this@ArticleDetailActivity.requestEnd(true)
                    return
                }

                if(highlightComment.first && highlightComment.second != null) {
                    if(result.comments.size == 1 && result.comments[0].uid == highlightComment.second!!.uid) {
                        this@ArticleDetailActivity.requestEnd(true)
                    } else {
                        this@ArticleDetailActivity.requestEnd(false)
                    }
                } else {
                    this@ArticleDetailActivity.requestEnd(false)
                }

                adapter.addComments(result.comments as ArrayList<Comment>)
            }

            override fun onFailure(call: Call<CommentListResponse>, err: Throwable) {
                this@ArticleDetailActivity.requestEnd(true)

                Snackbar.make(findViewById(R.id.main), getString(R.string.error_bring_comments), Snackbar.LENGTH_SHORT).show()
                Log.e("Request More Comment Failed", err.toString())
            }
        })
    }

    fun initNetworkTaskEnd() {
        if(article == null|| comments == null || (highlightComment.first && highlightComment.second == null) || (highlightSubComment.first && highlightSubComment.second == null)) {
            return
        }

        if(highlightSubComment.first) {
            bottomSheet.setHighlight(highlightSubComment.second).setData(article!!.uid, highlightComment.second!!).requestSubComments()
        }

        recyclerView.unVeil() //로딩이 끝났으므로 애니메이션 종료

        if (comments!!.isEmpty()) {
            adapter.resetData(article!!, ArrayList())
            initRecyclerView()
            return
        }

        if(!highlightComment.first) {
            adapter.resetData(article!!, comments!!)
            initRecyclerView()
            return
        }

        adapter.setHighlight(highlightComment.second!!)
        adapter.resetData(article!!, comments!!)
        initRecyclerView()
    }

    fun refreshNetworkTaskEnd() {
        if(article == null || comments == null || (highlightComment.first && highlightComment.second == null)) {
            return
        }

        adapter.resetData(article!!, comments!!)
        hideDarkOverlay()
        findViewById<SwipeRefreshLayout>(R.id.swipeLayout).isRefreshing = false
    }

    fun openSubComment(comment: Comment, openKeyboard: Boolean) {
        if(supportFragmentManager.findFragmentByTag("SubCommentBottomSheet") != null) return

        if(article == null) {
            bottomSheet
                .setData(intent.getStringExtra("articleId")!!, comment)
                .setHighlight(highlightSubComment.second)
                .showKeyboard(openKeyboard)
                .show(supportFragmentManager, "SubCommentBottomSheet")

            bottomSheet.requestSubComments()
            return
        }

        bottomSheet
            .setData(article!!.uid, comment)
            .setHighlight(highlightSubComment.second)
            .showKeyboard(openKeyboard)
            .show(supportFragmentManager, "SubCommentBottomSheet")

        bottomSheet.requestSubComments()
    }

    fun deleteArticle() {
        if(article == null || comments == null || (highlightComment.first && highlightComment.second == null)) {
            return
        }

        NetworkManager.apiService.deleteArticle(article!!.uid).enqueue(object : Callback<ArticleDeleteResponse> {
            override fun onResponse(call: Call<ArticleDeleteResponse>, response: Response<ArticleDeleteResponse>) {
                if(!response.isSuccessful) return

                //MyArticleFactory.addDeletedArticles(article!!.uid)
                //LikeArticleFactory.addDeletedArticles(article!!.uid)

                ArticleManager.getInstance().onArticleDeleted(article!!.uid)

                finish()
            }

            override fun onFailure(call: Call<ArticleDeleteResponse>, err: Throwable) {
                Log.e("ArticleDelete Failed", err.toString())
            }
        })
    }

    fun deleteComment(uid: String) {
        if(article == null || comments == null || (highlightComment.first && highlightComment.second == null)) {
            return
        }

        NetworkManager.apiService.deleteComment(article!!.uid, uid).enqueue(object : Callback<CommentDeleteResponse> {
            override fun onResponse(call: Call<CommentDeleteResponse>, response: Response<CommentDeleteResponse>) {
                if(!response.isSuccessful) return

                //adapter.deleteComment(uid)
                commentManager.onCommentDeleted(uid)
            }

            override fun onFailure(call: Call<CommentDeleteResponse>, err: Throwable) {
                Log.e("CommentDelete Failed", err.toString())
            }
        })
    }

    override fun onPause() {
        super.onPause()

        if(article == null || comments == null || (highlightComment.first && highlightComment.second == null)) {
            return
        }

        //좋아요 변동 있는 상태에서 액티비티 멈춤
        if(article!!.isLiked == null) {
            article!!.isLiked = false
        }

        if(article!!.isLiked!!.xor(adapter.originalLike)) {
            addArticleLike(article!!.isLiked!!) //액티비티가 닫힐 때 요청 보내기
        }
    }

    private fun addArticleLike(like: Boolean, callback: (() -> Unit)? = null) {
        if(article == null || comments == null || (highlightComment.first && highlightComment.second == null)) {
            return
        }

        FavoriteManager.getInstance().onFavoriteStateChanged(article!!, article!!.isLiked!!)

        NetworkManager.apiService.likeArticle(
            LikeArticleRequest(
                article!!.uid,
                like
            )
        ).enqueue(object : Callback<LikeResponse> {
            override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                callback?.invoke()
                if (response.isSuccessful) return

                if(article!!.isLiked == true) {
                    article!!.isLiked = false
                    article!!.likeCount--
                } else {
                    article!!.isLiked = true
                    article!!.likeCount++
                }
                Log.e("ArticleLike Failed", "HTTP Code " + response.code())
            }

            override fun onFailure(call: Call<LikeResponse>, err: Throwable) {
                callback?.invoke()
                Log.e("ArticleLike Failed", err.toString())
                if(article!!.isLiked == true) {
                    article!!.isLiked = false
                    article!!.likeCount--
                } else {
                    article!!.isLiked = true
                    article!!.likeCount++
                }
            }
        })
    }

    override fun onRefresh() {
        if(article == null || comments == null || (highlightComment.first && highlightComment.second == null)) {
            findViewById<SwipeRefreshLayout>(R.id.swipeLayout).isRefreshing = false
            return
        }

        showDarkOverlay()

        if(article!!.isLiked!!.xor(adapter.originalLike)) {
            addArticleLike(article!!.isLiked!!) {
                refresh()
            }
            return
        }

        refresh()
    }

    fun refresh() {
        val articleId = article!!.uid
        article = null
        comments = null

        NetworkManager.apiService.retrieveArticle(articleId).enqueue(object :
            Callback<ArticleDetailResponse> {
            override fun onResponse(call: Call<ArticleDetailResponse>, response: Response<ArticleDetailResponse>) {
                if(!response.isSuccessful){
                    Toast.makeText(this@ArticleDetailActivity, getString(R.string.post_information_response_not_successful), Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                article = response.body()!!.article
                if(article!!.writer == null) {
                    article!!.writer = getString(R.string.unknown_user_name)
                    article!!.userImg = ""
                }

                noMoreData = false
                refreshNetworkTaskEnd()
            }

            override fun onFailure(call: Call<ArticleDetailResponse>, err: Throwable) {
                Log.e("ArticleDetailActivity", "게시글 불러오기 실패 $err")
                Toast.makeText(this@ArticleDetailActivity, getString(R.string.fail_request_post_information), Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        NetworkManager.apiService.retrieveCommentList(articleId).enqueue(object :
            Callback<CommentListResponse> {
            override fun onResponse(call: Call<CommentListResponse>, response: Response<CommentListResponse>) {
                if(!response.isSuccessful){
                    Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_comment_information_response), Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                comments = response.body()!!.comments as ArrayList<Comment>
                comments!!.forEach { comment ->
                    if(comment.writer == null) {
                        comment.writer = getString(R.string.unknown_user_name)
                        comment.userImg = ""
                    }
                }

                refreshNetworkTaskEnd()
            }

            override fun onFailure(call: Call<CommentListResponse>, err: Throwable) {
                Log.e("ArticleDetailActivity", "댓글 불러오기 실패 $err")
                Toast.makeText(this@ArticleDetailActivity, getString(R.string.error_bring_comments), Toast.LENGTH_SHORT).show()
                finish()
            }
        })

    }

    private fun createComment(comment: String) {
        if(article == null || comments == null || (highlightComment.first && highlightComment.second == null)) {
            return
        }

        NetworkManager.apiService.createComment(
            CreateCommentRequest(
                article!!.uid,
                comment
            )
        ).enqueue(object : Callback<CommentCreateResponse> {
            override fun onResponse(call: Call<CommentCreateResponse>, response: Response<CommentCreateResponse>) {
                if (!response.isSuccessful) return

                commentManager.onCommentCreated(
                    Comment(
                        article!!.uid,
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

                Snackbar.make(findViewById(R.id.main), getString(R.string.comment_make_success), Snackbar.LENGTH_SHORT)
                    .setAnchorView(findViewById(R.id.comment_input_layout))
                    .show()
            }

            override fun onFailure(call: Call<CommentCreateResponse>, err: Throwable) {
                Log.e("CreateComment Failed", err.toString())
            }

        })
    }

    override fun onEditorAction(inputView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if(article == null|| comments == null || (highlightComment.first && highlightComment.second == null)) {
            return false
        }

        if (actionId == EditorInfo.IME_ACTION_SEND ||
            (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
            hideKeyboard()
            inputView?.clearFocus()
            createComment(inputView?.text.toString())
            (inputView!! as EditText).text.clear()
            return true
        }
        return false
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.backButton -> {
                finish()
            }
        }
    }

    fun report(commentId: String = "", subCommentId: String = "") {
        NetworkManager.apiService.report(article!!.uid, commentId, subCommentId).enqueue(object : Callback<ReportResponse> {
            override fun onResponse(p0: Call<ReportResponse>, p1: Response<ReportResponse>) {
                Toast.makeText(this@ArticleDetailActivity, getString(R.string.reported_successful), Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Call<ReportResponse>, p1: Throwable) {

            }

        })
    }

    fun createDeepLinkShare(commentId: String, subCommentId: String = "") {
        val deepLink = this.createDeepLink(article!!.uid, commentId, subCommentId)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_via_text) + deepLink)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_select_text)))

        Log.e("Deep Link", deepLink)
    }


    override fun onDestroy() {
        super.onDestroy()
        adapter.onDestroy()
    }

}
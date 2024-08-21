package com.hci.loopsns

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.utils.MDUtil.getStringArray
import com.hci.loopsns.event.CommentListener
import com.hci.loopsns.event.CommentManager
import com.hci.loopsns.event.FavoriteListener
import com.hci.loopsns.event.FavoriteManager
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.Comment
import com.hci.loopsns.network.IntentionSubjectResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.SearchResponse
import com.hci.loopsns.recyclers.search.SearchMode
import com.hci.loopsns.recyclers.search.SearchRecyclerViewAdapter
import com.hci.loopsns.storage.SettingManager
import com.skydoves.androidveil.VeilRecyclerFrameView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import kotlin.system.exitProcess


class ArticleSearchActivity : AppCompatActivity(), View.OnClickListener,
    TextView.OnEditorActionListener, View.OnFocusChangeListener, CommentListener, FavoriteListener {

    private lateinit var searchEditText: EditText

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchRecyclerViewAdapter

    private var categoryA = ""
    private var categoryB = ""
    private var currentType = "content"
    private var lastCall: Call<SearchResponse>? = null

    lateinit var categoryAs: List<String>
    lateinit var categoryBs: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animationInit()
        setContentView(R.layout.activity_search_result)

        adapter = SearchRecyclerViewAdapter(this)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        //recyclerView.addVeiledItems(1)
        //recyclerView.unVeil()

        searchEditText = findViewById(R.id.filter)
        searchEditText.setOnEditorActionListener(this)
        searchEditText.onFocusChangeListener = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            searchEditText.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        } else {
            searchEditText.requestFocus()
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        NetworkManager.apiService.getIntentionsSubjects(Locale.getDefault().language).enqueue(object : Callback<IntentionSubjectResponse> {
            override fun onResponse(
                call: Call<IntentionSubjectResponse>,
                response: Response<IntentionSubjectResponse>
            ) {
                if(!response.isSuccessful) {
                    finish()
                    return
                }

                categoryAs = response.body()!!.intentions
                categoryBs = response.body()!!.subjects
            }

            override fun onFailure(p0: Call<IntentionSubjectResponse>, p1: Throwable) {
                finish()
            }
        })

        CommentManager.getInstance().registerCommentListener(this)
        FavoriteManager.getInstance().registerFavoriteListener(this)
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingManager.getInstance().getCurrentLocaleContext(base))
    }

    fun animationInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    override fun finish() {
        super.finish()
        onBackPressedCallback.remove()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.backButton -> {
                finish()
            }
        }
    }

    fun onClickArticle(uid: String) {
        val intent = Intent(
            this,
            ArticleDetailActivity::class.java
        )
        intent.putExtra("articleId", uid)
        startActivity(intent)
    }

    override fun onEditorAction(inputView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if(inputView == null) return true

        if (actionId == EditorInfo.IME_ACTION_SEND ||
            (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
            retrieveArticles(inputView.text.toString())
            return true
        }
        return false
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(adapter.currentMode and SearchMode.History == SearchMode.History) {
                adapter.reloadData()
                searchEditText.clearFocus()
                return
            }
            finish()
        }
    }

    override fun onFocusChange(view: View, focus: Boolean) {
        if (view.id != R.id.filter || !focus) return

        adapter.loadHistory()
    }

    fun retrieveArticles(input: String, force: Boolean = false) {
        if(input.length < 2 && !force) {
            Toast.makeText(this, getString(R.string.searche_at_least_two_letters), Toast.LENGTH_SHORT).show()
            return
        }

        searchEditText.setText(input)
        searchEditText.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)

        adapter.search(input)

        lastCall?.cancel()
        lastCall = NetworkManager.apiService.search(categoryA, categoryB, input, currentType)
        lastCall?.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if(call.isCanceled) return

                if(!response.isSuccessful) {
                    adapter.loadHistory()
                    return
                }

                val result = response.body()!!.articles

                adapter.loadData(result)
            }

            override fun onFailure(call: Call<SearchResponse>, err: Throwable) {
                //adapter.loadHistory()
            }

        })
    }

    fun changeType(type: Int) {
        currentType = when(type) {
            0 -> "content"
            1 -> "keyword"
            2 -> "writer"
            else -> "content"
        }

        adapter.searchWithoutHistoryAdd()
        retrieveArticles(searchEditText.text.toString())
    }

    fun changeCategoryA(category: String) {
        if(category == categoryAs[0]) {
            this.categoryA = ""
        } else {
            this.categoryA = category
        }

        adapter.searchWithoutHistoryAdd()
        //retrieveArticles(searchEditText.text.toString())
        retrieveArticles("", true)
    }

    fun changeCategoryB(category: String) {
        if(category == categoryBs[0]) {
            this.categoryB = ""
        } else {
            this.categoryB = category
        }

        adapter.searchWithoutHistoryAdd()
        //retrieveArticles(searchEditText.text.toString())
        retrieveArticles("", true)
    }

    override fun onDestroy() {
        super.onDestroy()

        CommentManager.getInstance().removeCommentListener(this)
        FavoriteManager.getInstance().removeFavoriteListener(this)
    }

    override fun onCommentDeleted(uid: String) {
        adapter.updateCommentcount(uid, -1)
    }

    override fun onCommentCreated(comment: Comment) {
        adapter.updateCommentcount(comment.articleId!!, 1)
    }

    override fun onFavoriteArticle(article: ArticleDetail) {
        adapter.updateLikeCount(article.uid, 1)
    }

    override fun onUnfavoriteArticle(articleId: String) {
        adapter.updateLikeCount(articleId, -1)
    }
}
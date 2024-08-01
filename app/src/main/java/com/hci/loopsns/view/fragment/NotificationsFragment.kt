package com.hci.loopsns.view.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.R
import com.hci.loopsns.network.ArticleDetailResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.notification.NotificationRecyclerViewAdapter
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.storage.models.NotificationHotArticle
import com.hci.loopsns.storage.models.NotificationInterface
import com.hci.loopsns.utils.factory.NotificationFactory
import com.hci.loopsns.utils.factory.NotificationFactoryEventListener
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import kotlinx.coroutines.launch
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, NotificationFactoryEventListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationRecyclerViewAdapter
    private lateinit var badge: BadgeDrawable
    private var badgeCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        badge = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).getOrCreateBadge(R.id.menu_notification)
        view.findViewById<SwipeRefreshLayout>(R.id.swipeLayout).setOnRefreshListener(this)

        //Do In Async
        lifecycleScope.launch {
            val comments = LitePal.order("time desc").limit(20).find(NotificationComment::class.java)
            val hotArticles = LitePal.order("time desc").limit(20).find(NotificationHotArticle::class.java)

            val commentCount = LitePal.where("readed = ?", "0").count(NotificationComment::class.java)
            val hotArticleCount = LitePal.where("readed = ?", "0").count(NotificationHotArticle::class.java)

            badgeCount = commentCount + hotArticleCount
            if(badgeCount > 0) {
                badge.isVisible = true
                badge.number = badgeCount
                badge.badgeTextColor = Color.WHITE
                badge.backgroundColor = Color.RED
            } else {
                badge.isVisible = false
            }

            val totalList = ArrayList<NotificationInterface>()
            totalList.addAll(comments)
            totalList.addAll(hotArticles)
            totalList.sortByDescending { it.time }
            totalList.take(20)

            recyclerView = view.findViewById(R.id.notifications_recycler)
            adapter = NotificationRecyclerViewAdapter(::onClickArticle, totalList)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            NotificationFactory.addEventListener(this@NotificationsFragment)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            val comments = LitePal.order("time desc").limit(20).find(NotificationComment::class.java)
            val hotArticles = LitePal.order("time desc").limit(20).find(NotificationHotArticle::class.java)

            val commentCount = LitePal.where("readed = ?", "0").count(NotificationComment::class.java)
            val hotArticleCount = LitePal.where("readed = ?", "0").count(NotificationHotArticle::class.java)

            badgeCount = commentCount + hotArticleCount
            if(badgeCount > 0) {
                badge.isVisible = true
                badge.number = badgeCount
            } else {
                badge.isVisible = false
            }

            val totalList = ArrayList<NotificationInterface>()
            totalList.addAll(comments)
            totalList.addAll(hotArticles)
            totalList.sortByDescending { it.time }
            totalList.take(20)

            adapter.resetData(totalList)
            view?.findViewById<SwipeRefreshLayout>(R.id.swipeLayout)?.isRefreshing = false
        }
    }

    override fun onCreatedNotification(item: NotificationInterface) {
        lifecycleScope.launch {
            adapter.addNotification(item)
            badge.number = ++badgeCount

            //맨 위로 드래그 되어 있는가?
            if((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() <= 1) {
                recyclerView.smoothScrollToPosition(0)
            }
        }
    }

    private fun onClickArticle(articleId: String, commentId: String) {
        requireActivity().showDarkOverlay()

        NetworkManager.apiService.retrieveArticleDetail(articleId).enqueue(object :
            Callback<ArticleDetailResponse> {
            override fun onResponse(call: Call<ArticleDetailResponse>, response: Response<ArticleDetailResponse>) {
                requireActivity().hideDarkOverlay()
                if(!response.isSuccessful){
                    Snackbar.make(view!!.findViewById(R.id.longFragment), "게시글 정보를 불러올 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                    return
                }

                val articleDetail = response.body()!!.article
                val comments = response.body()!!.comments

                if(articleDetail.writer == null) {
                    articleDetail.writer = "알 수 없는 사용자"
                    articleDetail.userImg = ""
                }
                comments.forEach { comment ->
                    if(comment.writer == null) {
                        comment.writer = "알 수 없는 사용자"
                        comment.userImg = ""
                    }
                }

                val intent = Intent(
                    requireActivity(),
                    ArticleDetailActivity::class.java
                )

                intent.putExtra("move", commentId)
                intent.putExtra("article", articleDetail)
                intent.putParcelableArrayListExtra("comments", ArrayList(comments))
                startActivity(intent)
            }

            override fun onFailure(call: Call<ArticleDetailResponse>, err: Throwable) {
                requireActivity().hideDarkOverlay()
                Log.e("NotificationsFragment", "게시글 불러오기 실패$err")
            }
        })
    }
}
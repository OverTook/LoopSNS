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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.event.AutoRefresherInterface
import com.hci.loopsns.R
import com.hci.loopsns.recyclers.notification.NotificationRecyclerViewAdapter
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.storage.models.NotificationFavorite
import com.hci.loopsns.storage.models.NotificationInterface
import com.hci.loopsns.utils.factory.NotificationFactory
import com.hci.loopsns.utils.factory.NotificationFactoryEventListener
import com.hci.loopsns.utils.registerAutoRefresh
import com.hci.loopsns.utils.requestEnd
import github.com.st235.lib_expandablebottombar.ExpandableBottomBar
import github.com.st235.lib_expandablebottombar.Notification
import kotlinx.coroutines.launch
import org.litepal.LitePal

class NotificationsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, NotificationFactoryEventListener,
    AutoRefresherInterface {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationRecyclerViewAdapter
    private lateinit var badge: Notification
    private var badgeCount = 0
    private var currentPage = 0

    override var requested: Boolean = false
    override var noMoreData: Boolean = false
    override lateinit var requestAnimationView: View

    var user: FirebaseUser? = null

    override fun getRequestAnimation(): View = requestAnimationView

    override fun requestMoreData() {
        if(user == null) return

        currentPage++

        val lastCommentID = adapter.getLastNotificationID(NotificationComment::class.java)
        val lastHotArticleID = adapter.getLastNotificationID(NotificationFavorite::class.java)

        Log.e("LastCommentID", lastCommentID.toString())

        val comments = LitePal.where("userId = ?", user!!.uid).order("time desc").offset(lastCommentID).limit(20).find(NotificationComment::class.java)
        val favoriteNotis = LitePal.where("userId = ?", user!!.uid).order("time desc").offset(lastHotArticleID).limit(20).find(NotificationFavorite::class.java)

        val totalList = ArrayList<NotificationInterface>()
        totalList.addAll(comments)
        totalList.addAll(favoriteNotis)
        totalList.sortByDescending { it.time }
        totalList.take(20)

        adapter.addNotifications(totalList)

        this.requestEnd(totalList.size < 20)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        user = FirebaseAuth.getInstance().currentUser
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(user == null) return

        badge = requireActivity().findViewById<ExpandableBottomBar>(R.id.bottomNavigationView).menu.findItemById(R.id.menu_notification).notification()
        view.findViewById<SwipeRefreshLayout>(R.id.swipeLayout).setOnRefreshListener(this)
        requestAnimationView = view.findViewById(R.id.requestProgressBar)

        //Do In Async
        lifecycleScope.launch {
            val comments = LitePal.where("userId = ?", user!!.uid).order("time desc").limit(20).find(NotificationComment::class.java)
            val favoriteNotis = LitePal.where("userId = ?", user!!.uid).order("time desc").limit(20).find(NotificationFavorite::class.java)

            Log.e("uid", user!!.uid)
            val commentCount = LitePal.where("readed = ? AND userId = ?", "0", user!!.uid).count(NotificationComment::class.java)
            val favoriteNotisCount = LitePal.where("readed = ? AND userId = ?", "0", user!!.uid).count(NotificationFavorite::class.java)

            badgeCount = commentCount + favoriteNotisCount
            if(badgeCount > 0) {
                badge.show(badgeCount.toString())
                badge.badgeTextColor = Color.WHITE
            } else {
                badge.clear()
            }

            val totalList = ArrayList<NotificationInterface>()
            totalList.addAll(comments)
            totalList.addAll(favoriteNotis)
            totalList.sortByDescending { it.time }
            totalList.take(20)

            recyclerView = view.findViewById(R.id.notifications_recycler)
            adapter = NotificationRecyclerViewAdapter(requireContext(), ::onClickNotification, totalList)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.registerAutoRefresh(this@NotificationsFragment)

            noMoreData = totalList.size < 20

            NotificationFactory.addEventListener(this@NotificationsFragment)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onRefresh() {
        if(user == null) return

        lifecycleScope.launch {
            val comments = LitePal.where("userId = ?", user!!.uid).order("time desc").limit(20).find(NotificationComment::class.java)
            val favoriteNotis = LitePal.where("userId = ?", user!!.uid).order("time desc").limit(20).find(NotificationFavorite::class.java)

            val commentCount = LitePal.where("readed = ? AND userId = ?", "0", user!!.uid).count(NotificationComment::class.java)
            val favoriteNotisCount = LitePal.where("readed = ? AND userId = ?", "0", user!!.uid).count(NotificationFavorite::class.java)

            badgeCount = commentCount + favoriteNotisCount
            if(badgeCount > 0) {
                badge.show(badgeCount.toString())
            } else {
                badge.clear()
            }

            val totalList = ArrayList<NotificationInterface>()
            totalList.addAll(comments)
            totalList.addAll(favoriteNotis)
            totalList.sortByDescending { it.time }
            totalList.take(20)

            noMoreData = totalList.size < 20

            adapter.resetData(totalList)
            view?.findViewById<SwipeRefreshLayout>(R.id.swipeLayout)?.isRefreshing = false
        }
    }

    override fun onCreatedNotification(item: NotificationInterface) {
        lifecycleScope.launch {
            adapter.addNotification(item)
            badge.show((++badgeCount).toString())
            //맨 위로 드래그 되어 있는가?
            if((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() <= 1) {
                recyclerView.smoothScrollToPosition(0)
            }
        }
    }

    private fun onClickNotification(notification: NotificationInterface) {
        val intent = Intent(
            requireActivity(),
            ArticleDetailActivity::class.java
        )

        if(!notification.readed) {
            if((--badgeCount) == 0) {
                badge.clear()
            } else {
                badge.show(badgeCount.toString())
            }
        }

        when (notification) {
            is NotificationComment -> {
                if(!notification.readed) {
                    notification.readed = true
                    notification.save()
                }

                intent.putExtra("highlight", notification)
                intent.putExtra("articleId", notification.articleId)
                startActivity(intent)
            }
            is NotificationFavorite -> {
                if(!notification.readed) {
                    notification.readed = true
                    notification.save()
                }

                intent.putExtra("articleId", notification.articleId)
                startActivity(intent)
            }
        }

    }
}
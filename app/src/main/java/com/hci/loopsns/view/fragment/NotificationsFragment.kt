package com.hci.loopsns.view.fragment

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
import com.hci.loopsns.R
import com.hci.loopsns.network.Comment
import com.hci.loopsns.recyclers.detail.ArticleRecyclerViewAdapter
import com.hci.loopsns.recyclers.notification.NotificationRecyclerViewAdapter
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.storage.models.NotificationHotArticle
import com.hci.loopsns.storage.models.NotificationInterface
import com.hci.loopsns.utils.factory.NotificationFactory
import com.hci.loopsns.utils.factory.NotificationFactoryEventListener
import kotlinx.coroutines.launch
import org.litepal.LitePal
import org.litepal.crud.LitePalSupport

class NotificationsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, NotificationFactoryEventListener {

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
        val comments = LitePal.order("time asc").limit(20).find(NotificationComment::class.java)
        val hotArticles = LitePal.order("time asc").limit(20).find(NotificationHotArticle::class.java)

        val commentCount = LitePal.where("readed = ?", "0").count(NotificationComment::class.java)
        val hotArticleCount = LitePal.where("readed = ?", "0").count(NotificationHotArticle::class.java)

        badge = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).getOrCreateBadge(R.id.menu_notification)

        badgeCount = commentCount + hotArticleCount
        Log.e("UnreadCount", badgeCount.toString())
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
        totalList.sortBy { it.time }
        totalList.take(20)

        val recyclerView = view.findViewById<RecyclerView>(R.id.notifications_recycler)

        adapter = NotificationRecyclerViewAdapter(totalList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        NotificationFactory.addEventListener(this)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onRefresh() {
        val comments = LitePal.order("time asc").limit(20).find(NotificationComment::class.java)
        val hotArticles = LitePal.order("time asc").limit(20).find(NotificationHotArticle::class.java)

        
    }

    override fun onCreatedNotification(item: NotificationInterface) {
        adapter.addNotification(item)

        badge.number = ++badgeCount
    }
}
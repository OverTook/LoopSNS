package com.hci.loopsns.recyclers.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.R
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.storage.models.NotificationHotArticle
import com.hci.loopsns.storage.models.NotificationInterface
import com.hci.loopsns.utils.formatTo

class NotificationRecyclerViewAdapter(private var items: ArrayList<NotificationInterface>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val body = itemView.findViewById<TextView>(R.id.body)
        val time = itemView.findViewById<TextView>(R.id.time)
    }

    class HotArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val body = itemView.findViewById<TextView>(R.id.body)
        val time = itemView.findViewById<TextView>(R.id.time)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when(item) {
            is NotificationComment -> {
                (holder as CommentViewHolder).body.text = buildString {
                    append(item.writer)
                    append(": ")
                    append(item.contents)
                }

                holder.time.text = item.time.formatTo("yyyy-MM-dd HH:mm")
            }
            is NotificationHotArticle -> {
                //TODO
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return when (viewType) {
            ViewType.HOT_ARTICLE -> {
                TODO()
                //view = inflater.inflate(R.layout.fra, parent, false)
                //NotificationRecyclerViewAdapter.HotArticleViewHolder(view)
            }
            ViewType.COMMENT -> {
                view = inflater.inflate(R.layout.fragment_notifications_comment_item, parent, false)
                CommentViewHolder(view)
            }
            else -> {
                //TODO 팔로우 게시글 등
                view = inflater.inflate(R.layout.fragment_notifications_comment_item, parent, false)
                CommentViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        when(items[position]) {
            is NotificationComment -> {
                return ViewType.COMMENT
            }
            is NotificationHotArticle -> {
                return ViewType.HOT_ARTICLE
            }
            //TODO
            else -> {
                return ViewType.HOT_ARTICLE
            }
        }
    }

    fun addNotification(item: NotificationInterface) {
        items.add(0, item)
        this.notifyItemInserted(0)
    }
}
package com.hci.loopsns.recyclers.notification

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.R
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.storage.models.NotificationFavorite
import com.hci.loopsns.storage.models.NotificationInterface
import com.hci.loopsns.utils.formatTo
import java.lang.reflect.Type
import kotlin.reflect.KFunction1

class NotificationRecyclerViewAdapter(private val mContext: Context, private val notificationClickAction: KFunction1<NotificationInterface, Unit>, private var items: ArrayList<NotificationInterface>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root = itemView.findViewById<ConstraintLayout>(R.id.main)
        val body = itemView.findViewById<TextView>(R.id.body)
        val time = itemView.findViewById<TextView>(R.id.time)
    }

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root = itemView.findViewById<ConstraintLayout>(R.id.main)
        val body = itemView.findViewById<TextView>(R.id.body)
        val time = itemView.findViewById<TextView>(R.id.time)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when(item) {
            is NotificationComment -> {
                (holder as CommentViewHolder).root.setOnClickListener {
                    holder.root.setBackgroundColor(ContextCompat.getColor(mContext, R.color.notification_read_item_background))
                    notificationClickAction(item)
                }

                if(item.readed) {
                    holder.root.setBackgroundColor(ContextCompat.getColor(mContext, R.color.notification_read_item_background))
                } else {
                    holder.root.setBackgroundColor(ContextCompat.getColor(mContext, R.color.notification_unread_item_background))
                }

                holder.body.text = buildString {
                    append(item.writer)
                    append(": ")
                    append(item.contents)
                }

                holder.time.text = item.time.formatTo("yyyy-MM-dd HH:mm")
            }
            is NotificationFavorite -> {
                (holder as FavoriteViewHolder).root.setOnClickListener {
                    holder.root.setBackgroundColor(ContextCompat.getColor(mContext, R.color.notification_read_item_background))
                    notificationClickAction(item)
                }

                if(item.readed) {
                    holder.root.setBackgroundColor(ContextCompat.getColor(mContext, R.color.notification_read_item_background))
                } else {
                    holder.root.setBackgroundColor(ContextCompat.getColor(mContext, R.color.notification_unread_item_background))
                }

                holder.body.text = buildString {
                    append(mContext.getString(R.string.notification_comment_prefix))
                    append(item.likeCount)
                    append(mContext.getString(R.string.notification_comment_suffix))
                }

                holder.time.text = item.time.formatTo("yyyy-MM-dd HH:mm")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return when (viewType) {
            ViewType.FAVORITE_ARTICLE -> {
                view = inflater.inflate(R.layout.fragment_notifications_favorite_item, parent, false)
                FavoriteViewHolder(view)
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
            is NotificationFavorite -> {
                return ViewType.FAVORITE_ARTICLE
            }
            //TODO
            else -> {
                return ViewType.FAVORITE_ARTICLE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetData(items: ArrayList<NotificationInterface>) {
        this.items = items
        this.notifyDataSetChanged()
    }

    fun addNotifications(items: List<NotificationInterface>) {
        this.items.addAll(items)
        this.notifyItemRangeInserted(this.items.size, items.size)
    }

    fun addNotification(item: NotificationInterface) {
        this.items.add(0, item)
        this.notifyItemInserted(0)
    }

    fun <T : NotificationInterface> getLastNotificationID(type: Class<T>): Int {
        var offset = 0
        for(i in items.size - 1 downTo 0) {
            if(items[i].javaClass == type) {
                offset++
            }
        }
        return offset
    }
}
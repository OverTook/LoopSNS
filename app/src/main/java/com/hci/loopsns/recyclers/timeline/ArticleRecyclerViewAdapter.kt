package com.hci.loopsns.recyclers.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.R
import com.hci.loopsns.network.Article
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.toDate

class ArticleRecyclerViewAdapter(private val articleClickAction: (Article) -> Unit, private var items: List<Article>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class HotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.hot_article)

        val contents: TextView = itemView.findViewById(R.id.hot_article_content)
        val tag: TextView = itemView.findViewById(R.id.hot_tag)
    }

    class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.hot_article_overview)

        val contents: TextView = itemView.findViewById(R.id.content_text_normal)
        val category1: TextView = itemView.findViewById(R.id.tag_1_normal)
        val category2: TextView = itemView.findViewById(R.id.tag_2_normal)
        val keywords1: TextView = itemView.findViewById(R.id.keyword_1_normal)
        val keywords2: TextView = itemView.findViewById(R.id.keyword_2_normal)
        val keywords3: TextView = itemView.findViewById(R.id.keyword_3_normal)
        val keywords4: TextView = itemView.findViewById(R.id.keyword_4_normal)
        val time: TextView = itemView.findViewById(R.id.article_time_normal)

        val likeCount: TextView = itemView.findViewById(R.id.like_count_normal)
        val commentCount: TextView = itemView.findViewById(R.id.comment_count_normal)
    }
    
    class AdvertisementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //TODO 광고 게시글 타임라인 기능
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return when (viewType) {
            ArticleType.HOT_ARTICLE -> {
                view = inflater.inflate(R.layout.activity_map_overview_timeline_article_hot_item, parent, false)
                HotViewHolder(view)
            }
            ArticleType.NORMAL_ARTICLE -> {
                view = inflater.inflate(R.layout.activity_map_overview_timeline_article_normal_item, parent, false)
                NormalViewHolder(view)
            }
            else -> {
                //TODO 광고 게시글 타임라인 기능
                view = inflater.inflate(R.layout.activity_map_overview_timeline_article_normal_item, parent, false)
                AdvertisementViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when(holder) {
            is HotViewHolder -> {
                holder.root.setOnClickListener{
                    articleClickAction(item)
                }

                holder.contents.text = item.contents
                holder.tag.text = item.cat1
            }
            is NormalViewHolder -> {
                holder.root.setOnClickListener{
                    articleClickAction(item)
                }

                holder.contents.text = item.contents
                holder.category1.text = item.cat1
                holder.category2.text = item.cat2

                val keywords = listOf(
                    holder.keywords1,
                    holder.keywords2,
                    holder.keywords3,
                    holder.keywords4
                )

                for (i in 0 until item.keywords.size) {
                    if (i < item.keywords.size) {
                        if(item.keywords[i].isNotBlank()) {
                            keywords[i].visibility = View.VISIBLE
                            keywords[i].text = item.keywords[i]
                        }
                    }
                }

                holder.time.text = item.time.toDate().formatTo("yyyy-MM-dd HH:mm")
                holder.likeCount.text = item.likeCount.toString()
                holder.commentCount.text = item.commentCount.toString()
            }
            is AdvertisementViewHolder -> {
                //TODO 광고 게시글 타임라인 기능
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        //TODO 광고 게시글 타임라인 기능
        if(position < 2) {
            return ArticleType.HOT_ARTICLE
        }
        return ArticleType.NORMAL_ARTICLE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearItems() {
        items = emptyList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<Article>) {
        this.items = items
        notifyDataSetChanged()
    }
}
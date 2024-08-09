package com.hci.loopsns.recyclers.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginRight
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayout
import com.hci.loopsns.R
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.utils.dp
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.toDate
import com.hci.loopsns.view.fragment.profile.BaseProfileFragment

class ProfileRecyclerViewAdapter(private val activity: BaseProfileFragment, private var items: ArrayList<ArticleDetail>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.main)

        val contents: TextView = itemView.findViewById(R.id.contents)
        val time: TextView = itemView.findViewById(R.id.time)
        val picture: ImageView = itemView.findViewById(R.id.picture)

        val tags: LinearLayout = itemView.findViewById(R.id.tags)
        val keywords: FlexboxLayout = itemView.findViewById(R.id.keywords)

        val category1: TextView = itemView.findViewById(R.id.tag_1_article)
        val category2: TextView = itemView.findViewById(R.id.tag_2_article)
        val keywords1: TextView = itemView.findViewById(R.id.keyword_1_article)
        val keywords2: TextView = itemView.findViewById(R.id.keyword_2_article)
        val keywords3: TextView = itemView.findViewById(R.id.keyword_3_article)
        val keywords4: TextView = itemView.findViewById(R.id.keyword_4_article)

        val likeCount: TextView = itemView.findViewById(R.id.favorite_count)
        val commentCount: TextView = itemView.findViewById(R.id.comment_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return when (viewType) {
            ViewType.ARTICLE -> {
                view = inflater.inflate(R.layout.child_fragment_profile_article_item, parent, false)
                ArticleViewHolder(view)
            }
            else -> { //Never Happend
                view = inflater.inflate(R.layout.child_fragment_profile_article_item, parent, false)
                ArticleViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        if(position < items.size) {
            return ViewType.ARTICLE
        }
        return ViewType.ETC
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ArticleViewHolder -> {
                val item = items[position]

                holder.root.setOnClickListener {
                    activity.onClickArticle(item.uid)
                }

                holder.contents.text = item.contents
                holder.time.text =  item.time.toDate().formatTo("yyyy-MM-dd HH:mm")

                holder.category1.text = item.cat1
                holder.category2.text= item.cat2

                holder.likeCount.text = item.likeCount.toString()
                holder.commentCount.text = item.commentCount.toString()

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
                            keywords[i].text = buildString {
                                append("#")
                                append(item.keywords[i])
                            }
                        }
                    }
                }

                if (item.images.isNotEmpty()) {
                    holder.picture.visibility = View.VISIBLE

                    var params = holder.tags.layoutParams as ConstraintLayout.LayoutParams
                    params.marginEnd = 120.dp
                    holder.tags.layoutParams = params

                    params = holder.keywords.layoutParams as ConstraintLayout.LayoutParams
                    params.marginEnd = 120.dp
                    holder.keywords.layoutParams = params

                    Glide.with(activity)
                        .load(item.images[0])
                        .thumbnail(
                            Glide.with(activity).load(R.drawable.picture_placeholder)
                        )
                        .transform(CenterCrop(), RoundedCorners(30))
                        //.apply(RequestOptions.bitmapTransform(RoundedCorners(150)))
                        .into(holder.picture)

                } else {
                    holder.picture.visibility = View.GONE

                    var params = holder.tags.layoutParams as ConstraintLayout.LayoutParams
                    params.marginEnd = 10.dp
                    holder.tags.layoutParams = params

                    params = holder.keywords.layoutParams as ConstraintLayout.LayoutParams
                    params.marginEnd = 10.dp
                    holder.keywords.layoutParams = params
                }
            }
        }
    }

    fun getLastArticle(): String {
        if(items.size == 0) {
            return ""
        }
        return items[items.size - 1].uid
    }

    fun deleteArticle(uid: String) {
        for (i in 0..<items.size) {
            val item = items[i]

            if(item.uid != uid) {
                continue
            }

            items.removeAt(i)
            this.notifyItemRemoved(i)
            return
        }
    }

    fun createArticle(article: ArticleDetail) {
        items.add(0, article)
        this.notifyItemInserted(0)
    }

    fun updateLikeCount(articleId: String, count: Int) {
        for(i in 0..<items.size){
            if(items[i].uid != articleId) continue

            items[i].likeCount += count
            this.notifyItemChanged(i)
            return
        }
    }

    fun updateCommentCount(articleId: String, count: Int) {
        for(i in 0..<items.size){
            if(items[i].uid != articleId) continue

            items[i].commentCount += count
            this.notifyItemChanged(i)
            return
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetArticles(articles: List<ArticleDetail>) {
        items = articles as ArrayList<ArticleDetail>
        this.notifyDataSetChanged()
    }

    fun insertArticles(articles: List<ArticleDetail>) {
        if(articles.isEmpty()) return

        items.addAll(articles)
        this.notifyItemRangeInserted(items.size - articles.size, articles.size)
    }
}
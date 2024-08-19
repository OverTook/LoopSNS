package com.hci.loopsns.recyclers.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexboxLayout
import com.hci.loopsns.R
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.utils.dp
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.toDate
import kotlin.reflect.KFunction1

class ArticleRecyclerViewAdapter(private val context: Context, private val articleClickAction: KFunction1<ArticleDetail, Unit>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isCollapsed = false
    private var articles: ArrayList<ArticleDetail> = ArrayList()
    private var hotArticles: List<ArticleDetail> = emptyList()
//
//    class HotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val root: ConstraintLayout = itemView.findViewById(R.id.hot_article)
//
//        val contents: TextView = itemView.findViewById(R.id.hot_article_content)
//        val tag: TextView = itemView.findViewById(R.id.hot_tag)
//    }

    class HotArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.hot_article)
        val contents: TextView = itemView.findViewById(R.id.hot_article_content)
        val tag: TextView = itemView.findViewById(R.id.hot_tag)
    }

    class NormalArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.main)

        val contents: TextView = itemView.findViewById(R.id.contents)
        val category1: TextView = itemView.findViewById(R.id.tag_1_article)
        val category2: TextView = itemView.findViewById(R.id.tag_2_article)
        val keywords1: TextView = itemView.findViewById(R.id.keyword_1_article)
        val keywords2: TextView = itemView.findViewById(R.id.keyword_2_article)
        val keywords3: TextView = itemView.findViewById(R.id.keyword_3_article)
        val keywords4: TextView = itemView.findViewById(R.id.keyword_4_article)
        val time: TextView = itemView.findViewById(R.id.time)
        val picture: ImageView = itemView.findViewById(R.id.picture)

        val tags: LinearLayout = itemView.findViewById(R.id.tags)
        val keywords: FlexboxLayout = itemView.findViewById(R.id.keywords)

        val likeCount: TextView = itemView.findViewById(R.id.favorite_count)
        val commentCount: TextView = itemView.findViewById(R.id.comment_count)
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
                view = inflater.inflate(R.layout.activity_main_timeline_hot_article_item, parent, false)
                HotArticleViewHolder(view)
            }
            ArticleType.NORMAL_ARTICLE -> {
                view = inflater.inflate(R.layout.activity_main_timeline_article_item, parent, false)
                NormalArticleViewHolder(view)
            }
            else -> {
                //TODO 광고 게시글 타임라인 기능
                view = inflater.inflate(R.layout.activity_main_timeline_article_item, parent, false)
                AdvertisementViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is HotArticleViewHolder -> {
                val item = hotArticles[position]

                holder.root.setOnClickListener{
                    articleClickAction(item)
                }

                holder.contents.text = item.contents
                holder.tag.text = item.intention
            }
            is NormalArticleViewHolder -> {
                val item = articles[position - hotArticles.size]

                holder.root.setOnClickListener{
                    articleClickAction(item)
                }

                holder.contents.text = item.contents
                holder.category1.text = item.intention
                holder.category2.text = item.subject

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

                holder.time.text = item.time.toDate().formatTo("yyyy-MM-dd HH:mm")
                holder.likeCount.text = item.likeCount.toString()
                holder.commentCount.text = item.commentCount.toString()

                if (item.images.isNotEmpty()) {
                    holder.picture.visibility = View.VISIBLE

                    var params = holder.tags.layoutParams as ConstraintLayout.LayoutParams
                    params.marginEnd = 120.dp
                    holder.tags.layoutParams = params

                    params = holder.keywords.layoutParams as ConstraintLayout.LayoutParams
                    params.marginEnd = 120.dp
                    holder.keywords.layoutParams = params

                    Glide.with(context)
                        .load(item.images[0])
                        .thumbnail(Glide.with(context).load(R.drawable.picture_placeholder))
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
            is AdvertisementViewHolder -> {
                //TODO 광고 게시글 타임라인 기능
            }
        }
    }

    override fun getItemCount(): Int = articles.size + hotArticles.size

    override fun getItemViewType(position: Int): Int {
        //TODO 광고 게시글 타임라인 기능
        if(position < hotArticles.size && !isCollapsed) {
            return ArticleType.HOT_ARTICLE
        }
        return ArticleType.NORMAL_ARTICLE
    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun clearItems() {
//        items = ArrayList()
//        notifyDataSetChanged()
//    }

    fun setItemInCollapse(item: ArticleDetail) {
        isCollapsed = true
        articles.add(item)
        notifyItemInserted(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(articles: List<ArticleDetail>, hotArticles: List<ArticleDetail>) {
        isCollapsed = false
        this.articles = articles as ArrayList
        this.hotArticles = hotArticles
        notifyDataSetChanged() //TODO
    }

    fun addItems(articles: List<ArticleDetail>) {
        this.articles.addAll(articles)
        notifyItemRangeInserted(this.articles.size + this.hotArticles.size, articles.size)
    }

    fun updateCommentcount(articleId: String, increment: Int) {
        if(isCollapsed) {
            if(articles[0].uid == articleId) {
                articles[0].commentCount += increment
                notifyItemChanged(0)
            }
            return
        }

        for(i in articles.indices) {
            if(articles[i].uid != articleId) {
                continue
            }

            articles[i].commentCount += increment
            notifyItemChanged(i + hotArticles.size)
            return
        }
    }

    fun updateLikeCount(articleId: String, increment: Int) {
        if(isCollapsed) {
            if(articles[0].uid == articleId) {
                articles[0].likeCount += increment
                notifyItemChanged(0)
            }
            return
        }

        for(i in articles.indices) {
            if(articles[i].uid != articleId) {
                continue
            }

            articles[i].likeCount += increment
            notifyItemChanged(i + hotArticles.size)
            return
        }
    }
}
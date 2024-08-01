package com.hci.loopsns.recyclers.detail

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.R
import com.hci.loopsns.view.bottomsheet.ArticleOptionBottomSheet
import com.hci.loopsns.view.bottomsheet.CommentOptionBottomSheet
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.Comment
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.toDate

class ArticleRecyclerViewAdapter(private val activity: ArticleDetailActivity, private var article: ArticleDetail, private var items: ArrayList<Comment>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var commentCountView: TextView? = null
    var commentHighlight = -1

    val originalLike: Boolean = article.isLiked

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val writer: TextView = itemView.findViewById(R.id.writer_name)
        val time: TextView = itemView.findViewById(R.id.write_time)

        val category1: TextView = itemView.findViewById(R.id.tag_1_article)
        val category2: TextView = itemView.findViewById(R.id.tag_2_article)
        val keywords1: TextView = itemView.findViewById(R.id.keyword_1_article)
        val keywords2: TextView = itemView.findViewById(R.id.keyword_2_article)
        val keywords3: TextView = itemView.findViewById(R.id.keyword_3_article)
        val keywords4: TextView = itemView.findViewById(R.id.keyword_4_article)

        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val articleImage: ImageView = itemView.findViewById(R.id.article_image)
        val articleContent: TextView = itemView.findViewById(R.id.article_text)

        val commentCount: TextView = itemView.findViewById(R.id.comment_text)
        val likeCount: TextView = itemView.findViewById(R.id.like_count_detail)
        val likeLayout: ConstraintLayout = itemView.findViewById(R.id.article_like)
        val likeIcon: ImageView = itemView.findViewById(R.id.like)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.main)

        val writer: TextView = itemView.findViewById(R.id.comment_writer_name)
        val profileImage: ImageView = itemView.findViewById(R.id.comment_profile_image)
        val time: TextView = itemView.findViewById(R.id.comment_time)
        val articleContent: TextView = itemView.findViewById(R.id.comment_body)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
    }
    
    class AdvertisementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //TODO 광고 게시글 타임라인 기능
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return when (viewType) {
            ViewType.ARTICLE -> {
                view = inflater.inflate(R.layout.activity_article_detail_article_item, parent, false)
                ArticleViewHolder(view)
            }
            ViewType.COMMENT -> {
                view = inflater.inflate(R.layout.activity_article_detail_comment_item, parent, false)
                CommentViewHolder(view)
            }
            else -> {
                //TODO 광고 게시글 댓글 기능?
                view = inflater.inflate(R.layout.activity_article_detail_article_item, parent, false)
                AdvertisementViewHolder(view)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        if(holder !is ArticleViewHolder) {
            return
        }

        commentCountView = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ArticleViewHolder -> {
                if(article.userImg.isNotBlank()) {
                    Glide.with(activity)
                        .load(article.userImg)
                        .into(holder.profileImage)
                }

                holder.writer.text = article.writer
                holder.time.text = article.time.toDate().formatTo("yyyy-MM-dd HH:mm")

                holder.category1.text = article.cat1
                holder.category2.text= article.cat2

                val keywords = listOf(
                    holder.keywords1,
                    holder.keywords2,
                    holder.keywords3,
                    holder.keywords4
                )

                for (i in 0 until article.keywords.size) {
                    if (i < article.keywords.size) {
                        if(article.keywords[i].isNotBlank()) {
                            keywords[i].visibility = View.VISIBLE
                            keywords[i].text = article.keywords[i]
                        }
                    }
                }

                if (article.images.isNotEmpty()) {
                    holder.articleImage.visibility = View.VISIBLE
                    Glide.with(activity)
                        .load(article.images[0])
                        .thumbnail(Glide.with(activity).load(R.drawable.picture_placeholder))
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(150)))
                        .into(holder.articleImage)
                } else {
                    holder.articleImage.visibility = View.GONE
                }

                holder.optionButton.setOnClickListener {
                    ArticleOptionBottomSheet().setData(
                        article.canDelete,
                        activity::deleteArticle
                    ).show(activity.supportFragmentManager, "ArticleOptionBottomSheet")
                }

                holder.articleContent.text = article.contents

                commentCountView = holder.commentCount
                commentCountView?.text = buildString {
                    append("댓글 ")
                    append(article.commentCount)
                }

                if(article.isLiked) {
                    holder.likeIcon.setImageResource(R.drawable.favorite_fill_48px)
                } else {
                    holder.likeIcon.setImageResource(R.drawable.favorite_48px)
                }

                holder.likeCount.text = article.likeCount.toString()
                holder.likeLayout.setOnClickListener {
                    when(article.isLiked) {
                        true -> {
                            article.isLiked = false
                            animateLike(holder.likeIcon, false)

                            holder.likeCount.text = (--article.likeCount).toString()
                        }
                        false -> {
                            article.isLiked = true
                            animateLike(holder.likeIcon, true)

                            holder.likeCount.text = (++article.likeCount).toString()
                        }
                    }
                }
            }
            is CommentViewHolder -> {
                val item = items[position - 1] //게시글 한 개 빼야함

                if(position - 1 == commentHighlight) {
                    holder.root.setBackgroundColor(Color.rgb(230, 230, 230))
                } else {
                    holder.root.setBackgroundColor(Color.rgb(255, 255, 255))
                }

                holder.writer.text = item.writer
                holder.time.text = item.time.toDate().formatTo("yyyy-MM-dd HH:mm")
                holder.articleContent.text = item.contents

                holder.optionButton.setOnClickListener {
                    CommentOptionBottomSheet().setData(
                        item.canDelete,
                        item.uid,
                        activity::deleteComment
                    ).show(activity.supportFragmentManager, "CommentOptionBottomSheet")
                }

                if(item.userImg.isNotBlank()) {
                    Glide.with(activity)
                        .load(item.userImg)
                        .into(holder.profileImage)
                } else {
                    holder.profileImage.setImageResource(R.drawable.loop_logo)
                }
            }
            is AdvertisementViewHolder -> {
                //TODO 광고 댓글?
            }
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun getItemViewType(position: Int): Int {
        //TODO 광고 댓글?
        if(position == 0) {
            return ViewType.ARTICLE
        }
        return ViewType.COMMENT
    }

    fun animateLike(icon: ImageView, like: Boolean) {
        icon.clearAnimation()

        if(like) {
            icon.setImageResource(R.drawable.favorite_fill_48px)
        } else {
            icon.setImageResource(R.drawable.favorite_48px)
        }

        icon.scaleX = 0f
        icon.scaleY = 0f

        icon.animate()
            .setInterpolator(OvershootInterpolator())
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(200)
            .start()
    }

    fun resetData(article: ArticleDetail, items: ArrayList<Comment>) {
        this.article = article
        this.items = items
    }

    fun deleteComment(uid: String) {
        for (i in 0..<items.size) {
            if(items[i].uid != uid) {
                continue
            }

            this.items.removeAt(i)
            commentCountView?.text = buildString {
                append("댓글 ")
                append((--article.commentCount).toString())
            }
            this.notifyItemRemoved(i + 1) //게시글 하나 있음
            return
        }
    }

    fun addComment(comment: Comment) {
        this.items.add(0, comment)
        commentCountView?.text = buildString {
            append("댓글 ")
            append((++article.commentCount).toString())
        }
        this.notifyItemInserted(1) //게시글 하나 있음
    }
}
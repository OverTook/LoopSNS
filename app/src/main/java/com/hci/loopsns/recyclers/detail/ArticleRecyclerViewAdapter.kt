package com.hci.loopsns.recyclers.detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

    class ArticleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        val commentCount: TextView = itemView.findViewById(R.id.comment_count_detail)
        val likeCount: TextView = itemView.findViewById(R.id.like_count_detail)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
    }

    class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
                ArticleHolder(view)
            }
            ViewType.COMMENT -> {
                view = inflater.inflate(R.layout.activity_article_detail_comment_item, parent, false)
                CommentHolder(view)
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

        if(holder !is ArticleHolder) {
            return
        }

        commentCountView = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ArticleHolder -> {
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
                    Glide.with(activity)
                        .load(article.images[0])
                        .thumbnail(Glide.with(activity).load(R.drawable.picture_placeholder))
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
                commentCountView?.text = article.commentCount.toString()

                holder.likeCount.text = article.likeCount.toString()
            }
            is CommentHolder -> {
                val item = items[position - 1] //게시글 한 개 빼야함


                holder.writer.text = item.writer
                holder.time.text = item.time
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

    fun deleteComment(uid: String) {
        for (i in 0..<items.size) {
            if(items[i].uid != uid) {
                continue
            }

            this.items.removeAt(i)
            commentCountView?.text = (--article.commentCount).toString()
            this.notifyItemRemoved(i)
            return
        }
    }

    fun addComment(comment: Comment) {
        this.items.add(0, comment)
        commentCountView?.text = (++article.commentCount).toString()
        this.notifyItemInserted(1)
    }

}
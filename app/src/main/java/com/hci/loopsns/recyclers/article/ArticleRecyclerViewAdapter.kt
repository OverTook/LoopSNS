package com.hci.loopsns.recyclers.article

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.R
import com.hci.loopsns.event.CommentListener
import com.hci.loopsns.event.CommentManager
import com.hci.loopsns.event.SubCommentListener
import com.hci.loopsns.view.bottomsheet.ArticleOptionBottomSheet
import com.hci.loopsns.view.bottomsheet.CommentOptionBottomSheet
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.Comment
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.toDate

class ArticleRecyclerViewAdapter(private val activity: ArticleDetailActivity): RecyclerView.Adapter<RecyclerView.ViewHolder>(), CommentListener, SubCommentListener {

    private lateinit var article: ArticleDetail
    private lateinit var comments: ArrayList<Comment>

    private val commentManager = CommentManager.getInstance()

    private var highlightComment: Comment? = null
    private var commentCountView: TextView? = null

    var originalLike: Boolean = false

    init {
        commentManager.registerCommentListener(this)
        commentManager.registerSubCommentListener(this)
    }

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

        val commentCount: TextView = itemView.findViewById(R.id.comment_count)
        val likeCount: TextView = itemView.findViewById(R.id.favorite_count)
        val likeLayout: ConstraintLayout = itemView.findViewById(R.id.article_like)
        val likeIcon: ImageView = itemView.findViewById(R.id.favorite_icon)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.main)

        val writer: TextView = itemView.findViewById(R.id.comment_writer_name)
        val profileImage: ImageView = itemView.findViewById(R.id.comment_profile_image)
        val time: TextView = itemView.findViewById(R.id.comment_time)
        val articleContent: TextView = itemView.findViewById(R.id.comment_body)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
        val replyButton: ImageButton = itemView.findViewById(R.id.replyBtn)

        val replyCount: TextView = itemView.findViewById(R.id.reply_count)
    }

    class HighlightCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.main)

        val writer: TextView = itemView.findViewById(R.id.comment_writer_name)
        val profileImage: ImageView = itemView.findViewById(R.id.comment_profile_image)
        val time: TextView = itemView.findViewById(R.id.comment_time)
        val articleContent: TextView = itemView.findViewById(R.id.comment_body)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
        val replyButton: ImageButton = itemView.findViewById(R.id.replyBtn)

        val replyCount: TextView = itemView.findViewById(R.id.reply_count)
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
            ViewType.HIGHLIGHT_COMMENT -> {
                view = inflater.inflate(R.layout.activity_article_detail_highlight_comment_item, parent, false)
                HighlightCommentViewHolder(view)
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
                if(!article.userImg.isNullOrBlank()) {
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
                            keywords[i].text = buildString {
                                append("#")
                                append(article.keywords[i])
                            }
                        }
                    }
                }

                if (article.images.isNotEmpty()) {
                    holder.articleImage.visibility = View.VISIBLE
                    Glide.with(activity)
                        .load(article.images[0])
                        .thumbnail(Glide.with(activity).load(R.drawable.picture_placeholder))
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                        .into(holder.articleImage)
                } else {
                    holder.articleImage.visibility = View.GONE
                }

                holder.optionButton.setOnClickListener {
                    ArticleOptionBottomSheet().setData(
                        article.canDelete!!,
                        activity::deleteArticle,
                    ) {
                        activity.createDeepLinkShare("")
                    } .show(activity.supportFragmentManager, "ArticleOptionBottomSheet")
                }

                holder.articleContent.text = article.contents

                commentCountView = holder.commentCount
                commentCountView?.text = article.commentCount.toString()

                if(article.isLiked == true) {
                    holder.likeIcon.setImageResource(R.drawable.favorite_fill_48px)
                } else {
                    holder.likeIcon.setImageResource(R.drawable.favorite_48px)
                }

                holder.likeCount.text = article.likeCount.toString()
                holder.likeLayout.setOnClickListener {
                    when(article.isLiked == true) {
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
            is HighlightCommentViewHolder -> {
                if(highlightComment == null) {
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0);
                    return
                }

                if(highlightComment!!.isDeleted) {
                    holder.optionButton.visibility = View.GONE
                    holder.writer.setTextColor(activity.getColor(R.color.sub_text_2))
                    holder.writer.text = activity.getString(R.string.comment_writer_deleted)
                    holder.time.text = ""
                    holder.articleContent.text = activity.getString(R.string.comment_contents_deleted)
                } else {
                    holder.optionButton.visibility = View.VISIBLE
                    holder.writer.setTextColor(activity.getColor(R.color.main_text))
                    holder.writer.text = highlightComment!!.writer
                    holder.time.text = highlightComment!!.time.toDate().formatTo("yyyy-MM-dd HH:mm")
                    holder.articleContent.text = highlightComment!!.contents
                }

                holder.optionButton.setOnClickListener {
                    CommentOptionBottomSheet().setData(
                        false,
                        highlightComment!!.uid,
                        {
                            activity.deleteComment(highlightComment!!.uid)
                        }
                    ) {
                        activity.createDeepLinkShare(highlightComment!!.uid)
                    }.show(activity.supportFragmentManager, "CommentOptionBottomSheet")
                }
                holder.replyButton.setOnClickListener {
                    activity.openSubComment(highlightComment!!, true)
                }

                if(highlightComment!!.subCommentCount > 0) {
                    holder.replyCount.visibility = View.VISIBLE
                    holder.replyCount.text = buildString {
                        append(activity.getString(R.string.article_detail_sub_comment_count_start))
                        append(highlightComment!!.subCommentCount)
                        append(activity.getString(R.string.article_detail_sub_comment_count_end))
                    }
                    holder.replyCount.setOnClickListener {
                        activity.openSubComment(highlightComment!!, false)
                    }
                } else {
                    holder.replyCount.visibility = View.GONE
                }


                if(highlightComment!!.userImg.isNotBlank() && !highlightComment!!.isDeleted) {
                    Glide.with(activity)
                        .load(highlightComment!!.userImg)
                        .into(holder.profileImage)
                } else {
                    holder.profileImage.setImageResource(R.drawable.loop_logo)
                }
            }
            is CommentViewHolder -> {
                val item = comments[(comments.size) - (position - 1)] //역수 취해주기

                //holder.root.setBackgroundColor(ContextCompat.getColor(activity.applicationContext, R.color.activity_background))

                if(item.isDeleted) {
                    holder.optionButton.visibility = View.GONE
                    holder.writer.setTextColor(activity.getColor(R.color.sub_text_2))
                    holder.writer.text = activity.getString(R.string.comment_writer_deleted)
                    holder.time.text = ""
                    holder.articleContent.text = activity.getString(R.string.comment_contents_deleted)
                } else {
                    holder.optionButton.visibility = View.VISIBLE
                    holder.writer.setTextColor(activity.getColor(R.color.main_text))
                    holder.writer.text = item.writer
                    holder.time.text = item.time.toDate().formatTo("yyyy-MM-dd HH:mm")
                    holder.articleContent.text = item.contents
                }

                holder.optionButton.setOnClickListener {
                    CommentOptionBottomSheet().setData(
                        item.canDelete,
                        item.uid,
                        {
                            activity.deleteComment(item.uid)
                        }
                    ) {
                        activity.createDeepLinkShare(item.uid)
                    }.show(activity.supportFragmentManager, "CommentOptionBottomSheet")
                }
                holder.replyButton.setOnClickListener {
                    Log.e("ITEM ", item.contents)
                    Log.e("ITEM ", item.isDeleted.toString())
                    activity.openSubComment(item, true)
                }

                if(item.subCommentCount > 0) {
                    holder.replyCount.visibility = View.VISIBLE
                    holder.replyCount.text = buildString {
                        append(activity.getString(R.string.article_detail_sub_comment_count_start))
                        append(item.subCommentCount)
                        append(activity.getString(R.string.article_detail_sub_comment_count_end))
                    }
                    holder.replyCount.setOnClickListener {
                        activity.openSubComment(item, false)
                    }
                } else {
                    holder.replyCount.visibility = View.GONE
                }


                if(item.userImg.isNotBlank() && !item.isDeleted) {
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

    override fun getItemCount(): Int = comments.size + 2

    override fun getItemViewType(position: Int): Int {
        //TODO 광고 댓글?
        return when(position) {
            0 -> ViewType.ARTICLE
            1 -> ViewType.HIGHLIGHT_COMMENT
            else -> ViewType.COMMENT
        }
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

    fun onDestroy() {
        commentManager.removeCommentListener(this)
        commentManager.removeSubCommentListener(this)
    }

    fun setHighlight(comment: Comment) {
        this.highlightComment = comment
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetData(article: ArticleDetail, newComments: ArrayList<Comment>) {
        newComments.reverse()
        if(highlightComment != null) {
            for(i in 0..<newComments.size) {
                if(newComments[i].uid != highlightComment!!.uid) {
                    continue
                }

                newComments.removeAt(i)
                break
            }
        }

        this.article = article
        this.comments = newComments

        this.notifyDataSetChanged()

        originalLike = article.isLiked ?: false
    }

    fun addComments(newComments: ArrayList<Comment>) {
        newComments.reverse()
        if(highlightComment != null) {
            for(i in 0..<newComments.size) {
                if(newComments[i].uid != highlightComment!!.uid) {
                    continue
                }

                newComments.removeAt(i)
                break
            }
        }

        this.comments.addAll(0, newComments)
        this.notifyItemRangeInserted(comments.size + 2 - newComments.size, newComments.size) //
    }

    override fun onCommentDeleted(uid: String) {
        for (i in 0..<this.comments.size) {
            if(this.comments[i].uid != uid) {
                continue
            }

            //Log.e("DeletedBy", uid + " - " + i.toString())
            this.comments[i].isDeleted = true

//            this.comments.removeAt(i)
            val countString = (--article.commentCount).toString()
            commentCountView?.text = countString
            this.notifyItemChanged((comments.size + 1) - (i)) //게시글 하나 있음
            return
        }
    }

    override fun onCommentCreated(comment: Comment) {
        val countString = (++article.commentCount).toString()
        commentCountView?.text = countString
        this.comments.add(comment)
        this.notifyItemInserted(2) //게시글 하나 있음
    }

    override fun onSubCommentDeleted(parentUid: String, uid: String) {
        for (i in 0..<this.comments.size) {
            if(this.comments[i].uid != parentUid) {
                continue
            }
            comments[i].subCommentCount--
            this.notifyItemChanged((comments.size + 1) - (i)) //게시글 하나 있음
        }
    }

    override fun onSubCommentCreated(parentUid: String, comment: Comment) {
        for (i in 0..<this.comments.size) {
            if(this.comments[i].uid != parentUid) {
                continue
            }
            comments[i].subCommentCount++
            this.notifyItemChanged((comments.size + 1) - (i)) //게시글 하나 있음
        }
    }
}
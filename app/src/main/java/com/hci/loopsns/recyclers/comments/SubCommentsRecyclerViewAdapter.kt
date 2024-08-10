package com.hci.loopsns.recyclers.comments

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.R
import com.hci.loopsns.event.CommentListener
import com.hci.loopsns.event.CommentManager
import com.hci.loopsns.event.SubCommentListener
import com.hci.loopsns.network.Comment
import com.hci.loopsns.network.CommentDeleteResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.toDate
import com.hci.loopsns.view.bottomsheet.CommentOptionBottomSheet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubCommentsRecyclerViewAdapter(private val activity: ArticleDetailActivity): RecyclerView.Adapter<RecyclerView.ViewHolder>(), CommentListener, SubCommentListener {

    private lateinit var parentComment: Comment
    private lateinit var comments: ArrayList<Comment>
    private var highlightComment: Comment? = null

    private val commentManager = CommentManager.getInstance()

    init {
        commentManager.registerCommentListener(this)
        commentManager.registerSubCommentListener(this)
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val writer: TextView = itemView.findViewById(R.id.comment_writer_name)
        val profileImage: ImageView = itemView.findViewById(R.id.comment_profile_image)
        val time: TextView = itemView.findViewById(R.id.comment_time)
        val articleContent: TextView = itemView.findViewById(R.id.comment_body)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
    }

    class SubCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.main)

        val writer: TextView = itemView.findViewById(R.id.comment_writer_name)
        val profileImage: ImageView = itemView.findViewById(R.id.comment_profile_image)
        val time: TextView = itemView.findViewById(R.id.comment_time)
        val articleContent: TextView = itemView.findViewById(R.id.comment_body)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
    }

    class HighlightSubCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: ConstraintLayout = itemView.findViewById(R.id.main)

        val writer: TextView = itemView.findViewById(R.id.comment_writer_name)
        val profileImage: ImageView = itemView.findViewById(R.id.comment_profile_image)
        val time: TextView = itemView.findViewById(R.id.comment_time)
        val articleContent: TextView = itemView.findViewById(R.id.comment_body)

        val optionButton: ImageButton = itemView.findViewById(R.id.optionBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val context = parent.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return when (viewType) {
            ViewType.COMMENT -> {
                view = inflater.inflate(R.layout.bottom_sheet_sub_comment_comment_item, parent, false)
                CommentViewHolder(view)
            }
            ViewType.SUB_COMMENT -> {
                view = inflater.inflate(R.layout.bottom_sheet_sub_comment_sub_comment_item, parent, false)
                SubCommentViewHolder(view)
            }
            ViewType.HIGHLIGHT_SUB_COMMENT -> {
                view = inflater.inflate(R.layout.bottom_sheet_sub_comment_highlight_sub_comment_item, parent, false)
                HighlightSubCommentViewHolder(view)
            }
            else -> TODO()
        }
    }

    override fun getItemCount(): Int = comments.size + 2

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            0 -> ViewType.COMMENT
            1 -> ViewType.HIGHLIGHT_SUB_COMMENT
            else -> ViewType.SUB_COMMENT
        }
    }

    fun onDestroy() {
        commentManager.removeCommentListener(this)
        commentManager.removeSubCommentListener(this)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is CommentViewHolder -> {
                if(parentComment.isDeleted) {
                    holder.writer.setTextColor(activity.getColor(R.color.sub_text_2))
                    holder.optionButton.visibility = View.GONE
                    holder.writer.text = activity.getString(R.string.comment_writer_deleted)
                    holder.time.text = ""
                    holder.articleContent.text = activity.getString(R.string.comment_contents_deleted)
                } else {
                    holder.writer.setTextColor(activity.getColor(R.color.main_text))
                    holder.optionButton.visibility = View.VISIBLE
                    holder.writer.text = parentComment.writer
                    holder.time.text = parentComment.time.toDate().formatTo("yyyy-MM-dd HH:mm")
                    holder.articleContent.text = parentComment.contents
                }

                holder.optionButton.setOnClickListener {
                    CommentOptionBottomSheet().setData(
                        parentComment.canDelete,
                        parentComment.uid,
                        {
                            activity.deleteComment(parentComment.uid)
                        },
                        {
                            activity.createDeepLinkShare(parentComment.uid)
                        }
                    ) {
                        activity.report(parentComment.uid)
                    }.show(activity.supportFragmentManager, "CommentOptionBottomSheet")
                }


                if(parentComment.userImg.isNotBlank() && !parentComment.isDeleted) {
                    Glide.with(activity)
                        .load(parentComment.userImg)
                        .into(holder.profileImage)
                } else {
                    holder.profileImage.setImageResource(R.drawable.loop_logo)
                }
            }
            is HighlightSubCommentViewHolder -> {
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
                            deleteSubCommentRequest(highlightComment!!.uid)
                        },
                        {
                            activity.createDeepLinkShare(parentComment.uid, highlightComment!!.uid)
                        }
                    ) {
                        activity.report(parentComment.uid, highlightComment!!.uid)
                    }.show(activity.supportFragmentManager, "CommentOptionBottomSheet")
                }


                if(highlightComment!!.userImg.isNotBlank() && !highlightComment!!.isDeleted) {
                    Glide.with(activity)
                        .load(highlightComment!!.userImg)
                        .into(holder.profileImage)
                } else {
                    holder.profileImage.setImageResource(R.drawable.loop_logo)
                }
            }
            is SubCommentViewHolder -> {
                val item = comments[position - 2]

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
                            deleteSubCommentRequest(item.uid)
                        },
                        {
                            activity.createDeepLinkShare(parentComment.uid, item.uid)
                        }
                    ) {
                        activity.report(parentComment.uid, item.uid)
                    }.show(activity.supportFragmentManager, "CommentOptionBottomSheet")
                }


                if(item.userImg.isNotBlank() && !item.isDeleted) {
                    Glide.with(activity)
                        .load(item.userImg)
                        .into(holder.profileImage)
                } else {
                    holder.profileImage.setImageResource(R.drawable.loop_logo)
                }
            }
        }

    }

    fun setHighlight(comment: Comment?) {
        this.highlightComment = comment
    }


    fun deleteSubCommentRequest(subCommentId: String) {
        NetworkManager.apiService.deleteSubComment(activity.article!!.uid, parentComment.uid, subCommentId).enqueue(object :
            Callback<CommentDeleteResponse> {
            override fun onResponse(call: Call<CommentDeleteResponse>, response: Response<CommentDeleteResponse>) {
                if(!response.isSuccessful) return

                commentManager.onSubCommentDeleted(parentComment.uid, subCommentId)
            }

            override fun onFailure(call: Call<CommentDeleteResponse>, err: Throwable) {
                Log.e("CommentDelete Failed", err.toString())
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetData(parentComment: Comment, newComments: ArrayList<Comment>) {
        if(highlightComment != null) {
            for(i in 0..<newComments.size) {
                if(newComments[i].uid != highlightComment!!.uid) {
                    continue
                }

                newComments.removeAt(i)
                break
            }
        }

        this.parentComment = parentComment
        this.comments = newComments
        this.notifyDataSetChanged()
    }

    fun addComments(newComments: ArrayList<Comment>) {
        if(highlightComment != null) {
            for(i in 0..<newComments.size) {
                if(newComments[i].uid != highlightComment!!.uid) {
                    continue
                }

                newComments.removeAt(i)
                break
            }
        }

        this.comments.addAll(newComments)
        this.notifyItemRangeInserted(comments.size + 1, newComments.size)
    }

    override fun onCommentDeleted(uid: String) {
        if(uid != parentComment.uid) return

        parentComment.isDeleted = true
        this@SubCommentsRecyclerViewAdapter.notifyItemChanged(0)
    }

    override fun onCommentCreated(comment: Comment) {
    }

    override fun onSubCommentDeleted(parentUid: String, uid: String) {
        for (i in 0..<this.comments.size) {
            if(this.comments[i].uid != uid) {
                continue
            }

            this.comments[i].isDeleted = true
            //this.comments.removeAt(i)
            this.notifyItemChanged(i + 2) //원 댓글, 하이라이트 하나 있음
            return
        }
    }

    override fun onSubCommentCreated(parentUid: String, comment: Comment) {
        this.comments.add(comment)
        this.notifyItemInserted(comments.size + 1) //원 댓글 하나 있음
    }
}
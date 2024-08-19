package com.hci.loopsns.recyclers.search

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.tabs.TabLayout
import com.hci.loopsns.ArticleSearchActivity
import com.hci.loopsns.R
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.storage.models.SearchHistory
import com.hci.loopsns.utils.dp
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.toDate
import com.hci.loopsns.view.bottomsheet.SearchSelectCategoryBottomSheet
import com.skydoves.androidveil.VeilRecyclerFrameView
import org.litepal.LitePal
import kotlin.math.max

object SearchMode {
    const val History = 1
    const val BeforeSearch = 2
    const val IsLoading = 4
    const val NoResults = 8
}

class SearchRecyclerViewAdapter(private val activity: ArticleSearchActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var historyItems: ArrayList<SearchHistory> = LitePal.order("id desc")
        .limit(20)
        .find(SearchHistory::class.java) as ArrayList<SearchHistory>

    private var searchResults: List<ArticleDetail> = ArrayList()
    var currentMode: Int = SearchMode.History or SearchMode.BeforeSearch

    init {
        //notifyItemRangeInserted(0, historyItems.size)
        Log.e("dd", historyItems.size.toString())
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root = itemView.findViewById<ConstraintLayout>(R.id.history_root)
        val text = itemView.findViewById<TextView>(R.id.history_text)
        val delete = itemView.findViewById<ImageButton>(R.id.delete)
    }

    class SearhResultHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cat1 = itemView.findViewById<AppCompatButton>(R.id.cat1)
        val cat2 = itemView.findViewById<AppCompatButton>(R.id.cat2)
        val tabLayout = itemView.findViewById<TabLayout>(R.id.tabLayout)
    }

    class SearhResultItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    class NoResultOrBeforeSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    class ItemLoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loading: VeilRecyclerFrameView = itemView.findViewById(R.id.recyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        if(currentMode and SearchMode.History == SearchMode.History) {
            return ViewType.HISTORY
        }

        if (currentMode and SearchMode.BeforeSearch == SearchMode.BeforeSearch) {
            return ViewType.BEFORE_SEARCH
        }

        if(position == 0) {
            return ViewType.HEADER
        }

        if(currentMode and SearchMode.NoResults == SearchMode.NoResults) {
            return ViewType.NO_RESULTS
        }

        if(currentMode and SearchMode.IsLoading == SearchMode.IsLoading) {
            return ViewType.IS_LOADING
        }

        return ViewType.ARTICLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(activity)
//        if(currentMode and SearchMode.IsLoading == SearchMode.IsLoading) {
//            return ItemLoadingViewHolder(inflater.inflate(R.layout.activity_search_result_loading, parent, false))
//        }

//        if(currentMode and SearchMode.History == SearchMode.History) {
//            return HistoryViewHolder(inflater.inflate(R.layout.activity_search_history_item, parent, false))
//        }
//
//        if(currentMode and SearchMode.NoResults == SearchMode.NoResults) {
//            return NoResultOrBeforeSearchViewHolder(inflater.inflate(R.layout.activity_search_result_no_result, parent, false))
//        } else if (currentMode and SearchMode.BeforeSearch == SearchMode.BeforeSearch) {
//            return NoResultOrBeforeSearchViewHolder(inflater.inflate(R.layout.activity_search_result_before_search, parent, false))
//        }

        return when (viewType) {
            ViewType.HISTORY -> {
                HistoryViewHolder(inflater.inflate(R.layout.activity_search_history_item, parent, false))
            }
            ViewType.HEADER -> {
                SearhResultHeaderViewHolder(inflater.inflate(R.layout.activity_search_result_header, parent, false))
            }
            ViewType.ARTICLE -> {
                SearhResultItemViewHolder(inflater.inflate(R.layout.activity_search_result_item, parent, false))
            }
            ViewType.IS_LOADING -> {
                ItemLoadingViewHolder(inflater.inflate(R.layout.activity_search_result_loading, parent, false))
            }
            ViewType.BEFORE_SEARCH -> {
                NoResultOrBeforeSearchViewHolder(inflater.inflate(R.layout.activity_search_result_before_search, parent, false))
            }
            ViewType.NO_RESULTS -> {
                NoResultOrBeforeSearchViewHolder(inflater.inflate(R.layout.activity_search_result_no_result, parent, false))
            }
            else -> {
                TODO()
            }
        }
    }


    override fun getItemCount(): Int {
        if(currentMode and SearchMode.IsLoading == SearchMode.IsLoading) {
            return 2
        }

        if(currentMode and SearchMode.History == SearchMode.History) {
            if(historyItems.size > 20) {
                return 20
            }
            return historyItems.size
        }

        if (currentMode and SearchMode.BeforeSearch == SearchMode.BeforeSearch) {
            return 1
        }

        if(currentMode and SearchMode.NoResults == SearchMode.NoResults) {
            return 2
        }

        return searchResults.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is HistoryViewHolder -> {
                val item = historyItems[position]

                holder.text.text = item.text
                holder.delete.setOnClickListener {
                    historyItems.remove(item)
                    notifyItemRemoved(position)
                    item.delete()
                }

                holder.root.setOnClickListener {
                    historyItems.remove(item)
                    item.delete()

                    activity.retrieveArticles(item.text)
                    //notifyItemMoved(position, 0)

                }
            }
            is ItemLoadingViewHolder -> {
                holder.loading.setLayoutManager(LinearLayoutManager(activity))
                holder.loading.addVeiledItems(1)
                holder.loading.veil()
                //holder.loading.on
            }
            is SearhResultHeaderViewHolder -> {
                holder.cat1.setOnClickListener {
                    SearchSelectCategoryBottomSheet(activity, R.array.categories1_array) {
                        holder.cat1.text = it
                        activity.changeCategoryA(it)
                    }.show(activity.supportFragmentManager, "SearchSelectCategoryBottomSheet")
                }
                holder.cat2.setOnClickListener {
                    SearchSelectCategoryBottomSheet(activity, R.array.categories2_array) {
                        holder.cat2.text = it
                        activity.changeCategoryB(it)
                    }.show(activity.supportFragmentManager, "SearchSelectCategoryBottomSheet")
                }

                holder.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        activity.changeType(tab.position)
                    }

                    override fun onTabUnselected(p0: TabLayout.Tab) {
                    }

                    override fun onTabReselected(p0: TabLayout.Tab) {
                    }
                })
            }
            is SearhResultItemViewHolder -> {
                val item = searchResults[position - 1]

                holder.root.setOnClickListener {
                    activity.onClickArticle(item.uid)
                }

                holder.contents.text = item.contents
                holder.time.text =  item.time.toDate().formatTo("yyyy-MM-dd HH:mm")

                holder.category1.text = item.intention
                holder.category2.text= item.subject

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

    @SuppressLint("NotifyDataSetChanged")
    fun loadHistory() {
        historyItems.forEach {
            Log.e("ITEM", it.text)
        }
        Log.e("=================", "================")
        currentMode = currentMode or SearchMode.History
        this.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun search(text: String) {
        currentMode = SearchMode.IsLoading //3

        val index = historyItems.indexOfFirst { it.text == text }
        if (index != -1) {
            val item = historyItems.removeAt(index)
            item.delete()
            val newItem = SearchHistory(item.text)
            newItem.saveThrows()
            historyItems.add(0,  newItem)
        } else {
            val item = SearchHistory(text)
            item.saveThrows()
            historyItems.add(0, item)

            historyItems.forEach {
                Log.e("ITEM", it.text)
            }
            Log.e("=================", "================")
            if(historyItems.size > 20) {
                historyItems.removeAt(20).delete()
            }

            this.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchWithoutHistoryAdd() {
        currentMode = SearchMode.IsLoading //3
        this.notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun loadData(searchResults: List<ArticleDetail>) { //데이터 로드
        currentMode = currentMode and SearchMode.IsLoading.inv() //로딩 해제

        if(searchResults.isEmpty()) {
            currentMode = currentMode or SearchMode.NoResults //결과가 빔
            this.notifyDataSetChanged()
            return
        }
        currentMode = currentMode and SearchMode.NoResults.inv()

        this.searchResults = searchResults
        this.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reloadData() { //히스토리 내리는 거
        currentMode = currentMode and SearchMode.History.inv()
        this.notifyDataSetChanged()
    }

    fun updateCommentcount(articleId: String, increment: Int) {
        for(i in searchResults.indices) {
            if(searchResults[i].uid != articleId) {
                continue
            }

            searchResults[i].commentCount += increment
            notifyItemChanged(i + 1)
            return
        }
    }

    fun updateLikeCount(articleId: String, increment: Int) {
        for(i in searchResults.indices) {
            if(searchResults[i].uid != articleId) {
                continue
            }

            searchResults[i].likeCount += increment
            notifyItemChanged(i + 1)
            return
        }
    }
}
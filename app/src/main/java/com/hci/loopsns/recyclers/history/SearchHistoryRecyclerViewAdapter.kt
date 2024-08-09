package com.hci.loopsns.recyclers.history

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.R
import com.hci.loopsns.storage.models.SearchHistory
import org.litepal.LitePal

class SearchHistoryRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var historyItems: ArrayList<SearchHistory> = LitePal.order("id desc")
        .limit(20)
        .find(SearchHistory::class.java) as ArrayList<SearchHistory>

    init {
        //notifyItemRangeInserted(0, historyItems.size)
        Log.e("dd", historyItems.size.toString())
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root = itemView.findViewById<ConstraintLayout>(R.id.history_root)
        val text = itemView.findViewById<TextView>(R.id.history_text)
        val delete = itemView.findViewById<ImageButton>(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return HistoryViewHolder(inflater.inflate(R.layout.fragment_map_overview_search_history_item, parent, false))
    }

    override fun getItemCount(): Int {
        if(historyItems.size > 20) {
            return 20
        }
        return historyItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = historyItems[position]
        Log.e("item", item.text)

        (holder as HistoryViewHolder).text.text = item.text
        holder.delete.setOnClickListener {
            historyItems.remove(item)
            notifyItemRemoved(position)
            item.delete()
        }

        holder.root.setOnClickListener {
            Log.e("Click", "Search History")
        }
    }

    fun addHistory(text: String) {
        val item = SearchHistory(text)
        item.saveThrows()

        historyItems.add(0, item)
        notifyItemInserted(0)

        if(historyItems.size > 20) {
            historyItems.last().delete()
            notifyItemRemoved(historyItems.size - 1)
        }
    }
}
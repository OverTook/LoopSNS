package com.hci.loopsns.recyclers.search.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.ArticleCreateActivity
import com.hci.loopsns.ArticleSearchActivity
import com.hci.loopsns.R
import com.hci.loopsns.view.bottomsheet.SearchSelectCategoryBottomSheet

class CategoryAdapter(private val dataSet: Array<String>, private val clickAction: (String) -> Unit, private val dissmissAction: () -> Unit) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    // ViewHolder 정의
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root1: ConstraintLayout = view.findViewById(R.id.cat1_root)
        val root2: ConstraintLayout = view.findViewById(R.id.cat2_root)
        val cat1: TextView = view.findViewById(R.id.cat1)
        val cat2: TextView = view.findViewById(R.id.cat2)
    }

    // 레이아웃을 생성하여 ViewHolder에 전달
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bottom_sheet_search_select_category_item, parent, false)
        return ViewHolder(view)
    }

    // 데이터를 ViewHolder의 레이아웃에 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cat1.text = dataSet[position * 2]
        holder.root1.setOnClickListener {
            clickAction(dataSet[position * 2])
            dissmissAction()
        }

        if(dataSet.size == position * 2 + 1) {
            holder.root2.visibility = View.GONE
            return
        }

        holder.cat2.text = dataSet[position * 2 + 1]
        holder.root2.setOnClickListener {
            clickAction(dataSet[position * 2 + 1])
            dissmissAction()
        }
    }

    // 아이템의 개수를 반환
    override fun getItemCount() = (dataSet.size + 1) / 2
}
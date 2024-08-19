package com.hci.loopsns.recyclers.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.R

class SelectCategoryFilterAdapter(private val dataSet: List<String>, private val selectedSet: ArrayList<String>, private val clickAction: (String, Boolean) -> Unit) : RecyclerView.Adapter<SelectCategoryFilterAdapter.ViewHolder>() {

    // ViewHolder 정의
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root2: ConstraintLayout = view.findViewById(R.id.item2_root)
        val item1: CheckBox = view.findViewById(R.id.item1)
        val item2: CheckBox = view.findViewById(R.id.item2)
    }

    // 레이아웃을 생성하여 ViewHolder에 전달
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bottom_sheet_report_select_category_item, parent, false)
        return ViewHolder(view)
    }

    // 데이터를 ViewHolder의 레이아웃에 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item1.text = dataSet[position * 2]
        if(selectedSet.contains(dataSet[position * 2])) {
            holder.item1.isChecked = true
        }
        holder.item1.setOnCheckedChangeListener { _, isChecked ->
            clickAction(dataSet[position * 2], isChecked)
        }

        if(dataSet.size == position * 2 + 1) {
            holder.root2.visibility = View.GONE
            return
        }

        holder.item2.text = dataSet[position * 2 + 1]
        if(selectedSet.contains(dataSet[position * 2 + 1])) {
            holder.item2.isChecked = true
        }
        holder.item2.setOnCheckedChangeListener { _, isChecked ->
            clickAction(dataSet[position * 2 + 1], isChecked)
        }
    }

    // 아이템의 개수를 반환
    override fun getItemCount() = (dataSet.size + 1) / 2
}
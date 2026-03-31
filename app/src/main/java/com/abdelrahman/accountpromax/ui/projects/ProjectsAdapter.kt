package com.abdelrahman.accountpromax.ui.projects

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abdelrahman.accountpromax.models.ProjectEntity

class ProjectsAdapter(
    private val items: List<ProjectEntity>,
    private val selectedId: Long,
    private val typeface: Typeface,
    private val onClick: (ProjectEntity) -> Unit,
    private val onLongClick: (ProjectEntity) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tv.typeface = typeface
        holder.tv.text = if (item.id == selectedId) "✓ ${item.name}" else item.name
        holder.tv.setOnClickListener { onClick(item) }
        holder.tv.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount() = items.size
    class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)
}

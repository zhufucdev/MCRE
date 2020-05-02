package com.zhufucdev.mcre.recycler_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.activity.ProjectActivity
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.element.BaseElement
import com.zhufucdev.mcre.project_edit.element.ElementType

class ElementCardAdapter(val project: EditableProject) : RecyclerView.Adapter<ElementCardAdapter.CardHolder>() {
    class CardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recycler = itemView.findViewById<RecyclerView>(R.id.recycler_view)
        val help = itemView.findViewById<AppCompatImageView>(R.id.btn_help)
        val title = itemView.findViewById<TextView>(R.id.text_title)
        val adapter get() = recycler.adapter as ElementAdapter?
        fun bond(project: EditableProject, type: ElementType) {
            val adapter = recycler.adapter as ElementAdapter?
            if (adapter == null || adapter.type != type) {
                recycler.adapter = ElementAdapter(project, type)
                recycler.layoutManager = LinearLayoutManager(itemView.context)
                ProjectActivity.attachHelperTo(help, type.helperTitle, type.helperContent)
                title.setText(type.helperTitle)
            } else {
                adapter.refresh()
            }
        }
    }

    private var types = project.elementsByType().keys.toList()

    fun refresh() {
        val old = types
        types = project.elementsByType().keys.toList()
        DiffUtil.calculateDiff(DiffCallback(old, types)).dispatchUpdatesTo(this)
    }

    private val mItemClickListener = hashMapOf<ElementType, (BaseElement) -> Unit>()
    fun setItemClickListener(type: ElementType, l: (BaseElement) -> Unit) {
        mItemClickListener[type] = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder =
        CardHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_element_card, parent, false))

    override fun getItemCount(): Int = types.size

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        holder.bond(project, types[position])
        holder.adapter?.setItemClickListener { mItemClickListener[types[position]]?.invoke(it) }
    }

    inner class DiffCallback(private val old: List<ElementType>, private val new: List<ElementType>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            areContentsTheSame(oldItemPosition, newItemPosition)

        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == new[newItemPosition]
    }
}
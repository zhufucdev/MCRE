package com.zhufucdev.mcre.recycler_view

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.exception.ElementNotFoundException
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.element.BaseElement
import com.zhufucdev.mcre.project_edit.element.ElementType
import com.zhufucdev.mcre.utility.nameify
import com.zhufucdev.mcre.view.SimpleItemView
import java.util.*

class ElementAdapter(private val project: EditableProject, val type: ElementType)
    : RecyclerView.Adapter<ElementAdapter.ElementHolder>() {
    class ElementHolder(context: Context) : RecyclerView.ViewHolder(SimpleItemView(context)) {
        val item = itemView as SimpleItemView
        fun bond(element: BaseElement) {
            val context = item.context
            item.title = element.title.get(context)
            item.subtitle = element.description.get(context)
            item.setIcon(R.drawable.ic_short_text_black)
            item.viewTreeObserver.addOnDrawListener {
                item.updateLayoutParams<ViewGroup.LayoutParams> {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
        }
    }

    private var elements: List<BaseElement> = fetchElements()
    private fun fetchElements() = project.elementsByType()[type]
        ?: throw ElementNotFoundException(type.name.toLowerCase(Locale.ROOT).nameify())
    fun refresh() {
        val old = elements
        elements = fetchElements()
        DiffUtil.calculateDiff(DiffCallback(old, elements)).dispatchUpdatesTo(this)
    }

    private var mItemClickListener: ((BaseElement) -> Unit)? = null
    fun setItemClickListener(l: (BaseElement) -> Unit) {
        mItemClickListener = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementHolder =
        ElementHolder(parent.context)

    override fun getItemCount(): Int = elements.size

    override fun onBindViewHolder(holder: ElementHolder, position: Int) {
        holder.bond(elements[position])
        holder.item.setOnClickListener {
            mItemClickListener?.invoke(elements[position])
        }
    }

    class DiffCallback(private val old: List<BaseElement>, private val new : List<BaseElement>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == new[newItemPosition]

        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition].id == new[newItemPosition].id
    }
}
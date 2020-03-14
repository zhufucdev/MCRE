package com.zhufucdev.mcre.recycler_view

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.utility.MCTextRender
import com.zhufucdev.mcre.utility.TextUtil
import kotlin.math.roundToInt

class ColorsAdapter : RecyclerView.Adapter<ColorsAdapter.ColorHolder>() {

    inner class ColorHolder(context: Context) :
        RecyclerView.ViewHolder(FrameLayout(context)) {
        private val context get() = itemView.context

        fun setColor(color: TextUtil.TextColor) {
            val view: View
            if (!color.isColor) {
                view = TextView(itemView.context)
                (itemView as FrameLayout).apply {
                    removeAllViews()
                    addView(view)
                }
                view.textSize = context.resources.displayMetrics.density * 8
                MCTextRender(view).doRender(color.code)
            } else {
                view = View(context)
                (itemView as FrameLayout).apply {
                    addView(view)
                }

                fun getDrawable(id: Int, color: Int): Drawable {
                    return view.context.getDrawable(id)!!.apply {
                        setTint(view.context.resources.getColor(color))
                    }
                }
                view.background = getDrawable(R.drawable.color_holder, color.resourceID)
                if (color.backgroundID != -1)
                    itemView.background = getDrawable(R.drawable.color_holder_background, color.backgroundID)
            }
            view.updateLayoutParams<FrameLayout.LayoutParams> {
                setMargins(view.resources.getDimension(R.dimen.padding_big).roundToInt())
                gravity = Gravity.CENTER
                if (view is TextView) {
                    height = FrameLayout.LayoutParams.WRAP_CONTENT
                    width = FrameLayout.LayoutParams.WRAP_CONTENT
                    return
                }
                val l = (context.resources.displayMetrics.density * 26).roundToInt()
                height = l
                width = l
            }
        }
    }

    private val colors get() = TextUtil.TextColor.values()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorHolder = ColorHolder(parent.context)

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: ColorHolder, position: Int) {
        holder.setColor(colors[position])
        holder.itemView.setOnClickListener { mOnSelectionClickListener?.invoke(colors[position]) }
    }

    private var mOnSelectionClickListener: ((TextUtil.TextColor) -> Unit)? = null
    fun setOnSelectionClickListener(action: (TextUtil.TextColor) -> Unit) {
        mOnSelectionClickListener = action
    }
}
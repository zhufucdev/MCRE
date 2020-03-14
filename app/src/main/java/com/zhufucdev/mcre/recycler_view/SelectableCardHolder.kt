package com.zhufucdev.mcre.recycler_view

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.mcre.R

abstract class SelectableCardHolder(itemVIew: View) : RecyclerView.ViewHolder(itemVIew) {
    abstract val card: CardView
    val isCardSelected get() = card.cardBackgroundColor.defaultColor == card.context.resources.getColor(R.color.colorAccentLight)
    lateinit var oldBackgroundColor: ColorStateList
    open fun selectCard() {
        if (!::oldBackgroundColor.isInitialized) oldBackgroundColor = card.cardBackgroundColor
        card.setCardBackgroundColor(card.context.resources.getColor(R.color.colorAccentLight))
    }

    open fun unselectCard() {
        if (::oldBackgroundColor.isInitialized)
            card.setCardBackgroundColor(oldBackgroundColor)
        else {
            val tv = TypedValue()
            card.context.theme.resolveAttribute(R.attr.colorBackgroundFloating, tv, true)
            card.setCardBackgroundColor(card.resources.getColor(tv.resourceId))
        }
    }
}
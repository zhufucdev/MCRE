package com.zhufucdev.mcre.recycler_view

import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.mcre.R

abstract class SelectableCardHolder(itemVIew: View): RecyclerView.ViewHolder(itemVIew) {
    abstract val card: CardView
    val isCardSelected get() = card.cardBackgroundColor.defaultColor == card.context.resources.getColor(R.color.colorAccentLight)
    open fun selectCard() {
        card.setCardBackgroundColor(card.context.resources.getColor(R.color.colorAccentLight))
    }

    open fun unselectCard() {
        card.setCardBackgroundColor(card.context.resources.getColor(android.R.color.white))
    }
}
package com.zhufucdev.mcre.utility

import androidx.recyclerview.widget.RecyclerView

inline fun <T : RecyclerView.ViewHolder> RecyclerView.forEachAdapter(l: (T) -> Unit) =
    forEachAdapterIndexed<T> { t, _ -> l(t) }

inline fun <T : RecyclerView.ViewHolder> RecyclerView.forEachAdapterIndexed(l: (T, Int) -> Unit) {
    for (i in 0 until childCount) l(findViewHolderForAdapterPosition(i) as T, i)
}
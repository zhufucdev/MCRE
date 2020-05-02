package com.zhufucdev.mcre.project_edit

import android.content.Context
import com.zhufucdev.mcre.internal.ContextStringSupplier

class Name(val text: Any) : ContextStringSupplier {
    override fun get(context: Context): String =
        when (text) {
            is Int -> context.getString(text)
            is List<*> -> {
                buildString {
                    text.forEach {
                        if (it is Name)
                            append(it.get(context))
                        else
                            append(it.toString())
                    }
                }
            }
            else -> text.toString()
        }

    override fun equals(other: Any?): Boolean = other is Name && other.text == text

    override fun hashCode(): Int = text.hashCode() + 32

    override fun toString(): String = text.toString()
}
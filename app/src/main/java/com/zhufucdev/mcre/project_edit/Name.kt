package com.zhufucdev.mcre.project_edit

import android.content.Context
import com.zhufucdev.mcre.internal.ContextStringSupplier

class Name(val text: Any): ContextStringSupplier {
    override fun get(context: Context) = if (text is Int) context.getString(text) else text.toString()

    override fun equals(other: Any?): Boolean = other is Name && other.text == text

    override fun hashCode(): Int = text.hashCode() + 32

    override fun toString(): String = text.toString()
}
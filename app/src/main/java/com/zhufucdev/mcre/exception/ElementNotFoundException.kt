package com.zhufucdev.mcre.exception

import android.content.Context
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.internal.ContextStringSupplier
import com.zhufucdev.mcre.project_edit.Name

class ElementNotFoundException(private val where: Name) : Exception("No such element: $where"), ContextStringSupplier {
    override fun get(context: Context): String =
        context.getString(R.string.exception_element_not_found, where.get(context))
}
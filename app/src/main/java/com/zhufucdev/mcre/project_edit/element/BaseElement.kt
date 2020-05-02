package com.zhufucdev.mcre.project_edit.element

import com.zhufucdev.mcre.project_edit.Name

abstract class BaseElement : ISerializable {
    abstract val title: Name
    abstract val description: Name
    abstract val type: ElementType
    val id: Int
    init {
        // Thread safe
        synchronized(Companion) {
            id = Companion.id ++
        }
    }

    override fun equals(other: Any?): Boolean =
        other is BaseElement && other.type == type && other.title == title && other.description == description

    override fun hashCode(): Int {
        var result = title.hashCode() * 2 + 31
        result += description.hashCode() * 2 + 31
        result += id.hashCode() * 2 + 31
        return result
    }

    companion object {
        private var id = 0
    }
}
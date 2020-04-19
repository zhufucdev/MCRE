package com.zhufucdev.mcre.project_edit.element

import com.zhufucdev.mcre.project_edit.Name

abstract class BaseElement : ISerializable {
    abstract val title: Name
    abstract val description: Name

    override fun equals(other: Any?): Boolean =
        other is BaseElement && other.title == title && other.description == description

    override fun hashCode(): Int {
        var result = title.hashCode() * 2 + 31
        result += description.hashCode() * 2 + 31
        return result
    }
}
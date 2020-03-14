package com.zhufucdev.mcre.project_edit.element

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.zhufucdev.mcre.project_edit.EditableProject

abstract class StringElement: BaseElement() {
    var value: String = ""
    override fun serialize(project: EditableProject): JsonElement = JsonPrimitive(value)
}
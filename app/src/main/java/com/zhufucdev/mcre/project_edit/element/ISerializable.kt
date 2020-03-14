package com.zhufucdev.mcre.project_edit.element

import com.google.gson.JsonElement
import com.zhufucdev.mcre.project_edit.EditableProject

interface ISerializable {
    fun serialize(project: EditableProject): JsonElement
}
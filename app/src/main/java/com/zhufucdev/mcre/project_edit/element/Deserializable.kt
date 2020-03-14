package com.zhufucdev.mcre.project_edit.element

import com.google.gson.JsonElement

interface Deserializable {
    fun deserialize(dataStore: JsonElement): BaseElement
}
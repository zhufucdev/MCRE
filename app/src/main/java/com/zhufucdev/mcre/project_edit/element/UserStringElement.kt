package com.zhufucdev.mcre.project_edit.element

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.Name
import com.zhufucdev.mcre.utility.nameify

class UserStringElement(override val title: Name, override val description: Name): StringElement() {
    override val type: ElementType
        get() = ElementType.USER

    override fun serialize(project: EditableProject) = JsonObject().apply {
        addProperty("title", title.text.toString())
        addProperty("description", description.text.toString())
        addProperty("text", value)
    }

    companion object: Deserializable {
        override fun deserialize(dataStore: JsonElement): BaseElement {
            val data = dataStore.asJsonObject
            val r = UserStringElement(data["title"].asString.nameify(), data["description"].asString.nameify())
            r.value = data["text"].asString
            return r
        }
    }
}
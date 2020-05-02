package com.zhufucdev.mcre.project_edit.element

import com.google.gson.JsonElement
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.Name
import com.zhufucdev.mcre.utility.nameify

class PackDescription : StringElement {
    override val title: Name = R.string.name_description.nameify()
    override val description: Name = R.string.info_description.nameify()
    override val type: ElementType
        get() = ElementType.REQUIRED

    constructor() : super() {
        value = Env.presentActivity.getString(R.string.title_default_project_description)
    }

    constructor(value: String) : super() {
        this.value = value
    }

    companion object : Deserializable {
        override fun deserialize(dataStore: JsonElement): BaseElement = PackDescription(dataStore.asString)
    }
}
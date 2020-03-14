package com.zhufucdev.mcre.project_edit.element

import com.google.gson.JsonElement
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.Name
import com.zhufucdev.mcre.utility.nameify
import java.util.*

class PackName : StringElement {
    override val title: Name = Env.formatDate(Date()).nameify()
    override val description: Name = R.string.info_name.nameify()

    constructor() : super() {
        value = Env.formatDate(Date())
    }

    constructor(value: String) : super() {
        this.value = value
    }

    companion object : Deserializable {
        override fun deserialize(dataStore: JsonElement): PackName = PackName(dataStore.asString)
    }
}
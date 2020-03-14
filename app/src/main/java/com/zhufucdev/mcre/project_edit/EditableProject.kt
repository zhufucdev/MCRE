package com.zhufucdev.mcre.project_edit

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import com.zhufucdev.mcre.project_edit.element.BaseElement
import com.zhufucdev.mcre.project_edit.element.Deserializable
import com.zhufucdev.mcre.project_edit.element.PackDescription
import com.zhufucdev.mcre.project_edit.element.PackName
import java.io.File
import kotlin.reflect.full.companionObjectInstance

class EditableProject {
    val name: PackName
        get() {
            val result: PackName
            val index = elements.indexOfFirst { it is PackName }
            if (index == -1) {
                result = PackName()
                elements.add(result)
            } else {
                result = elements[index] as PackName
            }
            return result
        }
    val description: PackDescription
        get() {
            val result: PackDescription
            val index = elements.indexOfFirst { it is PackDescription }
            if (index == -1) {
                result = PackDescription()
                elements.add(result)
            } else {
                result = elements[index] as PackDescription
            }
            return result
        }
    var file: File? = null
    private val elements: ArrayList<BaseElement>

    constructor() {
        elements = arrayListOf(PackName(), PackDescription())
    }

    constructor(root: File) {
        file = root
        elements = arrayListOf()
        val header = JsonParser().parse(root.reader()).asJsonArray
        val packagePrefix = this::class.qualifiedName!!.removeSuffix("." + this::class.simpleName!!)
        header.forEach {
            val obj = it.asJsonObject
            val name = obj["name"].asString
            elements.add(
                (ClassLoader.getSystemClassLoader()
                    .loadClass("$packagePrefix.$name").kotlin.companionObjectInstance as Deserializable)
                    .deserialize(obj["value"])
            )
        }
    }

    fun save(dest: File? = null) {
        if (file == null) throw IllegalArgumentException("{dest} must not be null.")
        dest?.let { file = it }
        val writer = beginGenerateHeader()
        elements.forEach {
            val v = it.serialize(this)
            writer
                .beginObject()
                .name("name").value(it::class.simpleName)
                .name("value").jsonValue(v.toString())
                .endObject()
        }
        endHeader(writer)
    }

    private fun beginGenerateHeader(): JsonWriter {
        val writer = GsonBuilder()
            .setPrettyPrinting()
            .create()
            .newJsonWriter(File(file!!, "header.json").writer())
        // Name & description
        writer.beginArray()
        return writer
    }

    private fun endHeader(writer: JsonWriter) {
        // EOF
        writer.endArray().flush()
    }
}
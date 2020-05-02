package com.zhufucdev.mcre.project_edit

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import com.zhufucdev.mcre.project_edit.element.*
import com.zhufucdev.mcre.project_edit.operation_like.Operation
import com.zhufucdev.mcre.project_edit.operation_like.UndoRedo
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
    val elements: ArrayList<BaseElement>
    fun elementsByType(): Map<ElementType, List<BaseElement>> {
        val map = hashMapOf<ElementType, List<BaseElement>>()
        elements.forEach {
            if (!map.containsKey(it.type)) {
                map[it.type] = arrayListOf()
            }
            (map[it.type]!! as ArrayList).add(it)
        }
        return map.toSortedMap(Comparator { o1, o2 -> o1.order - o2.order })
    }
    val operationStack = OperationStack()

    constructor() {
        elements = arrayListOf(PackName(), PackDescription())
    }

    constructor(root: File) {
        file = root
        elements = arrayListOf()
        val header = JsonParser().parse(File(root, "header.json").reader()).asJsonArray
        val packagePrefix = this::class.qualifiedName!!.removeSuffix("." + this::class.simpleName!!)
        header.forEach {
            val obj = it.asJsonObject
            val name = obj["name"].asString
            elements.add(
                (Class.forName("$packagePrefix.element.$name").kotlin.companionObjectInstance as Deserializable)
                    .deserialize(obj["value"])
            )
        }
    }

    fun save(dest: File? = null) {
        if (file == null && dest == null) throw IllegalArgumentException("{dest} must not be null.")
        dest?.let {
            file = it
            if (!it.exists()) it.mkdirs()
        }
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

    class OperationStack {
        private val list = ArrayList<UndoRedo>()
        var header = -1
            private set
        val canUndo: Boolean get() = header >= 0
        val canRedo: Boolean get() = header < list.lastIndex

        private var mStatusChangeListener: ((Boolean, Boolean) -> Unit)? = null
        fun setStatusChangeListener(l: (Boolean, Boolean) -> Unit) {
            mStatusChangeListener = l
        }

        fun <T: UndoRedo> add(operation: T): T {
            if (list.lastIndex != header)
                for (i in list.lastIndex downTo header + 1) {
                    list.removeAt(i)
                }
            list.add(operation)
            header = list.lastIndex
            mStatusChangeListener?.invoke(canUndo, canRedo)
            return operation
        }

        fun undo() {
            list[header].undo()
            header--

            mStatusChangeListener?.invoke(canUndo, canRedo)
        }

        fun redo() {
            header++
            list[header].redo()

            mStatusChangeListener?.invoke(canUndo, canRedo)
        }
    }

    companion object {
        fun isProject(file: File): Boolean {
            return file.isDirectory && File(file, "header.json").isFile
        }
    }
}
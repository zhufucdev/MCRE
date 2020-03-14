package com.zhufucdev.mcre.project_edit.element

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.zhufucdev.mcre.project_edit.EditableProject
import java.io.File
import java.io.FileWriter

abstract class FileElement : BaseElement() {
    abstract val path: String

    abstract fun writeToFile(writer: FileWriter)
    final override fun serialize(project: EditableProject): JsonElement {
        writeToFile(FileWriter(File(project.file, path)))
        return JsonObject().apply {
            addProperty("path", path)
        }
    }
}
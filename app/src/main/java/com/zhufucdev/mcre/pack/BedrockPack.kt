package com.zhufucdev.mcre.pack

import com.google.gson.JsonParser
import com.zhufucdev.mcre.exceptions.JsonElementNotFoundException
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class BedrockPack internal constructor(root: File) : ResourcesPack(root) {
    val manifestFile get() = File(root, "manifest.json")
    override val icon: File
        get() = File(root, "pack_icon.png")
    companion object {
        fun from(file: File): BedrockPack {
            val r = BedrockPack(file)
            val parser = JsonParser()

            //Handle manifest JSON
            val manifestFile = r.manifestFile
            if (!manifestFile.exists()) throw FileNotFoundException("manifest.json doesn't exist.")
            val manifest = parser.parse(manifestFile.reader()).asJsonObject
            //JSON -> check all elements.
            if (!manifest.has("header")) throw JsonElementNotFoundException("header", manifestFile.path)
            val manifestHeader = manifest["header"].asJsonObject
            listOf("name", "description", "uuid", "version").forEach {
                if (!manifestHeader.has(it)) throw JsonElementNotFoundException("header/$it", manifestFile.path)
            }
            r.header = manifestHeader.let {
                ManifestHeader(
                    name = it["name"].asString,
                    uuid = UUID.fromString(it["uuid"].asString),
                    description = it["description"].asString,
                    version = Version.parse(it["version"].asJsonArray),
                    minVersion = it["min_engine_version"]?.asJsonArray?.let { v -> Version.parse(v) }
                )
            }

            return r
        }
    }
}
package com.zhufucdev.mcre.pack

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.JsonParser
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.exception.FileNotFoundException
import com.zhufucdev.mcre.exception.JsonElementNotFoundException
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class BedrockPack private constructor(root: File) : ResourcesPack(root) {
    val manifestFile get() = File(root, "manifest.json")
    val iconFile: File
        get() = File(root, "pack_icon.png")

    class BedrockManifest(
        name: String? = null,
        val uuid: UUID,
        description: String,
        version: Version,
        val minVersion: Version? = null
    ) : ManifestHeader(name, description, version)

    companion object {
        fun from(file: File): BedrockPack {
            val r = BedrockPack(file)
            val parser = JsonParser()

            //Handle manifest JSON
            val manifestFile = r.manifestFile
            if (!manifestFile.exists()) throw FileNotFoundException(manifestFile)
            val manifest = parser.parse(manifestFile.reader()).asJsonObject
            //JSON -> check all elements.
            if (!manifest.has("header")) throw JsonElementNotFoundException("/header", manifestFile.path)
            val manifestHeader = manifest["header"].asJsonObject
            listOf("name", "description", "uuid", "version").forEach {
                if (!manifestHeader.has(it)) throw JsonElementNotFoundException("/header/$it", manifestFile.path)
            }
            r.header = manifestHeader.let {
                BedrockManifest(
                    name = it["name"].asString,
                    uuid = UUID.fromString(it["uuid"].asString),
                    description = it["description"].asString,
                    version = Version.parse(it["version"].asJsonArray),
                    minVersion = it["min_engine_version"]?.asJsonArray?.let { v -> Version.parse(v) }
                )
            }
            r.icon = try {
                Env.threadPool.submit<Bitmap> { BitmapFactory.decodeStream(r.iconFile.inputStream()) }[5, TimeUnit.SECONDS]
            } catch (e: Exception) {
                null
            }

            return r
        }
    }
}
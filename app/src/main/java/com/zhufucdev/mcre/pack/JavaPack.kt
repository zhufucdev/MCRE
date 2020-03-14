package com.zhufucdev.mcre.pack

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.exception.FileNotFoundException
import com.zhufucdev.mcre.exception.JsonElementNotFoundException
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

class JavaPack private constructor(root: File) : ResourcesPack(root) {
    class JavaMeta(
        name: String,
        description: String,
        version: JavaVersion
    ) : ManifestHeader(name, description, version)

    class JavaVersion(format: Int) : Version(listOf(format))

    companion object {
        fun from(file: File): JavaPack {
            val r = JavaPack(file)
            fun readMcmeta(mcmeta: JsonObject, mcmetaFile: File) {
                val pack = mcmeta["pack"]
                if (!pack.isJsonObject) throw JsonElementNotFoundException("/pack", mcmetaFile.name)
                // => Check all elements
                listOf("description", "pack_format").forEach {
                    if (!pack.asJsonObject.has(it)) throw JsonElementNotFoundException("/pack/$it", mcmetaFile.name)
                }
                r.header = JavaMeta(
                    name = file.nameWithoutExtension,
                    description = pack.asJsonObject["description"].asString,
                    version = JavaVersion(pack.asJsonObject["pack_format"].asInt)
                )
            }
            if (file.isDirectory) {
                // Read icon.
                r.icon = try {
                    Env.threadPool.submit<Bitmap> {
                        BitmapFactory.decodeStream(
                            File(
                                file,
                                "pack.png"
                            ).inputStream()
                        )
                    }[5, TimeUnit.SECONDS]
                } catch (e: Exception) {
                    null
                }
                // Read pack.mcmeta
                val mcmetaFile = File(file, "pack.mcmeta")
                if (!mcmetaFile.exists()) throw FileNotFoundException(mcmetaFile)
                val mcmeta = JsonParser().parse(mcmetaFile.reader()).asJsonObject
                readMcmeta(mcmeta, mcmetaFile)
            } else {
                // From a zip file:
                val zf = ZipFile(file)
                val iconEntry = zf.getEntry("pack.png")
                if (iconEntry != null)
                //Read icon.
                    r.icon = try {
                        Env.threadPool.submit<Bitmap> {
                            BitmapFactory.decodeStream(zf.getInputStream(iconEntry))
                        }[5, TimeUnit.SECONDS]
                    } catch (e: Exception) {
                        null
                    }
                // Read mcmeta
                val mcmetaEntry = zf.getEntry("pack.mcmeta")
                    ?: throw FileNotFoundException(File(file, "pack.mcmeta"))
                readMcmeta(
                    JsonParser().parse(zf.getInputStream(mcmetaEntry).reader()).asJsonObject,
                    File(file, "pack.mcmeta")
                )
            }
            return r
        }
    }
}
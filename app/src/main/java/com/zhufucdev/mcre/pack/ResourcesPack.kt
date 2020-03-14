package com.zhufucdev.mcre.pack

import android.graphics.Bitmap
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.pack.ResourcesPack.Type.*
import com.zhufucdev.mcre.utility.DataUnit
import java.io.File
import java.io.Reader
import java.math.BigInteger
import java.util.zip.ZipInputStream

abstract class ResourcesPack(val root: File) {
    var icon: Bitmap? = null
    lateinit var header: ManifestHeader
    private var mSize = DataUnit.from(BigInteger.valueOf(-1))
    fun calcSize(): DataUnit = Env.formatedSize(root).apply { mSize = this }
    val size get() = if (mSize.byte == BigInteger.valueOf(-1)) calcSize() else mSize

    enum class Type {
        BedrockEdition, JavaEdition, Unknown, Useless
    }

    open class Version(private val v: List<Int>) {
        val raw: List<Int> get() = v
        override fun toString(): String = buildString {
            v.forEach {
                append("$it.")
            }
            removeSuffix(".")
        }

        companion object {
            fun parse(json: JsonArray): Version {
                val r = ArrayList<Int>()
                json.forEach {
                    r.add(it.asInt)
                }
                return Version(r)
            }
        }
    }

    open class ManifestHeader(
        val name: String? = null,
        val description: String,
        val version: Version
    )

    companion object {
        fun recognize(file: File): Type {
            fun validateMcmeta(reader: Reader) = try {
                JsonParser().parse(reader)
                    .let { json -> json.isJsonObject && json.asJsonObject.has("pack") }
            } catch (e: Exception) {
                false
            }

            val java =
                if (file.isDirectory) File(file, "pack.mcmeta").let { it.exists() && validateMcmeta(it.reader()) }
                else {
                    val zis = ZipInputStream(file.inputStream())
                    var entry = zis.nextEntry
                    var result = false
                    while (entry != null) {
                        if (!entry.isDirectory && entry.name == "pack.mcmeta") {
                            result = validateMcmeta(zis.reader())
                            break
                        }
                        entry = zis.nextEntry
                    }
                    zis.close()

                    result
                }
            val bedrock = File(file, "manifest.json").let {
                it.exists() &&
                        try {
                            JsonParser().parse(it.reader())
                                .let { json -> json.isJsonObject && json.asJsonObject.has("header") }
                        } catch (e: Exception) {
                            false
                        }
            }
            return when {
                java && !bedrock -> JavaEdition
                !java && bedrock -> BedrockEdition
                !java && !bedrock -> Useless
                else -> Unknown
            }
        }

        fun from(file: File): PackWrapper =
            when (recognize(file)) {
                BedrockEdition -> PackWrapper(BedrockEdition, file, BedrockPack.from(file))
                JavaEdition -> PackWrapper(JavaEdition, file, JavaPack.from(file))
                Unknown -> TODO()
                Useless -> PackWrapper(Useless, file, null)
            }

    }
}
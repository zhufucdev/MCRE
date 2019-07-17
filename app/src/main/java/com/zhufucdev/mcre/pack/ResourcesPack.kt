package com.zhufucdev.mcre.pack

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.pack.ResourcesPack.Type.*
import com.zhufucdev.mcre.utility.DataUnit
import java.io.File
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList

abstract class ResourcesPack internal constructor(val root: File) {
    abstract val icon: File
    lateinit var header: ManifestHeader
    private var mSize = DataUnit.from(BigInteger.valueOf(-1))
    fun calcSize(): DataUnit = Env.formatedSize(root).apply { mSize = this }
    val size get() = if (mSize.byte == BigInteger.valueOf(-1)) calcSize() else mSize

    enum class Type {
        Bedrock, JavaVersion, Unknown, Useless
    }

    class Version(val v: List<Int>) {
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

    class ManifestHeader(
        val name: String? = null,
        val uuid: UUID,
        val description: String,
        val version: Version,
        val minVersion: Version? = null
    )

    companion object {
        fun recognize(file: File): Type {
            val java = File(file, "pack.mcmeta").let {
                it.exists()
                        && try {
                    JsonParser().parse(it.reader()).let { json -> json.isJsonObject && json.asJsonObject.has("pack") }
                } catch (e: Exception) {
                    false
                }
            }
            val bedrock = File(file, "manifest.json").let {
                it.exists()
                        && try {
                    JsonParser().parse(it.reader()).let { json -> json.isJsonObject && json.asJsonObject.has("header") }
                } catch (e: Exception) {
                    false
                }
            }
            return when {
                java && !bedrock -> JavaVersion
                !java && bedrock -> Bedrock
                !java && !bedrock -> Useless
                else -> Unknown
            }
        }

        fun from(file: File): PackWrapper {
            if (!file.exists() || !file.isDirectory) throw IllegalArgumentException("File must exist and be a directory.")
            return when (recognize(file)) {
                Bedrock -> PackWrapper(Bedrock, file, BedrockPack.from(file))
                JavaVersion -> TODO()
                Unknown -> TODO()
                Useless -> PackWrapper(Useless, file, null)
            }
        }
    }
}
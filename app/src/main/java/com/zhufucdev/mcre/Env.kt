package com.zhufucdev.mcre

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.util.SparseArray
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.zhufucdev.mcre.activity.BaseActivity
import com.zhufucdev.mcre.activity.MainActivity
import com.zhufucdev.mcre.pack.PackWrapper
import com.zhufucdev.mcre.pack.ResourcesPack
import com.zhufucdev.mcre.utility.DataUnit
import java.io.File
import java.lang.IllegalArgumentException
import java.math.BigInteger
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

object Env {
    var threadPool = Executors.newCachedThreadPool()!!

    val packs = ArrayList<PackWrapper>()

    val isPermissionsAllGranted get() = ContextCompat.checkSelfPermission(presentActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    object Packs {
        fun remove(file: File) {
            remove(listOf(file))
        }

        fun remove(files: List<File>) {
            if (files.isEmpty()) throw IllegalArgumentException("files must not be empty.")

            fun refreshList() {
                if (presentActivity is MainActivity) (presentActivity as MainActivity).mainFragment.handler.sendEmptyMessage(if (packs.isEmpty()) 0 else 1)
            }

            val todo = TODO.new(Processes.PackDeleting, R.string.info_cleaning_up) {
                files.forEach { it.deleteRecursively() }
                clear()
            }
            todo.obj = files
            val recover = TreeMap<File, Int>()
            files.forEach {
                val pos = packs.indexOfFirst { pack -> pack.file == it }
                recover[it] = pos
                packs.removeAt(pos)
            }

            presentActivity.showSnackBar {
                Snackbar.make(
                    it,
                    it.context.getString(
                        R.string.info_deleted,
                        buildString {
                            if (files.size > 1) {
                                for (i in 0 until files.size - 1) {
                                    append(files[i].nameWithoutExtension + ',')
                                }
                                append("and ${files.last().nameWithoutExtension}")
                            } else {
                                append(files.first().nameWithoutExtension)
                            }
                        }
                    ),
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.action_undo) {
                    recover.forEach { entry ->
                        packs.add(entry.value, ResourcesPack.from(entry.key))
                    }
                    todo.clear()
                    refreshList()
                }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                        if (event != DISMISS_EVENT_ACTION && !todo.inProcess) {
                            threadPool.execute {
                                todo.doIt()
                            }
                        }
                    }
                })
            }
            refreshList()
        }
    }

    object TODO {
        private val toDoList = ArrayList<ToDo>()
        private val listeners = SparseArray<(Int) -> Unit>()
        fun new(process: Processes, stringRes: Int, doIt: ToDo.() -> Unit) =
            ToDo(process, toDoList.maxBy { it.id }?.id ?: 0, stringRes, doIt).apply { toDoList.add(0, this) }

        val isEmpty get() = toDoList.isEmpty()

        fun addOnDoneListener(l: (Int) -> Unit): Int {
            val id = listeners.size() + 1
            listeners.put(id, l)
            return id
        }

        fun removeOnDoneListener(id: Int) = listeners.remove(id)

        fun forEach(l: (ToDo) -> Unit) = toDoList.forEach(l)

        fun first() = toDoList.firstOrNull()
        fun firstProcessing() = toDoList.firstOrNull { it.inProcess } ?: toDoList.first()

        fun any(l: (ToDo) -> Boolean) = toDoList.any(l)

        class ToDo(val process: Processes, val id: Int, val strRes: Int, private val doIt: ToDo.() -> Unit) {
            val extra = JsonObject()
            var obj: Any? = null
            var inProcess = false
            fun doIt() {
                if (!inProcess) {
                    inProcess = true
                    doIt(this)
                }
            }

            fun clear() {
                toDoList.remove(this)
                listeners.forEach { key, value -> value(key) }
            }

            override fun equals(other: Any?): Boolean = other is ToDo && other.id == this.id
            override fun hashCode(): Int {
                var result = process.hashCode()
                result = 31 * result + id
                result = 31 * result + strRes
                return result
            }
        }

    }

    var packsRoot = File(Environment.getExternalStorageDirectory(), "games/com.mojang/resource_packs")

    lateinit var presentActivity: BaseActivity

    /**
     * Utility functions
     */
    fun formatedSize(file: File) = DataUnit.from(fileSize(file))

    fun fileSize(file: File): BigInteger {
        return if (file.exists()) {
            var r = BigInteger.valueOf(0)
            if (file.isFile) {
                r = r.plus(file.length().toBigInteger())
            } else {
                file.listFiles().forEach {
                    r = r.plus(fileSize(it))
                }
            }
            r
        } else {
            0.toBigInteger()
        }
    }
}
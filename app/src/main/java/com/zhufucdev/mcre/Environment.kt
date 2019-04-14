package com.zhufucdev.mcre

import android.os.Environment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.zhufucdev.mcre.pack.PackWrapper
import com.zhufucdev.mcre.utility.DataUnit
import com.zhufucdev.mcre.pack.ResourcesPack
import java.io.File
import java.math.BigInteger
import java.util.concurrent.Executors

object Environment {
    val threadPool = Executors.newCachedThreadPool()!!

    val packs = ArrayList<PackWrapper>()

    object Packs {
        val toBeRemoved = ArrayList<Pair<File, TODO.ToDo>>()
        fun remove(file: File) {
            fun refreshList() {
                if (presentActivity is MainActivity) (presentActivity as MainActivity).handler.sendEmptyMessage(1)
            }

            presentActivity.showSnackBar {
                Snackbar.make(
                    it,
                    it.context.getString(R.string.info_deleted, file.nameWithoutExtension),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.action_undo) {
                    threadPool.execute {
                        toBeRemoved.forEach { file ->
                            packs.add(ResourcesPack.from(file.first))
                            file.second.done()
                        }
                        refreshList()
                        toBeRemoved.clear()
                    }
                }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                        if (event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION) {
                            threadPool.execute {
                                toBeRemoved.forEach { file ->
                                    file.first.deleteRecursively()
                                    file.second.done()
                                }
                                toBeRemoved.clear()
                            }
                        }
                    }
                })
            }
            toBeRemoved.add(file to TODO.new(Processes.PackDeleting, R.string.info_cleaning_up))
            packs.removeAll { it.file == file }
            refreshList()
        }
    }

    object TODO {
        private val toDoList = ArrayList<ToDo>()
        private val listeners = HashMap<Int, (Int) -> Unit>()
        fun new(process: Processes, stringRes: Int) =
            ToDo(process, toDoList.maxBy { it.id }?.id ?: 0, stringRes).apply { toDoList.add(0, this) }

        val isEmpty get() = toDoList.isEmpty()

        fun first() = toDoList.firstOrNull()
        fun addOnDoneListener(l: (Int) -> Unit): Int {
            val id = listeners.maxBy { it.key }?.key ?: 0
            listeners[id] = l
            return id
        }

        fun removeOnDoneListener(id: Int) = listeners.remove(id)

        class ToDo(val process: Processes, val id: Int, val strRes: Int) {
            fun done() {
                toDoList.remove(this)
                TODO.listeners.forEach { it.value(id) }
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
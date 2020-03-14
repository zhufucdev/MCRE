package com.zhufucdev.mcre.utility

import android.util.Log
import com.zhufucdev.mcre.Processes

object Logger {
    enum class Type {
        Info, Warn, Error
    }

    class LogR(val type: Type, val process: Processes, text: String, e: Exception? = null)

    val logs = ArrayList<LogR>()

    fun info(process: Processes, t: Any) {
        Log.i(process.name, t.toString())
        logs.add(LogR(Type.Info, process, t.toString()))
    }

    fun warn(process: Processes, t: Any) {
        Log.w(process.name, t.toString())
        logs.add(LogR(Type.Warn, process, t.toString()))
    }

    fun error(process: Processes,t: Any, e: Exception) {
        Log.e(process.name, t.toString(), e)
        logs.add(LogR(Type.Error, process, t.toString(), e))
    }

    fun debug(msg: String) {
        Log.d(Processes.Debug.name, msg)
    }
}
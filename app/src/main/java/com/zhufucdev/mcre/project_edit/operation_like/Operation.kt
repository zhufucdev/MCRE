package com.zhufucdev.mcre.project_edit.operation_like

import kotlin.reflect.KMutableProperty0

class Operation<T>(private val value: KMutableProperty0<T>, val from: T, val to: T): UndoRedo {
    override fun redo() {
        value.set(to)
        mOnRedoListener?.invoke()
    }

    override fun undo() {
        value.set(from)
        mOnUndoListener?.invoke()
    }

    private var mOnUndoListener: (() -> Unit)? = null
    fun setOnUndoneListener(l: () -> Unit): Operation<T> {
        mOnUndoListener = l
        return this
    }
    private var mOnRedoListener: (() -> Unit)? = null
    fun setOnRedoneListener(l: () -> Unit): Operation<T> {
        mOnRedoListener = l
        return this
    }

    override fun equals(other: Any?): Boolean = other is Operation<*>
            && other.from == from && other.to == to

    override fun hashCode(): Int {
        var r = from.hashCode()
        r += to.hashCode() * 31
        r += value.get().hashCode() * 31
        return r
    }
}
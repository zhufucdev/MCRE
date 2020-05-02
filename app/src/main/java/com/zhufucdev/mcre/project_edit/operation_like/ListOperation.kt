package com.zhufucdev.mcre.project_edit.operation_like

import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.exception.OperationPerformException
import com.zhufucdev.mcre.project_edit.Name
import com.zhufucdev.mcre.utility.alert
import com.zhufucdev.mcre.utility.nameify

class ListOperation<T>(private val list: MutableList<T>, private val item: T, private val remove: Boolean = false) :
    UndoRedo {
    private var lastIndex: Int = -1
    private var mOnUndoneListener: (() -> Unit)? = null
    fun setOnUndoneListener(l: () -> Unit) {
        mOnUndoneListener = l
    }
    private var mOnRedoneListener: (() -> Unit)? = null
    fun setOnRedoneListener(l: () -> Unit) {
        mOnRedoneListener = l
    }
    override fun undo() {
        operate(remove)
        mOnUndoneListener?.invoke()
    }

    override fun redo() {
        operate(!remove)
        mOnRedoneListener?.invoke()
    }

    private fun operate(positive: Boolean) {
        if (positive) {
            if (lastIndex == -1) {
                alert(OperationPerformException(this))
                return
            }
            list.add(lastIndex, item)
        } else {
            val index = list.indexOf(item)
            if (index == -1) {
                alert(OperationPerformException(this))
                return
            }
            list.removeAt(index)
            lastIndex = index
        }
    }

    override val name: Name
        get() = R.string.operation_list_modifying.nameify()
}
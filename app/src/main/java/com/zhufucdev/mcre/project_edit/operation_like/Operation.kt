package com.zhufucdev.mcre.project_edit.operation_like

import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.exception.ElementNotFoundException
import com.zhufucdev.mcre.project_edit.Name
import com.zhufucdev.mcre.project_edit.element.BaseElement
import com.zhufucdev.mcre.project_edit.work.ProjectPagerAdapter
import com.zhufucdev.mcre.utility.nameify
import kotlin.reflect.KMutableProperty0

class Operation<T>(
    private val value: KMutableProperty0<T>, val from: T, val to: T,
    internal var performer: ProjectPagerAdapter.Tab? = null
) : UndoRedo {
    private fun findSuitablePerformer(): ProjectPagerAdapter.Tab? {
        val performer = performer
        return when {
            performer == null -> {
                null
            }
            performer.isClosed -> {
                performer.clone()
            }
            else -> {
                performer
            }
        }

    }

    override fun redo() {
        value.set(to)
        operate(mOnRedoListener)
    }

    override fun undo() {
        value.set(from)
        operate(mOnUndoListener)
    }

    private fun operate(listener: ((ProjectPagerAdapter.Tab?) -> Unit)?) {
        val p = findSuitablePerformer()
        if (p != null && !p.fragment.isAdded) {
            p.fragment.setReadyListener { listener?.invoke(p) }
            performer = p
        } else {
            listener?.invoke(p)
        }
        p?.head()
    }

    private var mOnUndoListener: ((ProjectPagerAdapter.Tab?) -> Unit)? = null
    fun setOnUndoneListener(l: (ProjectPagerAdapter.Tab?) -> Unit): Operation<T> {
        mOnUndoListener = l
        return this
    }

    private var mOnRedoListener: ((ProjectPagerAdapter.Tab?) -> Unit)? = null
    fun setOnRedoneListener(l: (ProjectPagerAdapter.Tab?) -> Unit): Operation<T> {
        mOnRedoListener = l
        return this
    }

    override val name: Name
        get() = R.string.operation_varible_changing.nameify()

    override fun equals(other: Any?): Boolean = other is Operation<*>
            && other.from == from && other.to == to

    override fun hashCode(): Int {
        var r = from.hashCode()
        r += to.hashCode() * 31
        r += value.get().hashCode() * 31
        return r
    }
}
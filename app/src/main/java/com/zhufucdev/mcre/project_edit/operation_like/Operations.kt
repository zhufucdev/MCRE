package com.zhufucdev.mcre.project_edit.operation_like

import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.Name
import com.zhufucdev.mcre.project_edit.work.ProjectPagerAdapter
import com.zhufucdev.mcre.utility.combineNames
import com.zhufucdev.mcre.utility.nameify

class Operations : ArrayList<Operation<*>>(), UndoRedo {
    private var first: ProjectPagerAdapter.Tab? = null
    private var last: ProjectPagerAdapter.Tab? = null
    override fun add(element: Operation<*>): Boolean {
        if (size == 0) first = element.performer
        else last = element.performer
        return super.add(element)
    }

    override fun undo() {
        forEach {
            it.performer = null
            it.undo()
        }
        first?.head()
    }

    override fun redo() {
        forEach {
            it.performer = null
            it.redo()
        }
        last?.head()
    }

    override val name: Name
        get() {
            val list = mutableListOf<Name>()
            forEachIndexed { index, operation ->
                list.add(
                    if (index < lastIndex) {
                        combineNames(operation.name, ", ".nameify())
                    } else {
                        operation.name
                    }
                )
            }
            return Name(list)
        }
}
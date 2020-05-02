package com.zhufucdev.mcre.project_edit.operation_like

import com.zhufucdev.mcre.project_edit.Name

interface UndoRedo {
    fun undo()
    fun redo()
    val name: Name
}
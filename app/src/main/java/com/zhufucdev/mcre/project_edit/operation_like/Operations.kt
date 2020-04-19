package com.zhufucdev.mcre.project_edit.operation_like

class Operations: ArrayList<Operation<*>>(), UndoRedo {
    override fun undo() {
        forEach { it.undo() }
    }

    override fun redo() {
        forEach { it.redo() }
    }
}
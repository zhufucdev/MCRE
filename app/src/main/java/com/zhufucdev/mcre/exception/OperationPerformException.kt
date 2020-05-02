package com.zhufucdev.mcre.exception

import android.app.Application
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.operation_like.UndoRedo

class OperationPerformException(operation: UndoRedo): LocalizedException("Failed to perform ${operation.name.get(Env.presentActivity)}.") {
    override val messageID: Int
        get() = R.string.exception_perform_operation
    override val formatArgs: Array<Any> = arrayOf(operation.name.get(Env.presentActivity))
}
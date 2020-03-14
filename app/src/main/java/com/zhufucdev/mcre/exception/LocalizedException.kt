package com.zhufucdev.mcre.exception

import android.content.Context
import com.zhufucdev.mcre.internal.ContextStringSupplier

abstract class LocalizedException: ContextStringSupplier, Exception {
    constructor(debugMessage: String): super(debugMessage)
    constructor(): super()

    abstract val messageID: Int
    abstract val formatArgs: Array<Any>
    override fun get(context: Context): String = context.getString(messageID, *formatArgs)
}
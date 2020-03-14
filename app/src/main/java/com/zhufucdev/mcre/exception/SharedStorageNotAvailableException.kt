package com.zhufucdev.mcre.exception

import com.zhufucdev.mcre.R

class SharedStorageNotAvailableException: LocalizedException() {
    override val messageID: Int
        get() = R.string.exception_shared_storage_not_available
    override val formatArgs: Array<Any> get() = emptyArray()
}
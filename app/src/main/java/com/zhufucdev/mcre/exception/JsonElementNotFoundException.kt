package com.zhufucdev.mcre.exception

import com.zhufucdev.mcre.R

class JsonElementNotFoundException(path: String, fileName: String) : LocalizedException("$path at $fileName") {
    override val messageID: Int
        get() = R.string.exception_json_element_not_found
    override val formatArgs: Array<Any> = arrayOf(path, fileName)
}
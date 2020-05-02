package com.zhufucdev.mcre.exception

import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.element.BaseElement

class ElementNotSupportedException(private val what: BaseElement): LocalizedException() {
    override val messageID: Int
        get() = R.string.exception_element_not_supported
    override val formatArgs: Array<Any>
        get() = arrayOf(what::class.simpleName!!, what.type.name)
}
package com.zhufucdev.mcre.project_edit.element

import com.zhufucdev.mcre.R

enum class ElementType(val order: Int, val helperTitle: Int, val helperContent: Int) {
    USER(1, R.string.title_user_element, R.string.info_user_element),
    REQUIRED(0, R.string.title_required, R.string.info_required)
}
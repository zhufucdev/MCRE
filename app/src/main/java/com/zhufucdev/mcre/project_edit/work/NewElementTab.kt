package com.zhufucdev.mcre.project_edit.work

import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.fragment.NewElementFragment

class NewElementTab(fragment: NewElementFragment, parent: ProjectPagerAdapter) :
    ProjectPagerAdapter.Tab(fragment, R.string.title_new_element, parent) {
    private val id = ++count
    override fun hashCode(): Int = super.hashCode() * 31 + id

    companion object {
        private var count = 0
    }
}
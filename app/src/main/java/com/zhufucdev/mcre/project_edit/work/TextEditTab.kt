package com.zhufucdev.mcre.project_edit.work

import com.zhufucdev.mcre.project_edit.fragment.TextEditFragment

class TextEditTab(fragment: TextEditFragment, adapter: ProjectPagerAdapter) :
    ProjectPagerAdapter.Tab(fragment, fragment.itemName, adapter) {
    override fun equals(other: Any?): Boolean = other is TextEditTab
            && other.fragment == fragment
}
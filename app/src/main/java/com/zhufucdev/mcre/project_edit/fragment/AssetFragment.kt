package com.zhufucdev.mcre.project_edit.fragment

import android.view.Menu
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.EditableProject

class AssetFragment(project: EditableProject): BaseFragment(R.layout.fragment_asset, project) {
    override val fabResource: Int
        get() = R.drawable.ic_add_white

    override fun initAppbar(menu: Menu) {

    }
}
package com.zhufucdev.mcre.project_edit.fragment

import android.os.Bundle
import android.view.Menu
import android.view.View
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.activity.ProjectActivity
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.work.ProjectPagerAdapter
import com.zhufucdev.mcre.project_edit.work.TextEditTab
import kotlinx.android.synthetic.main.fragment_data.*

class DataFragment(private val pagerAdapter: ProjectPagerAdapter, project: EditableProject) : BaseFragment(R.layout.fragment_data, project) {
    private val nameEdit by lazy {
        TextEditFragment(
            getString(R.string.name_name),
            project::name,
            project
        )
    }
    private val descriptionEdit by lazy {
        TextEditFragment(
            getString(R.string.name_description),
            project::description,
            project
        )
    }
    override val fabResource: Int
        get() = R.drawable.ic_add_white

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProjectActivity.attachHelperTo(
            help_required_card,
            R.string.title_required,
            R.string.info_required
        )
        data_item_name.setOnClickListener {
            val tab = TextEditTab(nameEdit, pagerAdapter)
            pagerAdapter.addTab(tab)
            tab.head()
        }
        data_item_description.setOnClickListener {
            val tab = TextEditTab(descriptionEdit, pagerAdapter)
            pagerAdapter.addTab(tab)
            tab.head()
        }
    }

    override fun initAppbar(menu: Menu) {
    }
}
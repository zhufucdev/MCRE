package com.zhufucdev.mcre.project_edit.fragment

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.activity.ProjectActivity
import com.zhufucdev.mcre.exception.ElementNotSupportedException
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.element.ElementType
import com.zhufucdev.mcre.project_edit.element.PackName
import com.zhufucdev.mcre.project_edit.element.StringElement
import com.zhufucdev.mcre.project_edit.element.UserStringElement
import com.zhufucdev.mcre.project_edit.work.NewElementTab
import com.zhufucdev.mcre.project_edit.work.ProjectPagerAdapter
import com.zhufucdev.mcre.project_edit.work.TextEditTab
import com.zhufucdev.mcre.recycler_view.ElementCardAdapter
import com.zhufucdev.mcre.utility.alert
import kotlinx.android.synthetic.main.fragment_data.*

class DataFragment(private val pagerAdapter: ProjectPagerAdapter, project: EditableProject) :
    BaseFragment(R.layout.fragment_data, project) {
    private val nameEdit by lazy {
        TextEditFragment(
            getString(R.string.name_name),
            project.name,
            project
        )
    }
    private val descriptionEdit by lazy {
        TextEditFragment(
            getString(R.string.name_description),
            project.description,
            project
        )
    }
    private val adapter by lazy {
        ElementCardAdapter(project)
    }
    override val fabResource: Int
        get() = R.drawable.ic_add_white

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // UI
        // => Adapter for element cards
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(context!!)
        // => Listener
        adapter.setItemClickListener(ElementType.REQUIRED) {
            val fragment = when(it) {
                is PackName -> nameEdit
                else -> descriptionEdit
            }
            val tab = TextEditTab(fragment, pagerAdapter)
            pagerAdapter.addTab(tab)
            tab.head()
        }
        adapter.setItemClickListener(ElementType.USER) { e ->
            val tab = pagerAdapter.tabs.firstOrNull { it.fragment.editing?.id == e.id }
            if (tab != null) {
                tab.head()
            } else {
                when (e) {
                    is StringElement -> {
                        val fragment = TextEditFragment(e.title.get(requireContext()), e, project)
                        val tab2 = TextEditTab(fragment, pagerAdapter)
                        pagerAdapter.addTab(tab2)
                        tab2.head()
                    }
                    else -> alert(ElementNotSupportedException(e))
                }
            }
        }
    }

    override val fabListener: ((View) -> Unit)? = {
        val fragment = NewElementFragment(project, adapter::refresh)
        val tab = NewElementTab(fragment, pagerAdapter)
        pagerAdapter.addTab(tab)
        tab.head()
    }

    override fun initAppbar(menu: Menu) {
    }
}
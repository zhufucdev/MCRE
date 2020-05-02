package com.zhufucdev.mcre.project_edit.fragment

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.zhufucdev.mcre.Processes
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.element.BaseElement
import com.zhufucdev.mcre.project_edit.work.ProjectPagerAdapter
import com.zhufucdev.mcre.utility.Logger
import com.zhufucdev.mcre.utility.animateMenuChange
import com.zhufucdev.mcre.utility.hideThen
import com.zhufucdev.mcre.utility.isUp

abstract class BaseFragment(contentLayoutID: Int, val project: EditableProject, open val editing: BaseElement? = null)
    : Fragment(contentLayoutID) {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        callFab()
        callAppbar()
    }

    override fun onResume() {
        super.onResume()
        callFab()
        callAppbar()
    }

    override fun onPause() {
        super.onPause()
        showAppbar = appbar?.isUp == true
    }

    private fun callFab() {
        val fab = fab ?: return
        if (fab.tag != fabResource) {
            fab.tag = fabResource
            fab.hideThen {
                setImageResource(fabResource)
                show()
            }
        }
        if (fabListener != null)
            fab.setOnClickListener(fabListener!!)
        else
            fab.setOnClickListener(null)
    }

    private fun callAppbar() {
        val bar = appbar
        if (bar != null) {
            bar.menu.clear()
            initAppbar(bar.menu)
            if (showAppbar && !bar.isUp)
                bar.performShow()
            else if (!showAppbar && bar.isUp)
                bar.performHide()
        } else {
            Logger.warn(
                Processes.UI,
                "Failed to initialize Bottom Appbar for ${this::class.simpleName}: Null reference."
            )
        }
    }

    abstract val fabResource: Int
    open val fabListener: ((View) -> Unit)? get() = null
    val fab get() = activity?.findViewById<FloatingActionButton>(R.id.fab)
    private val appbar by lazy { activity?.findViewById<BottomAppBar>(R.id.bottom_app_bar) }
    private var showAppbar = true
    abstract fun initAppbar(menu: Menu)

    lateinit var tab: ProjectPagerAdapter.Tab
}
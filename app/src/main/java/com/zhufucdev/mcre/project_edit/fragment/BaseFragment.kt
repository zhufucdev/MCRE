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
import com.zhufucdev.mcre.utility.Logger
import com.zhufucdev.mcre.utility.animateMenuChange
import com.zhufucdev.mcre.utility.hideThen

abstract class BaseFragment(contentLayoutID: Int, val project: EditableProject): Fragment(contentLayoutID) {
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
        if (appbar != null) {
            appbar!!.menu.clear()
            initAppbar(appbar!!.menu)
        } else {
            Logger.warn(Processes.UI, "Failed to initialize Bottom Appbar for ${this::class.simpleName}: Null reference.")
        }
    }

    abstract val fabResource: Int
    open val fabListener: ((View) -> Unit)? get() = null
    private val fab get() = activity?.findViewById<FloatingActionButton>(R.id.fab)
    private val appbar by lazy { activity?.findViewById<BottomAppBar>(R.id.bottom_app_bar) }
    abstract fun initAppbar(menu: Menu)
}
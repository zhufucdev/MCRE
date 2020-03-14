package com.zhufucdev.mcre.activity

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.exception.ElementNotFoundException
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.work.ProjectPagerAdapter
import com.zhufucdev.mcre.recycler_view.FileAdapter
import com.zhufucdev.mcre.utility.alert
import com.zhufucdev.mcre.utility.nameify
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.base_layout.*
import kotlinx.android.synthetic.main.base_layout.fab
import kotlinx.android.synthetic.main.content_project.*

class ProjectActivity : BaseActivity(), TabLayout.OnTabSelectedListener {
    private lateinit var pagerAdapter: ProjectPagerAdapter

    lateinit var project: EditableProject
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_layout)
        LayoutInflater.from(this).inflate(R.layout.activity_project, container)
        setSupportActionBar(bottom_app_bar)
        // TODO: Handle existing project
        project = EditableProject()

        pagerAdapter = ProjectPagerAdapter(
            supportFragmentManager,
            tab_layout to view_pager,
            this
        )
        tab_layout.addOnTabSelectedListener(this)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save -> {
                    saveProject()
                    true
                }
                else -> false
            }
        }
    }

    fun saveProject() {
        if (!::project.isInitialized) {
            alert(ElementNotFoundException(R.string.name_project.nameify()))
            return
        }
        val file = project.file
        if (file != null) {
            project.save(file)
            return
        }
        val recycler = RecyclerView(this)
        val adapter = FileAdapter(Environment.getExternalStorageDirectory(), true, R.string.action_new_folder)
        adapter.setOnAltButtonClickListener {
            //TODO
        }
        recycler.apply {
            this.adapter = adapter
            layoutManager = GridLayoutManager(this@ProjectActivity, 2)
        }
        AlertDialog.Builder(this)
            .setView(recycler)
            .show()
    }

    override fun showSnackBar(builder: (View) -> Snackbar) {
        builder(findViewById(android.R.id.content)).apply {
            view.translationY += fab.top + fab.translationY - project_root.measuredHeight
            show()
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        val menu = PopupMenu(this@ProjectActivity, tab.view)
        menu.menuInflater.inflate(R.menu.menu_project_tab, menu.menu)
        val mTab = pagerAdapter.getTab(tab.position)
        if (!mTab.closeable) {
            menu.menu.removeItem(R.id.tab_close)
            menu.menu.removeItem(R.id.tab_pin)
        }
        menu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.tab_pin -> {
                    mTab.apply {
                        isPinned = !isPinned
                    }
                    true
                }
                R.id.tab_close -> {
                    mTab.apply {
                        if (closeable) close()
                    }
                    true
                }
                R.id.tab_close_all -> {
                    pagerAdapter.beginUpdate()
                    pagerAdapter.forEach {
                        if (it.closeable) it.close()
                    }
                    pagerAdapter.endUpdate()
                    true
                }
                R.id.tab_close_others -> {
                    pagerAdapter.beginUpdate()
                    pagerAdapter.forEach {
                        if (tab.position != it.position && it.closeable) {
                            it.close()
                        }
                    }
                    pagerAdapter.endUpdate()
                    true
                }
                R.id.tab_close_unpinned -> {
                    pagerAdapter.beginUpdate()
                    pagerAdapter.forEach {
                        if (!it.isPinned)
                            it.close()
                    }
                    pagerAdapter.endUpdate()
                    true
                }
                else -> false
            }
        }
        menu.show()
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> false
        }
    }

    companion object {
        fun attachHelperTo(view: View, title: Int, helper: Int) {
            view.setOnClickListener {
                AlertDialog.Builder(view.context)
                    .setTitle(title)
                    .setMessage(helper)
                    .setPositiveButton(R.string.action_close, null)
                    .show()
            }
        }
    }
}

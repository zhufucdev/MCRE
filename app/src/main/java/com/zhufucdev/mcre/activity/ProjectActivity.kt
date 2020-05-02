package com.zhufucdev.mcre.activity

import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.exception.ElementNotFoundException
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.work.ProjectPagerAdapter
import com.zhufucdev.mcre.project_edit.work.ReplaceFileDialog
import com.zhufucdev.mcre.recycler_view.FileAdapter
import com.zhufucdev.mcre.utility.*
import kotlinx.android.synthetic.main.activity_project.*
import kotlinx.android.synthetic.main.base_layout.*
import kotlinx.android.synthetic.main.base_layout.fab
import kotlinx.android.synthetic.main.content_project.*
import kotlinx.android.synthetic.main.shared_new_folder.view.*
import java.io.File
import java.util.*

class ProjectActivity : BaseActivity(), TabLayout.OnTabSelectedListener {
    private lateinit var pagerAdapter: ProjectPagerAdapter

    lateinit var project: EditableProject
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UI
        setContentView(R.layout.base_layout)
        LayoutInflater.from(this).inflate(R.layout.activity_project, container)
        setSupportActionBar(bottom_app_bar)
        // Data
        project =
            intent.getStringExtra("open")?.let { EditableProject(File(it)) }
                ?: EditableProject()
        projectActivities[project] = this
        // UI
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

    private fun saveProject() {
        if (!::project.isInitialized) {
            alert(ElementNotFoundException(R.string.name_project.nameify()))
            return
        }
        val file = project.file
        if (file != null) {
            project.save(file)
            return
        }
        fun showResult(where: File) {
            showSnackBar {
                Snackbar.make(
                    it,
                    getString(R.string.info_saved_to, project.name.value, where.absolutePath),
                    Snackbar.LENGTH_LONG
                )
            }
        }

        val container = FrameLayout(this)
        val recycler = RecyclerView(this)
        val input = layoutInflater.inflate(R.layout.shared_new_folder, container, false)
        var x = 0F
        var y = 0F
        var recyclerSupposedHeight = 0
        fun showInput(fromX: Float, fromY: Float) {
            recycler.measure()
            x = fromX + recycler.measuredWidth / 2
            y = fromY
            input.visibility = View.VISIBLE
            ViewAnimationUtils.createCircularReveal(
                input,
                x.toInt(),
                y.toInt(),
                0f,
                (input.measuredHeight orBigger input.measuredWidth).toFloat()
            ).apply {
                duration = 170
                doOnEnd {
                    recycler.visibility = View.INVISIBLE
                    recyclerSupposedHeight = recycler.measuredHeight
                    ObjectAnimator.ofInt(recyclerSupposedHeight, 0).apply {
                        addUpdateListener {
                            recycler.updateLayoutParams<FrameLayout.LayoutParams> {
                                height = it.animatedValue as Int
                            }
                        }
                        duration = 300
                        start()
                    }
                }
                start()
            }
        }

        fun makeMatchParent(it: View) {
            it.updateLayoutParams<FrameLayout.LayoutParams> {
                height = FrameLayout.LayoutParams.MATCH_PARENT
                width = FrameLayout.LayoutParams.MATCH_PARENT
            }
        }

        fun hideInput() {
            input.edit_text.hideSoftKeyboard()
            recycler.visibility = View.VISIBLE
            ViewAnimationUtils.createCircularReveal(
                input,
                x.toInt(),
                y.toInt(),
                (input.measuredHeight orBigger input.measuredWidth).toFloat(),
                0f
            ).apply {
                duration = 170
                doOnEnd {
                    input.visibility = View.INVISIBLE
                }
                start()
            }
            ObjectAnimator.ofInt(0, recyclerSupposedHeight).apply {
                addUpdateListener {
                    recycler.updateLayoutParams<FrameLayout.LayoutParams> {
                        height = it.animatedValue as Int
                    }
                }
                duration = 170
                doOnEnd {
                    makeMatchParent(recycler)
                }
                start()
            }
        }
        container.apply {
            addView(recycler)
            addView(input)
        }
        listOf(recycler, input).forEach { makeMatchParent(it) }
        input.visibility = View.INVISIBLE
        val adapter = FileAdapter(Environment.getExternalStorageDirectory(), true, R.string.action_new_folder)
        adapter.setOnAltButtonClickListener { x1, y1 ->
            showInput(x1, y1)
        }
        recycler.apply {
            this.adapter = adapter
            layoutManager = GridLayoutManager(this@ProjectActivity, 2)
        }
        var makeDir: File? = null
        val onConfirm: (DialogInterface) -> Unit = { dialog ->
            if (input.isVisible) {
                // If making new dir
                val name = input.edit_text.text.toString()
                if (name.isEmpty()) {
                    // Check empty name
                    input.edit_text.error = getString(R.string.info_empty_name)
                } else {
                    val newFile = File(adapter.root, name)
                    if (newFile.exists()) {
                        // Check existing file
                        input.edit_text.error = getString(R.string.info_file_exists)
                    } else {
                        // Mkdir
                        newFile.mkdir()
                        makeDir = newFile
                        // Redirect to file explorer
                        adapter.root = newFile
                        adapter.refresh()
                        hideInput()
                    }
                }
            } else {
                if (makeDir != null) {
                    // If a new dir was previously created, use it
                    project.save(makeDir)
                    showResult(makeDir!!)
                } else {
                    // If not, create a new dir using the project's name
                    val target = File(adapter.root, project.name.value)
                    if (target.exists()) {
                        // Check existing file
                        ReplaceFileDialog(
                            this,
                            replacer = ReplaceFileDialog.FileInfo(project.name.value, true, Date(), null),
                            replace = ReplaceFileDialog.FileInfo(target)
                        ).apply {
                            setOnReplaceClickListener {
                                // To replace, first delete the existing file
                                target.deleteRecursively()
                                project.save(target)
                                showResult(target)
                            }
                            setOnKeepBothClickListener {
                                val new = File(adapter.root, project.name.value + " (${getString(R.string.name_copy)})")
                                project.save(new)
                                showResult(new)
                            }
                            show()
                        }
                    } else {
                        project.save(target)
                        showResult(target)
                    }
                }
                dialog.dismiss()
            }
        }
        val onCancel: (DialogInterface) -> Unit = { dialog ->
            if (input.isVisible) hideInput()
            else dialog.dismiss()
        }
        val cancelAutoDismissal: (DialogInterface) -> Unit = { dialog ->
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                onConfirm(dialog)
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                onCancel(dialog)
            }
        }
        with(AlertDialog.Builder(this)) {
            setView(container)
            setPositiveButton(R.string.action_ok, null)
            setNegativeButton(R.string.action_cancel, null)
            create()
        }.apply {
            setOnShowListener(cancelAutoDismissal)
            show()
        }
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

    override fun onBackPressed() {

        super.onBackPressed()
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

        private val projectActivities = hashMapOf<EditableProject, ProjectActivity>()
        operator fun get(project: EditableProject) = projectActivities[project]
    }
}

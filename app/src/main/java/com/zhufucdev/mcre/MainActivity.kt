package com.zhufucdev.mcre

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import com.google.android.material.snackbar.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhufucdev.mcre.recycler_view.PacksAdapter
import com.zhufucdev.mcre.utility.AnimationEndListener
import com.zhufucdev.mcre.utility.Logger
import com.zhufucdev.mcre.pack.ResourcesPack
import com.zhufucdev.mcre.utility.forEachAdapter
import com.zhufucdev.mcre.utility.forEachAdapterIndexed

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //UI
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            Snackbar.make(main_root, "TODO", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

        text_root_path.text = getString(R.string.root_path_located, Environment.packsRoot.absolutePath)

        main_swipe_refresh.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener {
                Environment.threadPool.execute {
                    listPacks()
                    main_swipe_refresh.isRefreshing = false
                }
            }
        }
        //Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    showRationaleText()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                }
            }
        }

        //Data
        Environment.threadPool.execute {
            listPacks()
        }
    }

    private lateinit var topSnackbar: Snackbar
    override fun showSnackBar(builder: (CoordinatorLayout) -> Snackbar) {
        builder(main_root).apply { topSnackbar = this }.show()
    }

    private lateinit var mainAdapter: PacksAdapter
    private fun setOnCardClickListener(){
        mainAdapter.setOnCardClickListener { index ->
            main_recycler.forEachAdapterIndexed<PacksAdapter.PackHolder> { t, i ->
                if (i != index && t.isActionGroupShown){
                    t.hideActions(t.actionGroup.width/2,t.actionGroup.height/2)
                }
            }
        }
    }
    val handler = Handler {
        fun hideProgressBar() {
            progress_searching.startAnimation(AlphaAnimation(1f, 0f).apply {
                duration = 120
                setAnimationListener(AnimationEndListener {
                    progress_searching.visibility = View.GONE
                    btn_searched.apply {
                        visibility = View.VISIBLE
                        startAnimation(AlphaAnimation(0f, 1f).apply { duration = 120 })
                    }
                })
            })
        }
        when (it.what) {
            0 -> {
                // Signal of item empty
                hideProgressBar()
                text_main_warn.setText(R.string.empty_pack)
                sign_empty_pack_items.isVisible = true
                true
            }
            1 -> {
                // Signal of loading complete with something found
                main_recycler.apply {
                    if (!::mainAdapter.isInitialized) {
                        mainAdapter = PacksAdapter()
                        adapter = mainAdapter
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        setHasFixedSize(true)
                        setOnCardClickListener()
                    } else {
                        mainAdapter.notifyDataSetChanged()
                    }
                }
                btn_retry.visibility = View.GONE
                hideProgressBar()
                if (sign_empty_pack_items.visibility == View.VISIBLE) {
                    sign_empty_pack_items.startAnimation(AlphaAnimation(1f, 0f).apply {
                        duration = 120
                        setAnimationListener(AnimationEndListener {
                            sign_empty_pack_items.visibility = View.GONE
                        })
                    })
                }

                main_recycler.startAnimation(
                    AlphaAnimation(0f, 1f).apply {
                        duration = 240
                    }
                )
                true
            }
            2 -> {
                // Signal of loading begin
                progress_searching.visibility = View.VISIBLE
                btn_searched.visibility = View.GONE
                true
            }
            else -> false
        }
    }

    private fun listPacks() {
        handler.sendEmptyMessage(2)

        Environment.packs.clear()
        Environment.packsRoot.listFiles()?.forEach {
            if (!it.isDirectory || Environment.Packs.toBeRemoved.any { file -> file.first == it }) return@forEach
            Logger.info(Processes.PackSearch, "loading ${it.name}.")
            try {
                Environment.packs.add(
                    ResourcesPack.from(it)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            ?: Logger.warn(Processes.PackSearch, "ignored cause pack root doesn't exist.")
        // If loaded successfully but no packs found.
        if (Environment.packs.isEmpty()) {
            handler.sendEmptyMessage(0)
        } else {
            // If something needs to be shown
            handler.sendEmptyMessage(1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private var isExitDialogShown = false
    override fun onBackPressed() {
        // Handle the action when exiting.
        // Check if there's something to do.
        if (!Environment.TODO.isEmpty && !isExitDialogShown) {
            // If yes, show a dialog to tell the user what's gotta to be done.
            val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.info_task_finishing)
                .setMessage(Environment.TODO.first()!!.strRes)
                .setCancelable(false)
                .setPositiveButton(R.string.action_cancel) { _,_ ->
                    super.onBackPressed()
                }
                .show()
            isExitDialogShown = true
            Environment.TODO.addOnDoneListener {
                runOnUiThread {
                    if (!Environment.TODO.isEmpty) {
                        dialog.setMessage(getString(Environment.TODO.first()!!.strRes))
                    } else {
                        dialog.setMessage(getString(R.string.info_thread_pool_shutting_down))
                        Environment.threadPool.shutdown()
                        dialog.dismiss()
                        super.onBackPressed()
                    }
                }
            }
        } else if (Environment.TODO.isEmpty) {
            super.onBackPressed()
        }

        if (::topSnackbar.isInitialized) topSnackbar.dismiss()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun showRationaleText() {
        main_swipe_refresh.isVisible = false
        text_main_warn.setText(R.string.permission_denied)
        btn_retry.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.first() != PackageManager.PERMISSION_GRANTED) {
                showRationaleText()
            } else {
                Environment.threadPool.execute { listPacks() }
            }
        }
    }
}

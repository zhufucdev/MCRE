package com.zhufucdev.mcre.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.FileFragment
import com.zhufucdev.mcre.MainFragment
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.recycler_view.PacksAdapter
import com.zhufucdev.mcre.utility.SwipeDirection
import com.zhufucdev.mcre.utility.forEachHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File
import java.util.concurrent.Executors

class MainActivity : BaseActivity() {

    val mainFragment = MainFragment()
    val fileFragment = FileFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Env.threadPool.isShutdown || Env.threadPool.isTerminated)
            Env.threadPool = Executors.newCachedThreadPool()
        // =>UI
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Handler().post {
            // Add Main Fragment for pack viewing.
            if (!::presentFragment.isInitialized)
                setFragment(mainFragment, true)
        }

        fab.setOnClickListener {
            if (presentFabSrc == R.drawable.ic_add_white) {
                if (presentFragment != fileFragment) {
                    setFragment(fileFragment)
                    mainFragment.turnOffSelectingMode(true)
                    main_bottom_app_bar.performShow()
                    ObjectAnimator.ofFloat(fab, "rotation", 0f, 135f).apply {
                        duration = 500
                        interpolator = OvershootInterpolator(2f)
                        start()
                    }
                } else {
                    setFragment(mainFragment, true)
                    fileFragment.removeExtendCards()
                    main_bottom_app_bar.performShow()
                    ObjectAnimator.ofFloat(fab, "rotation", 135f, 0f).apply {
                        duration = 500
                        interpolator = OvershootInterpolator(2f)
                        start()
                    }
                }
            }
        }

        text_root_path.text = getString(R.string.root_path_located, Env.packsRoot.absolutePath)

        main_bottom_app_bar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_delete -> {
                    val list = ArrayList<File>()
                    main_recycler.forEachHolder<PacksAdapter.PackHolder> { holder ->
                        if (holder.isCardSelected)
                            list.add(Env.packs[holder.adapterPosition].file)
                    }
                    Env.Packs.remove(list)
                    mainFragment.turnOffSelectingMode(true)
                    true
                }
                R.id.action_select_all -> {
                    main_recycler.forEachHolder<PacksAdapter.PackHolder> { holder -> holder.selectCard() }
                    true
                }
                R.id.action_select_inverse -> {
                    main_recycler.forEachHolder<PacksAdapter.PackHolder> { holder ->
                        if (holder.isCardSelected)
                            holder.unselectCard()
                        else
                            holder.selectCard()
                    }
                    mainFragment.notifyCardChanged()
                    true
                }
                else -> false
            }
        }
        //Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Env.isPermissionsAllGranted) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    showRationaleText()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                }
            } else {
                fileFragment.adapter.refresh()
            }
        }
    }

    override fun onSwipe(fraction: Float, direction: SwipeDirection) {
        if (presentFragment == mainFragment && direction == SwipeDirection.RIGHT) {
            fileFragment.view?.apply {
                if (fraction + 0.1f < 1f) {
                    rotation = -(0.9f - fraction) * 75f
                    alpha = fraction + 0.1f
                } else {
                    alpha = 1f
                    rotation = 0f
                }
            }
            mainFragment.view!!.apply {
                rotation = 75 * fraction
                alpha = 0.9f - fraction
            }
            fab.rotation = 45 * fraction
        } else if (presentFragment == fileFragment && direction == SwipeDirection.LEFT) {
            mainFragment.view?.apply {
                if (fraction + 0.1f < 1f) {
                    rotation = 75 * (0.9f - fraction)
                    alpha = fraction + 0.1f
                } else {
                    alpha = 1f
                    rotation = 0f
                }
            }
            fileFragment.view!!.apply {
                rotation = -75 * fraction
                alpha = 0.9f - fraction
            }
            fab.rotation = 45 * (1 - fraction)
        }
    }

    private var swipeTimeBegin = 0L
    override fun swipeBegan(direction: SwipeDirection) {
        swipeTimeBegin = System.currentTimeMillis()
        val x = fab.left + fab.width / 2f
        val y = fab.top + fab.height / 2f
        if (presentFragment == mainFragment && direction == SwipeDirection.RIGHT) {
            if (!supportFragmentManager.fragments.contains(fileFragment)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.main_root, fileFragment)
                    .runOnCommit {
                        fileFragment.view!!.apply {
                            alpha = 0f
                            rotation = -75f
                            pivotX = x
                            pivotY = y
                        }
                    }
                    .commitAllowingStateLoss()
            } else {
                fileFragment.view!!.apply {
                    alpha = 0f
                    rotation = -75f
                    pivotX = x
                    pivotY = y
                }
            }
            mainFragment.view!!.apply {
                alpha = 1f
                rotation = 0f
                pivotX = x
                pivotY = y
            }
        } else if (presentFragment == fileFragment && direction == SwipeDirection.LEFT) {
            if (!supportFragmentManager.fragments.contains(mainFragment)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.main_root, fileFragment)
                    .runOnCommit {
                        mainFragment.view!!.apply {
                            alpha = 0f
                            rotation = 75f
                            pivotX = x
                            pivotY = y
                        }
                    }
                    .commitAllowingStateLoss()
            } else {
                mainFragment.view!!.apply {
                    alpha = 0f
                    rotation = 75f
                    pivotX = x
                    pivotY = y
                }
            }
            fileFragment.view!!.apply {
                alpha = 1f
                rotation = 0f
                pivotX = x
                pivotY = y
            }
        }
    }

    override fun swipeEnd(fraction: Float, direction: SwipeDirection) {
        val pass = fraction >= 0.6f || (System.currentTimeMillis() - swipeTimeBegin <= 400 && fraction >= 0.28f)

        fun animate(view: View, to: Float) {
            ObjectAnimator.ofFloat(view, "rotation", view.rotation, to).apply {
                doOnEnd {
                    view.alpha = if (to == 0f) 1f else 0f
                }
                duration = 140
                start()
            }
        }

        fun rotateFabTo(v: Float) = ObjectAnimator.ofFloat(fab, "rotation", fab.rotation, v).apply {
            duration = 140
            start()
        }
        if (presentFragment == mainFragment) {
            if (pass) {
                presentFragment = fileFragment
                mainFragment.turnOffSelectingMode(true)
                main_bottom_app_bar.performShow()
                fileFragment.view?.apply {
                    animate(this, 0f)
                }
                animate(mainFragment.view!!, 75f)
                rotateFabTo(45f)
            } else {
                fileFragment.view?.apply {
                    animate(this, -75f)
                }
                animate(mainFragment.view!!, 0f)
                rotateFabTo(0f)
            }
        } else {
            if (pass) {
                presentFragment = mainFragment
                fileFragment.removeExtendCards()
                main_bottom_app_bar.performShow()
                mainFragment.view?.apply {
                    animate(this, 0f)
                }
                animate(fileFragment.view!!, -75f)
                rotateFabTo(0f)
            } else {
                mainFragment.view?.apply {
                    animate(this, 75f)
                }
                animate(fileFragment.view!!, 0f)
                rotateFabTo(45f)
            }
        }
        presentFragment.onResume()
    }

    lateinit var presentFragment: Fragment
    private fun setFragment(fragment: Fragment, rotationInverse: Boolean = false) {
        val x = fab.x + fab.width / 2f
        val y = fab.y + fab.height / 2f
        if (::presentFragment.isInitialized) {
            // => Animation
            // Rotation of old fragment
            val old = presentFragment
            old.view?.apply {
                pivotX = x
                pivotY = y
                val to = if (!rotationInverse) 75f else -75f
                ObjectAnimator.ofFloat(this, "rotation", rotation, to).apply {
                    addUpdateListener {
                        alpha = 1f - animatedFraction
                    }
                    duration = 300
                    interpolator = OvershootInterpolator(0.6f)
                    start()
                }
            }
        }
        fun animate() {
            if (fragment.view == null) {
                presentFragment = fragment
            } else {
                fragment.view!!.apply {
                    isVisible = true
                    pivotX = x
                    pivotY = y
                    ObjectAnimator.ofFloat(this, "rotation", rotation, 0f).apply {
                        addUpdateListener {
                            alpha = animatedFraction
                        }
                        duration = 300
                        interpolator = OvershootInterpolator(0.6f)
                        doOnEnd {
                            presentFragment = fragment
                            fragment.onResume()
                        }
                        start()
                    }
                }
            }
        }
        if (supportFragmentManager.fragments.contains(fragment)) {
            animate()
        } else {
            supportFragmentManager.beginTransaction()
                .add(R.id.main_root, fragment)
                .runOnCommit {
                    fragment.view?.rotation = if (!rotationInverse) -75f else 75f
                    animate()
                }
                .commitAllowingStateLoss()
        }
        main_bottom_app_bar.translationZ = 10f
    }

    override fun showSnackBar(builder: (View) -> Snackbar) {
        builder(content).apply {
            view.translationY += fab.top + fab.translationY - main_root.measuredHeight
            show()
        }
    }

    private var presentFabSrc = R.drawable.ic_add_white
    fun switchFabTo(src: Int) {
        if (presentFabSrc != src)
            fab.hide(
                object : FloatingActionButton.OnVisibilityChangedListener() {
                    override fun onHidden(fab: FloatingActionButton) {
                        Handler().postDelayed({
                            fab.setImageResource(src)
                            fab.show()
                        }, 100)
                    }
                }
            )
        presentFabSrc = src
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
        // If selecting mode is on.
        if (mainFragment.isSelectingModeOn) {
            mainFragment.turnOffSelectingMode(true)
        } else {
            // Check if there's something to do.
            if (!Env.TODO.isEmpty && !isExitDialogShown) {
                // If yes, show a dialog to tell the user what's gotta to be clear.
                val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.info_task_finishing)
                    .setMessage(Env.TODO.firstProcessing().strRes)
                    .setCancelable(false)
                    .setPositiveButton(R.string.action_cancel) { _, _ ->
                        super.onBackPressed()
                    }
                    .show()
                isExitDialogShown = true
                Env.threadPool.execute {
                    Env.TODO.forEach { it.doIt() }
                }
                Env.TODO.addOnDoneListener {
                    runOnUiThread {
                        if (!Env.TODO.isEmpty) {
                            dialog.setMessage(getString(Env.TODO.firstProcessing().strRes))
                        } else {
                            dialog.setMessage(getString(R.string.info_thread_pool_shutting_down))
                            Env.threadPool.shutdown()
                            dialog.dismiss()
                            super.onBackPressed()
                        }
                    }
                }
            } else if (Env.TODO.isEmpty) {
                super.onBackPressed()
            }
        }
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
                Env.threadPool.execute {
                    mainFragment.listPacks()
                    fileFragment.adapter.refresh()
                }
            }
        }
    }
}

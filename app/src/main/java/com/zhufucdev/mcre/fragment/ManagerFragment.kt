package com.zhufucdev.mcre.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.Processes
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.activity.MainActivity
import com.zhufucdev.mcre.pack.PackWrapper
import com.zhufucdev.mcre.pack.ResourcesPack
import com.zhufucdev.mcre.recycler_view.PacksAdapter
import com.zhufucdev.mcre.utility.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File
import java.util.concurrent.Callable

@Suppress("UNCHECKED_CAST")
class ManagerFragment : Fragment(R.layout.fragment_main) {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (Env.isPermissionsAllGranted) listPacks()

        main_swipe_refresh.apply {
            setColorSchemeResources(R.color.colorAccent)
            setOnRefreshListener {
                if (isSelectingModeOn)
                    turnOffSelectingMode(true)
                Env.threadPool.execute {
                    listPacks()
                    main_swipe_refresh.isRefreshing = false
                }
            }
        }
    }

    private val progressSearch by lazy { activity?.progress_searching }
    private val btnSearched by lazy { activity?.btn_searched }
    private val pathSearched by lazy { activity?.text_root_path }
    val handler = Handler {
        fun hideProgressBar() {
            progressSearch?.startAnimation(AlphaAnimation(1f, 0f).apply {
                duration = 120
                setAnimationListener(AnimationEndListener {
                    progressSearch?.visibility = View.GONE
                    btnSearched?.apply {
                        visibility = View.VISIBLE
                        startAnimation(AlphaAnimation(0f, 1f).apply { duration = 120 })
                    }
                })
            })
        }

        fun showRecycler() {
            main_recycler.apply {
                isVisible = true
                setOnCardClickListener()
            }
            btn_retry.isVisible = false
            hideProgressBar()
            if (sign_empty_pack_items.isVisible) {
                ObjectAnimator.ofFloat(1f, 0f).apply {
                    duration = 120
                    addUpdateListener {
                        sign_empty_pack_items.alpha = animatedValue as Float
                        text_main_warn.alpha = animatedValue as Float
                        main_recycler.alpha = 1f - animatedValue as Float
                    }
                    doOnEnd {
                        sign_empty_pack_items.apply {
                            isVisible = false
                            alpha = 1f
                        }
                        text_main_warn.apply {
                            isVisible = false
                            alpha = 1f
                        }
                    }
                    start()
                }
            }
        }

        fun initialize() {
            main_recycler.apply {
                adapter = this@ManagerFragment.adapter
                layoutManager = LinearLayoutManager(activity)
            }
            showRecycler()
        }
        when (it.what) {
            0 -> {
                // Signal of item empty
                hideProgressBar()
                text_main_warn.apply {
                    setText(R.string.empty_pack)
                    isVisible = true
                }
                sign_empty_pack_items.isVisible = true
                main_recycler.isVisible = false
                true
            }
            1 -> {
                // Signal of loading complete with something found
                initialize()
                true
            }
            2 -> {
                // Signal of loading begin
                progressSearch?.visibility = View.VISIBLE
                btnSearched?.visibility = View.GONE
                pathSearched?.text =
                    getString(R.string.root_path_located, Env.packsRoot.absolutePath)
                true
            }
            3 -> {
                // Signal of list changed
                if (main_recycler.adapter == null)
                    initialize()
                else {
                    showRecycler()
                    with(it.obj as Pair<List<PackWrapper>, List<PackWrapper>>) {
                        DiffUtil.calculateDiff(PacksAdapter.DiffCallback(first, second)).dispatchUpdatesTo(adapter)
                    }
                }
                true
            }
            else -> false
        }
    }

    fun listPacks(oldList: List<PackWrapper>? = null) {
        handler.sendEmptyMessage(2)
        fun isToBeDeleted(it: File) = Env.TODO.any { todo -> (todo.obj as List<File>).contains(it) }
        val tasks = mutableListOf<Callable<Any>>()
        val oldPacks = oldList ?: (Env.packs.clone() as List<PackWrapper>).filter { !isToBeDeleted(it.file) }
        Env.packs.clear()
        Env.packsRoot.listFiles()?.forEach {
            if (isToBeDeleted(it))
                return@forEach
            Logger.info(Processes.PackSearch, "loading ${it.name}.")
            tasks.add(
                Callable {
                    Env.packs.add(ResourcesPack.from(it))
                }
            )
        }
            ?: Logger.warn(Processes.PackSearch, "ignored cause pack root doesn't exist.")
        // Invoke all tasks and wait for results.
        Env.threadPool.invokeAll(tasks)
        // If loaded successfully but no packs found.
        if (Env.packs.isEmpty()) {
            handler.sendEmptyMessage(0)
        } else {
            // If something needs to be shown
            handler.sendMessage(Message.obtain(handler, 3, oldPacks to Env.packs))
        }
    }

    val adapter = PacksAdapter()
    val isSelectingModeOn get() = main_recycler.any<PacksAdapter.PackHolder> { it.isCardSelected }
    private val toolbar by lazy { activity!!.findViewById<Toolbar>(R.id.toolbar) }
    private val appBar by lazy { activity!!.findViewById<BottomAppBar>(R.id.main_bottom_app_bar) }
    private val switchFabTo get() = (activity as MainActivity)::switchFabTo
    fun turnOnSelectingMode() {
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_close_white)
            setNavigationOnClickListener {
                turnOffSelectingMode(true)
            }
        }
        appBar.apply {
            animateMenuChange {
                activity!!.menuInflater.inflate(R.menu.menu_manager, menu)
            }
        }
        switchFabTo(R.drawable.ic_edit_white)
    }

    fun turnOffSelectingMode(sudden: Boolean = false) {
        toolbar.apply {
            navigationIcon = null
        }
        appBar.apply {
            animateMenuChange {
                menu.clear()
            }
        }
        if (!sudden || main_recycler.countIf<PacksAdapter.PackHolder> { it.isCardSelected } <= 1)
            switchFabTo(R.drawable.ic_add_white)
        main_recycler.forEachHolder<PacksAdapter.PackHolder> { it.unselectCard() }
    }

    fun notifyCardChanged() {
        if (!isSelectingModeOn) turnOffSelectingMode()
        else {
            val selected = main_recycler.countIf<PacksAdapter.PackHolder> { it.isCardSelected }
            if (selected == 1) {
                switchFabTo(R.drawable.ic_edit_white)
            } else {
                switchFabTo(R.drawable.ic_add_white)
            }
        }
    }

    private fun setOnCardClickListener() {
        adapter.setOnCardLongClickListener {
            val holder = main_recycler.get<PacksAdapter.PackHolder>(it)
            if (!isSelectingModeOn) {
                holder.selectCard()
                turnOnSelectingMode()
            } else {
                if (holder.isCardSelected)
                    turnOffSelectingMode(true)
                else {
                    holder.selectCard()
                    main_recycler.forEachHolderIndexed<PacksAdapter.PackHolder> { t, i ->
                        if (i != it) t.unselectCard()
                    }
                }
            }

        }
        adapter.setOnCardClickListener { index ->
            main_recycler.forEachHolderIndexed<PacksAdapter.PackHolder> { t, i ->
                if (i != index && t.isActionGroupShown) {
                    t.hideActions()
                }
            }
            !isSelectingModeOn.apply {
                if (this) {
                    main_recycler.get<PacksAdapter.PackHolder>(index).apply {
                        if (isCardSelected) {
                            unselectCard()
                        } else {
                            selectCard()
                        }
                        notifyCardChanged()
                    }
                }
            }
        }
    }
}
package com.zhufucdev.mcre

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.zhufucdev.mcre.activity.MainActivity
import com.zhufucdev.mcre.pack.ResourcesPack
import com.zhufucdev.mcre.recycler_view.PacksAdapter
import com.zhufucdev.mcre.utility.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File

class MainFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_main, container, false)

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
                main_recycler.apply {
                    isVisible = true
                    adapter = this@MainFragment.adapter
                    layoutManager = LinearLayoutManager(activity)
                    setOnCardClickListener()
                }
                btn_retry.isVisible = false
                hideProgressBar()
                if (sign_empty_pack_items.isVisible) {
                    ObjectAnimator.ofFloat(1f,0f).apply {
                        duration = 120
                        addUpdateListener {
                            sign_empty_pack_items.alpha = animatedValue as Float
                            text_main_warn.alpha = animatedValue as Float
                            main_recycler.alpha = animatedFraction
                        }
                        doOnEnd {
                            sign_empty_pack_items.apply{
                                isVisible = false
                                alpha = 1f
                            }
                            text_main_warn.apply{
                                isVisible = false
                                alpha = 1f
                            }
                        }
                        start()
                    }
                }
                true
            }
            2 -> {
                // Signal of loading begin
                progressSearch?.visibility = View.VISIBLE
                btnSearched?.visibility = View.GONE
                true
            }
            else -> false
        }
    }

    fun listPacks() {
        handler.sendEmptyMessage(2)

        Env.packs.clear()
        Env.packsRoot.listFiles()?.forEach {
            if (!it.isDirectory || Env.TODO.any { todo ->
                    @Suppress("UNCHECKED_CAST")
                    (todo.obj as List<File>).contains(it)
                }) return@forEach
            Logger.info(Processes.PackSearch, "loading ${it.name}.")
            try {
                Env.packs.add(
                    ResourcesPack.from(it)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            ?: Logger.warn(Processes.PackSearch, "ignored cause pack root doesn't exist.")
        // If loaded successfully but no packs found.
        if (Env.packs.isEmpty()) {
            handler.sendEmptyMessage(0)
        } else {
            // If something needs to be shown
            handler.sendEmptyMessage(1)
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
                activity!!.menuInflater.inflate(R.menu.menu_appbar, menu)
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
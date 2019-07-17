package com.zhufucdev.mcre

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.zhufucdev.mcre.activity.MainActivity
import com.zhufucdev.mcre.recycler_view.FileAdapter
import com.zhufucdev.mcre.utility.Logger
import com.zhufucdev.mcre.utility.forEachHolder
import com.zhufucdev.mcre.utility.measure
import kotlinx.android.synthetic.main.fragment_file.*
import kotlin.reflect.jvm.jvmName

class FileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_file, container, false)

    val adapter = FileAdapter()
    private val signEmptyPackItems by lazy { (activity as MainActivity).mainFragment.view?.findViewById<LinearLayout>(R.id.sign_empty_pack_items) }
    private val topCard by lazy { activity?.findViewById<LinearLayout>(R.id.card_top_root) }
    val handler = Handler {
        when (it.what) {
            0 -> {
                if (adapter.itemCount == 0)
                    signEmptyPackItems?.isVisible = true
                else {
                    signEmptyPackItems?.isVisible = false
                    recycler_file.isVisible = true
                }
                true
            }
            else -> false
        }
    }

    private fun extendCards(disableBack: Boolean): View =
        LayoutInflater.from(context).inflate(R.layout.recycler_file_extends, topCard, false).apply {
            adapter.giveUpperLevelListenersTo(findViewById(R.id.extend_card_back), disableBack)
            if (disableBack) {
                findViewById<AppCompatImageView>(R.id.extend_card_back_icon).imageTintList =
                    resources.getColorStateList(android.R.color.darker_gray)
            }
        }

    private var isExtendCardsAdded = false
    val nestedScrollView by lazy { recycler_file.parent as NestedScrollView }
    private fun isOverflow(scrollY: Int) =
        scrollY >= resources.getDimensionPixelSize(R.dimen.padding_big) + resources.getDimensionPixelSize(R.dimen.padding_normal) * 3

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recycler_file.apply {
            visibility = View.INVISIBLE
            adapter = this@FileFragment.adapter
            layoutManager = GridLayoutManager(context, 2)
        }
        Logger.info(Processes.Debug, recycler_file::class.jvmName)

        nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val overflow = isOverflow(scrollY)
            if (!isExtendCardsAdded && overflow && (activity as MainActivity).presentFragment === this) {
                addExtendCards()
            } else if (isExtendCardsAdded && !overflow) {
                removeExtendCards()
            }
        }
        adapter.setOnDirectoryChangedListener {
            nestedScrollView.scrollTo(0, 0)
            if (isExtendCardsAdded) {
                removeExtendCards()
            }
        }

        var hasDrawn = false
        adapter.setOnDrawnListener {
            if (!hasDrawn)
                handler.sendEmptyMessage(0)
            hasDrawn = true
        }
        adapter.setOnItemClickListener {
            recycler_file.forEachHolder<FileAdapter.FileHolder> { holder ->
                if (holder.layoutPosition != it) {
                    holder.unselectCard()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.info(Processes.Debug, "FileFragment.onStart")
        if (isOverflow(nestedScrollView.scrollY) && !isExtendCardsAdded) addExtendCards()
    }

    fun removeExtendCards() {
        val view = topCard?.findViewById<LinearLayout>(R.id.layout_extends) ?: return
        val begin = view.let {
            it.measure()
            it.measuredHeight
        }
        ObjectAnimator.ofInt(begin, 0).apply {
            addUpdateListener {
                view.updateLayoutParams<LinearLayout.LayoutParams> {
                    height = animatedValue as Int
                }
            }
            doOnEnd {
                topCard?.removeView(view)
            }
            duration = 300
            start()
        }
        isExtendCardsAdded = false
    }

    fun addExtendCards() {
        val view = extendCards(disableBack = adapter.root == Environment.getExternalStorageDirectory())
        topCard?.addView(view)
        val final = view.let {
            it.measure()
            it.measuredHeight
        }
        ObjectAnimator.ofInt(0, final).apply {
            addUpdateListener {
                view.updateLayoutParams<LinearLayout.LayoutParams> {
                    height = animatedValue as Int
                }
            }
            duration = 300
            start()
        }
        isExtendCardsAdded = true
    }
}
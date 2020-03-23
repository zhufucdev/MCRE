package com.zhufucdev.mcre.fragment

import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import androidx.core.view.forEach
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.zhufucdev.mcre.Processes
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.activity.MainActivity
import com.zhufucdev.mcre.activity.ProjectActivity
import com.zhufucdev.mcre.recycler_view.FileAdapter
import com.zhufucdev.mcre.utility.Logger
import com.zhufucdev.mcre.utility.forEachHolder
import com.zhufucdev.mcre.utility.get
import com.zhufucdev.mcre.utility.measure
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_file.*
import java.io.File
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmName

class FileFragment : Fragment(), View.OnClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_file, container, false)

    private lateinit var adapter: FileAdapter
    private val signEmptyPackItems by lazy {
        (activity as MainActivity).managerFragment.view?.findViewById<LinearLayout>(
            R.id.sign_empty_pack_items
        )
    }
    private val topCard by lazy { activity?.findViewById<LinearLayout>(R.id.card_top_root) }
    private val switchFabTo get() = (activity as MainActivity)::switchFabTo

    private fun extendCards(disableBack: Boolean): View =
        LayoutInflater.from(context).inflate(R.layout.recycler_file_extends, topCard, false).apply {
            adapter.giveUpperLevelListenersTo(findViewById(R.id.extend_card_back), disableBack)
            adapter.giveAltButtonListenerTo(findViewById(R.id.extend_card_new_project))
            if (disableBack) {
                findViewById<AppCompatImageView>(R.id.extend_card_back_icon).imageTintList =
                    resources.getColorStateList(android.R.color.darker_gray)
            }
        }

    private var isExtendCardsAdded = false
    val nestedScrollView by lazy { file_recycler_view.parent as NestedScrollView }
    private fun isOverflow(scrollY: Int) =
        scrollY >= resources.getDimensionPixelSize(R.dimen.padding_big) + resources.getDimensionPixelSize(
            R.dimen.padding_normal
        ) * 3

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = FileAdapter(showing)
        file_recycler_view.apply {
            adapter = this@FileFragment.adapter
            layoutManager = GridLayoutManager(context, 2)
        }
        Logger.info(Processes.Debug, file_recycler_view::class.jvmName)

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

        adapter.setOnItemClickListener {
            file_recycler_view.forEachHolder<FileAdapter.FileHolder> { holder ->
                if (holder.layoutPosition != it) {
                    holder.unselectCard()
                }
            }
            if (adapter.selectedFile?.isDirectory == true) {
                switchFabTo(R.drawable.ic_open_in_new_black) {
                    (activity as MainActivity).fab.rotation = 0f
                }
            } else {
                switchFabTo(R.drawable.ic_add_white) {
                    (activity as MainActivity).fab.rotation = 45f
                }
            }
        }

        adapter.setOnAltButtonClickListener { _, _ ->
            startActivity(
                Intent(context, ProjectActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(
                    activity,
                    if (!isExtendCardsAdded) adapter.newProjectCard
                    else topCard?.findViewById<View>(R.id.extend_card_new_project),
                    "shared"
                ).toBundle()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (isOverflow(nestedScrollView.scrollY) && !isExtendCardsAdded) addExtendCards()
    }

    fun removeExtendCards() {
        adapter.newProjectCard?.transitionName = "shared"
        val view = topCard?.findViewById<LinearLayout>(R.id.layout_extends) ?: return
        topCard!!.forEach {
            if (it.id == R.id.layout_extends && it != view)
                topCard!!.removeView(it)
        }
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
        if (isExtendCardsAdded) return
        isExtendCardsAdded = true
        val view = extendCards(disableBack = adapter.root == Environment.getExternalStorageDirectory())
        adapter.newProjectCard?.transitionName = null
        view.findViewById<View>(R.id.extend_card_new_project).transitionName = "shared"
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
    }

    // On fab click
    override fun onClick(v: View?) {
        val path = adapter.selectedFile?.absolutePath
        if (path != null) {
            startActivity(
                Intent(
                    context,
                    ProjectActivity::class.java
                ).putExtra("open", path)
            )
        }
    }

    companion object {
        var showing: File = Environment.getExternalStorageDirectory()

    }
}
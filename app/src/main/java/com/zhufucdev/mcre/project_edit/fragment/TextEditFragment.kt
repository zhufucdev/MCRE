package com.zhufucdev.mcre.project_edit.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.element.PackName
import com.zhufucdev.mcre.project_edit.element.StringElement
import com.zhufucdev.mcre.recycler_view.ColorsAdapter
import com.zhufucdev.mcre.utility.*
import kotlinx.android.synthetic.main.fragment_edit_text.*
import kotlin.reflect.KProperty0

class TextEditFragment(val itemName: String, val value: KProperty0<StringElement>) :
    BaseFragment(R.layout.fragment_edit_text) {
    private lateinit var render: MCTextRender
    private val isColorsPanelShown get() = colors_panel.translationY == cpShowTranslation
    private val cpHideTranslation get() = colors_panel.measuredHeight.toFloat()
    private val cpShowTranslation get() = 0f
    private val bottomAppBar by lazy { activity?.findViewById<BottomAppBar>(R.id.bottom_app_bar) }
    private val keyboardListener by lazy { KeyboardChangeListener(activity!!.findViewById(R.id.project_root)) }
    private val colorsAdapter = ColorsAdapter()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        item_name.text = getString(R.string.title_edit_what, itemName)
        edit_text.setText(value.get().value)
        render = MCTextRender(edit_text)
        render.doRender()
        render.setExceptionListener {
            edit_text.error =
                if (it is StringIndexOutOfBoundsException) {
                    getString(R.string.exception_expecting, getString(R.string.name_hex))
                } else {
                    getString(R.string.exception_render_mc_text)
                }
        }

        // UI
        edit_text.doAfterTextChanged {
            value.get().value = edit_text.editableText.toString()
        }
        format_recycler_view.apply {
            adapter = colorsAdapter
            layoutManager = GridLayoutManager(context, 4)
        }
        colors_panel.apply {
            measure()
            translationY = cpHideTranslation
        }

        keyboardListener.setKeyBoardListener { shown, _ ->
            if (isColorsPanelShown && shown) {
                colors_panel.translationY = cpHideTranslation
                bottomAppBar?.performShow()
            }
        }

        // Handle insertion
        colorsAdapter.setOnSelectionClickListener { color ->
            edit_text.editableText.insert(edit_text.selectionStart, color.code)
            if (edit_text.selectionStart != edit_text.selectionEnd &&
                edit_text.text?.let {
                    val end = edit_text.selectionEnd
                    !it.substring(end - 2).trimStart().startsWith(TextUtil.END)
                } != false) {
                edit_text.editableText.insert(edit_text.selectionEnd, TextUtil.END)
            }
        }
    }

    override val fabListener: ((View) -> Unit)? = {
        if (keyboardListener.isShowing && !isColorsPanelShown) {
            edit_text.hideSoftKeyboard()
        }
        val animator =
            if (isColorsPanelShown) {
                bottomAppBar?.performShow()
                ObjectAnimator.ofFloat(
                    colors_panel,
                    "translationY",
                    cpShowTranslation,
                    cpHideTranslation
                ) to AccelerateInterpolator()
            }
            else {
                bottomAppBar?.performHide()
                ObjectAnimator.ofFloat(
                    colors_panel,
                    "translationY",
                    cpHideTranslation,
                    cpShowTranslation
                ) to DecelerateInterpolator()
            }
        animator.first.apply {
            duration = 300L
            interpolator = animator.second
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::render.isInitialized) render.destroy()
    }

    override val fabResource: Int
        get() = R.drawable.ic_edit_white
}
package com.zhufucdev.mcre.project_edit.fragment

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.EditableProject
import com.zhufucdev.mcre.project_edit.element.*
import com.zhufucdev.mcre.exception.*
import com.zhufucdev.mcre.utility.nameify
import kotlinx.android.synthetic.main.fragment_new_element.*
import kotlin.reflect.KFunction

class NewElementFragment(project: EditableProject, private val refresh: KFunction<Unit>) : BaseFragment(R.layout.fragment_new_element, project) {
    override val fabResource: Int
        get() = R.drawable.ic_done_white

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        type_spinner.adapter =
            ArrayAdapter
                .createFromResource(context!!, R.array.project_element_types, android.R.layout.simple_spinner_item)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        text_input_name.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                text_input_description.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override val fabListener: (View) -> Unit = {
        val name = text_input_name.text?.toString()
        val description = text_input_description.text?.toString()
        when {
            name.isNullOrEmpty() -> {
                text_input_name.error = requireContext().getString(R.string.exception_empty_input)
            }
            description.isNullOrEmpty() -> {
                text_input_description.error = requireContext().getString(R.string.exception_empty_input)
            }
            else -> {
                val element: BaseElement = when (val s =
                    requireContext().resources.getStringArray(R.array.project_element_type_values)[type_spinner.selectedItemPosition]) {
                    "string" -> UserStringElement(name.nameify(), description.nameify())
                    else -> throw ElementNotFoundException(s.nameify())
                }
                project.elements.add(element)
                tab.close()
                refresh.call()
            }
        }
    }

    override fun initAppbar(menu: Menu) {
    }
}
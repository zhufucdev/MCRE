package com.zhufucdev.mcre.project_edit.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.utility.hideThen

abstract class BaseFragment(contentLayoutID: Int): Fragment(contentLayoutID) {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        callFab()
    }

    override fun onResume() {
        super.onResume()
        callFab()
    }

    private fun callFab() {
        val fab = fab ?: return
        if (fab.tag != fabResource) {
            fab.tag = fabResource
            fab.hideThen {
                setImageResource(fabResource)
                show()
            }
        }
        if (fabListener != null)
            fab.setOnClickListener(fabListener!!)
        else
            fab.setOnClickListener(null)
    }

    abstract val fabResource: Int
    open val fabListener: ((View) -> Unit)? get() = null
    val fab get() = activity?.findViewById<FloatingActionButton>(R.id.fab)
}
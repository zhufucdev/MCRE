package com.zhufucdev.mcre.settings

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.doOnLayout
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.exception.ElementNotFoundException
import com.zhufucdev.mcre.utility.nameify

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (view is LinearLayout) {
            findNavController().currentDestination?.arguments?.get("highlight")?.let {
                val selection = findPreference<Preference>(it.defaultValue as String)
                    ?: throw ElementNotFoundException(it.defaultValue.toString().nameify())
                try {
                    listView.doOnLayout {
                        listView[selection.order].apply {
                            val oldBackground = background
                            var isRecovered = false
                            background = resources.getDrawable(R.color.colorAccentLight)
                            setOnTouchListener { _, _ ->
                                if (!isRecovered) {
                                    background = oldBackground
                                    isRecovered = true
                                }
                                false
                            }
                        }
                    }
                } catch (ignore: Exception) {
                }
            }
        }
    }
}
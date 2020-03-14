package com.zhufucdev.mcre.settings

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zhufucdev.mcre.R

class MainFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_main, rootKey)

        findPreference<Preference>("appearance")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_appearanceFragment)
            true
        }
        findPreference<Preference>("general")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_generalFragment)
            true
        }
    }
}
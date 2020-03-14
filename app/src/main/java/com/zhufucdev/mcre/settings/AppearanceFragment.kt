package com.zhufucdev.mcre.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.zhufucdev.mcre.R

class AppearanceFragment : BasePreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_appearance, rootKey)
        findPreference<SwitchPreferenceCompat>("night_mode")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, v ->
                AppCompatDelegate.setDefaultNightMode(
                    if (v as Boolean) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                true
            }
    }
}
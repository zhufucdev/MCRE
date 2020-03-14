package com.zhufucdev.mcre.settings

import android.os.Bundle
import androidx.navigation.NavArgument
import androidx.navigation.NavType
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.R
import java.io.File

class GeneralFragment : BasePreferenceFragment() {
    private val changes = arrayListOf<String>()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_general, rootKey)

        findPreference<EditTextPreference>("pack_root")?.apply {
            text = Env.packsRoot.absolutePath
            setSummaryProvider {
                Env.packsRoot.absolutePath
            }
            setOnPreferenceChangeListener { _, newValue ->
                if (File(newValue as String) != Env.packsRoot) {
                    notifyChange(key)
                    Env.packsRoot = File(newValue)
                }
                true
            }
        }
    }

    private fun notifyChange(id: String) {
        if (!changes.contains(id)) {
            changes.add(id)
            val controller = findNavController()
            controller.graph.addArgument(
                "changes",
                NavArgument.Builder()
                    .setType(NavType.StringArrayType)
                    .setIsNullable(false)
                    .setDefaultValue(changes.toTypedArray())
                    .build()
            )
        }
    }
}
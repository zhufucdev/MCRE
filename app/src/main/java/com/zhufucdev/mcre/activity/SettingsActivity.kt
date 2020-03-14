package com.zhufucdev.mcre.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.zhufucdev.mcre.Processes
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.fragment.ManagerFragment
import com.zhufucdev.mcre.settings.MainFragment
import com.zhufucdev.mcre.utility.Logger
import kotlinx.android.synthetic.main.settings_activity.*

@Suppress("UNCHECKED_CAST")
class SettingsActivity : BaseActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navController = findNavController(R.id.settings_content)
        val highlight = intent.getStringExtra("highlight")
        val navigateID =
            intent.getIntExtra("navigate", -1).let {
                if (it != -1) navController.navigate(it)
                navController.currentDestination?.id
            }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.label == MainFragment::class.simpleName) {
                updateResult()
            } else if (destination.id == navigateID && highlight != null) {
                destination.addArgument("highlight", NavArgument.Builder().setDefaultValue(highlight).build())
            }
        }
    }

    private fun updateResult() {
        val args = navController.graph.arguments["changes"]
        if (args != null)
            setResult(
                0,
                Intent().putExtra(
                    "changes",
                    args.defaultValue as Array<String>
                )
            )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun showSnackBar(builder: (View) -> Snackbar) {
        builder(settings_root).show()
    }

    override fun onBackPressed() {
        if (intent.getIntExtra("navigate", -1) != -1) {
            updateResult()
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
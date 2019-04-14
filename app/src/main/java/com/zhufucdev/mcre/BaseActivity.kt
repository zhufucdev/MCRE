package com.zhufucdev.mcre

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Environment.presentActivity = this
    }

    abstract fun showSnackBar(builder: (CoordinatorLayout) -> Snackbar)
}
package com.zhufucdev.mcre.internal

import android.content.Context

interface ContextStringSupplier {
    fun get(context: Context): String
}
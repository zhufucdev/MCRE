package com.zhufucdev.mcre.exception

import android.content.Context
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.internal.ContextStringSupplier
import java.io.File

class FileNotFoundException(file: File): FileSystemException(file), ContextStringSupplier {
    override fun get(context: Context): String = context.getString(R.string.exception_file_not_found, file.absolutePath)
}
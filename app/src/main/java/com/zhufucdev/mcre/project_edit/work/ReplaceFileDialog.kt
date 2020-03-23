package com.zhufucdev.mcre.project_edit.work

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.utility.DataUnit
import com.zhufucdev.mcre.view.SimpleItemView
import kotlinx.android.synthetic.main.dialog_file_replace.view.*
import java.io.File
import java.util.*

class ReplaceFileDialog(context: Context, replacer: FileInfo, replace: FileInfo) :
    AlertDialog(context, false, null) {

    private var mOnReplaceClickListener: (() -> Unit)? = null
    private var mOnSkipClickListener: (() -> Unit)? = null
    private var mOnKeepBothClickListener: (() -> Unit)? = null

    fun setOnReplaceClickListener(l: () -> Unit) {
        mOnReplaceClickListener = l
    }

    fun setOnSkipClickListener(l: () -> Unit) {
        mOnSkipClickListener = l
    }

    fun setOnKeepBothClickListener(l: () -> Unit) {
        mOnKeepBothClickListener = l
    }

    init {
        setTitle(R.string.title_file_replacing)
        val view = layoutInflater.inflate(R.layout.dialog_file_replace, null)
        setView(view)
        val replacerText = view.file_replacing
        val replaceText = view.file_existing
        val text = view.text
        fun apply(item: SimpleItemView, info: FileInfo) {
            item.apply {
                title = info.name
                setIcon(
                    if (info.isFolder)
                        R.drawable.ic_folder_black
                    else
                        R.drawable.ic_file_black
                )
                val type = context.getString(if (info.isFolder) R.string.name_folder else R.string.name_file)
                val dateModified = context.getString(R.string.info_modified, Env.formatDate(info.dateModified))
                val size =
                    if (info.size != null) "${info.size!!.formatedValue} ${info.size!!.name}"
                    else if (!info.isSizeCalculating) context.getString(R.string.info_size_unknown)
                    else {
                        info.setOnSizeCalculatedListener {
                            subtitle = "$type\n$dateModified\n${it.formatedValue} ${it.name}"
                        }
                        context.getString(R.string.info_calculating)
                    }
                subtitle = "$type\n$dateModified\n$size"
            }
        }
        apply(replaceText, replace)
        apply(replacerText, replacer)
        text.text = context.getString(R.string.info_replace_or_not, replace.name)
        setButton(BUTTON_POSITIVE, context.getString(R.string.action_replace)) { _, _ ->
            mOnReplaceClickListener?.invoke()
        }
        setButton(BUTTON_NEUTRAL, context.getString(R.string.action_keep_both)) { _, _ ->
            mOnKeepBothClickListener?.invoke()
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.action_skip)) { _, _ ->
            mOnSkipClickListener?.invoke()
        }
    }

    class FileInfo {
        val name: String
        val isFolder: Boolean
        val dateModified: Date
        var size: DataUnit?
        var isSizeCalculating = false
        private var mOnSizeCalculatedListener: ((DataUnit) -> Unit)? = null
        fun setOnSizeCalculatedListener(l: (DataUnit) -> Unit) {
            if (!isSizeCalculating) l.invoke(size!!)
            mOnSizeCalculatedListener = l
        }

        constructor(
            name: String,
            isFolder: Boolean,
            dateModified: Date,
            size: DataUnit?
        ) {
            this.name = name
            this.isFolder = isFolder
            this.dateModified = dateModified
            this.size = size
        }

        constructor(file: File) {
            name = file.name
            isFolder = file.isDirectory
            dateModified = Date(file.lastModified())
            size = null
            isSizeCalculating = true

            // Calculate file size
            Env.threadPool.execute {
                size = Env.formatedSize(file)
                mOnSizeCalculatedListener?.invoke(size!!)
            }
        }
    }
}
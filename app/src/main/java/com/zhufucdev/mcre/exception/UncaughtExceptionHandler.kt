package com.zhufucdev.mcre.exception

import android.content.Context
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import com.zhufucdev.mcre.R
import kotlin.concurrent.thread

class UncaughtExceptionHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        thread {
            Looper.prepare()
            val builder =
                AlertDialog.Builder(context)
                    .setIcon(R.drawable.ic_error_black)
                    .setTitle(R.string.title_unhandled_exception)
            var firstCause: Throwable = e
            while (true) {
                if (firstCause.cause == null || firstCause is LocalizedException) break
                firstCause = firstCause.cause!!
            }
            val message =
                if (firstCause is LocalizedException) {
                    context.getString((firstCause as LocalizedException).messageID, *firstCause.formatArgs)
                } else {
                    "in ${t.name} thread" +
                            "${System.lineSeparator()} ${e::class.simpleName}: ${e.message} "
                }

            if (t.name == "main") {
                builder
                    .setCancelable(false)
                    .setMessage(message)
                    .setPositiveButton(R.string.action_close_app) { dialog, _ ->
                        dialog.dismiss()
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
            } else {
                builder
                    .setCancelable(true)
                    .setMessage("$message ${System.lineSeparator()} ${context.getString(R.string.info_unhandled_exception)}")
                    .setPositiveButton(R.string.action_close, null)
            }

            builder.show()
            Looper.loop()
        }
    }
}
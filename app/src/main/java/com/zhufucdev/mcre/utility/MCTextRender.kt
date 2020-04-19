package com.zhufucdev.mcre.utility

import android.graphics.Typeface
import android.os.Handler
import android.os.Message
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.*
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.set
import androidx.core.widget.doAfterTextChanged
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.Processes
import javax.security.auth.Destroyable

class MCTextRender(private val attachTo: TextView): Destroyable {
    private lateinit var watcher: TextWatcher
    init {
        if (attachTo is EditText)
            watcher = attachTo.doAfterTextChanged { renderTextFormat() }
    }

    private val handler = Handler {
        if (it.what == 0)
            attachTo.setTextKeepState(it.obj as SpannableStringBuilder)
        else
            mExceptionListener?.invoke(it.obj as Exception)
        true
    }
    private val context = attachTo.context!!
    private var lastRenderRequest = System.currentTimeMillis()
    private var lastRender = System.currentTimeMillis()
    private var lastRenderedContent = ""
    private fun renderTextFormat() {
        if (System.currentTimeMillis() - lastRender < 300)
            return
        lastRenderRequest = System.currentTimeMillis()
        Env.threadPool.execute {
            Thread.sleep(300L)
            if (System.currentTimeMillis() - lastRenderRequest < 300)
                return@execute
            // Do render if there is no request in 500ms
            doRender()
        }
    }

    fun doRender(sourceText: String? = null) {
        lastRender = System.currentTimeMillis()
        val rawText = sourceText?.also { attachTo.text = it } ?: attachTo.text
        mRenderListener?.invoke(lastRenderedContent, rawText.toString())
        lastRenderedContent = rawText.toString()
        try {
            val selections = rawText.split(TextUtil.END)
            val spanBuilder = SpannableStringBuilder()
            val handleSelection: (Int, String) -> Unit = { index, selection ->
                // e.g.This is §aHello§lWorld
                val firstMark = selection.indexOf(TextUtil.KEY)
                val startOffset = spanBuilder.length
                spanBuilder.append(selection)
                if (firstMark != -1) {
                    // Render colors
                    var index = firstMark
                    while (index != -1) {
                        val code = selection.substring(index, index + 2)
                        val format = TextUtil.TextColor.of(code)
                        var next = selection.indexOf(TextUtil.KEY, index + 1)
                        while (
                            next != -1
                            && next + 2 <= selection.length
                            && !selection.substring(next, next + 2).let { TextUtil.TextColor.of(it).isColorOrEnd }
                        ) {
                            next = selection.indexOf(TextUtil.KEY, next + 1)
                        }
                        if (format.isColor) {
                            val end: Int = if (next == -1) {
                                selection.length
                            } else {
                                next
                            }
                            spanBuilder[index + startOffset, end + startOffset] =
                                ForegroundColorSpan(context.resources.getColor(format.resourceID))
                            if (format.backgroundID != -1) {
                                spanBuilder[index + startOffset, end + startOffset] =
                                    BackgroundColorSpan(context.resources.getColor(format.backgroundID))
                            }
                        }
                        index = next
                    }
                    index = firstMark
                    // Render format
                    loop@ while (index != -1) {
                        val code = selection.substring(index, index + 2)
                        val format = TextUtil.TextColor.of(code)
                        if (!format.isColor) {
                            fun isBoldAlready(): Boolean {
                                for (i in index - 2 downTo 0) {
                                    val code1 = selection.substring(i, i + 2)
                                    if (code1 == TextUtil.TextColor.BOLD.code)
                                        return true
                                }
                                return false
                            }

                            fun isItalicAlready(): Boolean {
                                for (i in index - 2 downTo 0) {
                                    val code1 = selection.substring(i, i + 2)
                                    if (code1 == TextUtil.TextColor.ITALIC.code)
                                        return true
                                }
                                return false
                            }

                            val span: Any = when (format) {
                                TextUtil.TextColor.BOLD -> StyleSpan(if (isItalicAlready()) Typeface.BOLD_ITALIC else Typeface.BOLD)
                                TextUtil.TextColor.ITALIC -> StyleSpan(if (isBoldAlready()) Typeface.BOLD_ITALIC else Typeface.ITALIC)
                                TextUtil.TextColor.UNDERLINED -> UnderlineSpan()
                                TextUtil.TextColor.STRUCK_THROUGH -> StrikethroughSpan()
                                else -> {
                                    index = selection.indexOf(TextUtil.KEY, index + 1)
                                    continue@loop
                                }
                            }
                            spanBuilder[index + startOffset, selection.length + startOffset] = span
                        }
                        index = selection.indexOf(TextUtil.KEY, index + 1)
                    }
                }
                if (index != selections.lastIndex) {
                    spanBuilder.append(TextUtil.END)
                }
            }
            selections.forEachIndexed(handleSelection)

            handler.sendMessage(Message.obtain(handler, 0, spanBuilder))
        } catch (e: Exception) {
            Logger.error(Processes.MCTextRender, "While rendering MC text:", e)
            handler.sendMessage(Message.obtain(handler, 1, e))
        }
    }

    private var mExceptionListener: ((Exception) -> Unit)? = null
    fun setExceptionListener(l: (Exception) -> Unit) {
        mExceptionListener = l
    }

    private var mRenderListener: ((String, String) -> Unit)? = null
    fun setRenderListener(l: (String, String) -> Unit) {
        mRenderListener = l
    }

    private var mDestroyed = false
    override fun destroy() {
        if (mDestroyed) throw IllegalStateException("Object has already been destroyed.")
        if (::watcher.isInitialized)
            attachTo.removeTextChangedListener(watcher)
    }
}
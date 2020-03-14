package com.zhufucdev.mcre.utility

import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener


class KeyboardChangeListener(private val contentView: View) : OnGlobalLayoutListener {
    private var mOriginHeight = 0
    private var mPreHeight = 0
    private var mKeyBoardListen: ((Boolean, Int) -> Unit)? = null
    var isShowing = false
        private set

    private fun addContentTreeObserver() {
        contentView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        val currHeight: Int = contentView.height
        if (currHeight == 0) {
            return
        }
        var hasChange = false
        if (mPreHeight == 0) {
            mPreHeight = currHeight
            mOriginHeight = currHeight
        } else {
            if (mPreHeight != currHeight) {
                hasChange = true
                mPreHeight = currHeight
            } else {
                hasChange = false
            }
        }
        if (hasChange) {
            val isShow: Boolean
            var keyboardHeight = 0
            if (mOriginHeight == currHeight) {
                isShow = false
            } else {
                keyboardHeight = mOriginHeight - currHeight
                isShow = true
            }
            isShowing = isShow
            mKeyBoardListen?.invoke(isShow, keyboardHeight)
        }
    }

    fun setKeyBoardListener(keyBoardListen: (Boolean, Int) -> Unit) {
        mKeyBoardListen = keyBoardListen
    }

    fun destroy() {
        contentView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
    }

    interface KeyBoardListener {
        fun onKeyboardChange(isShow: Boolean, keyboardHeight: Int)
    }

    init {
        addContentTreeObserver()
    }
}
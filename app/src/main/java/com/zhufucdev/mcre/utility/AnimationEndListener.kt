package com.zhufucdev.mcre.utility

import android.view.animation.Animation

class AnimationEndListener(private val onEnd: () -> Unit) : Animation.AnimationListener {
    override fun onAnimationRepeat(animation: Animation?) {}

    override fun onAnimationEnd(animation: Animation?) {
        onEnd.invoke()
    }

    override fun onAnimationStart(animation: Animation?) {}
}
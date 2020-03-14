package com.zhufucdev.mcre.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.view.animation.OvershootInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.doOnEnd
import com.zhufucdev.mcre.R

class SelectableIconView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int)
            : super(context, attributes, defStyleAttr)

    private var imgBitmap: Bitmap? = null
    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        imgBitmap = bm
    }

    private fun rotate(from: Float, to: Float, overshoot: Boolean = false, onEnd: ((Animator) -> Unit)? = null) {
        ObjectAnimator.ofFloat(from, to).apply {
            duration = 150
            if (overshoot)
                interpolator = OvershootInterpolator(2f)
            addUpdateListener {
                rotationY = animatedValue as Float
            }
            if (onEnd != null)
                doOnEnd(onEnd)
            start()
        }
    }

    private var oldColorTint = imageTintList
    private var oldDrawable = drawable
    fun rebuildImageResource(res: Int) {
        oldDrawable = context.getDrawable(res)
        setImageResource(res)
    }

    var isIconSelected = false
        private set
    fun select() {
        if (isIconSelected) return
        rotate(0f, 90f) {
            setImageResource(R.drawable.ic_check_circle_white)
            imageTintList =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.getColorStateList(R.color.colorAccent)
                else context.resources.getColorStateList(R.color.colorAccent)
            rotate(-90f, 0f, true)
        }
        isIconSelected = true
    }

    fun unselect() {
        if (!isIconSelected) return
        rotate(0f, 90f) {
            imageTintList = oldColorTint
            if (imgBitmap != null)
                setImageBitmap(imgBitmap)
            else
                setImageDrawable(oldDrawable)
            rotate(-90f, 0f, true)
        }
        isIconSelected = false
    }
}
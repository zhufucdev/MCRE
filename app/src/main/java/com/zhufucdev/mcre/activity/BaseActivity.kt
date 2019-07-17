package com.zhufucdev.mcre.activity

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.utility.SwipeDirection
import kotlin.math.absoluteValue

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Env.presentActivity = this
    }

    open fun onSwipe(fraction: Float, direction: SwipeDirection) {}
    open fun swipeBegan(direction: SwipeDirection) {}
    open fun swipeEnd(fraction: Float, direction: SwipeDirection) {}

    private var startX = 0f
    private var startY = 0f
    private var isSwipeStarted = false
    private var lastDirection = SwipeDirection.LEFT
    private var lastFraction = 0f
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val width = resources.displayMetrics.widthPixels
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                super.dispatchTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = event.x - startX
                val offsetYAbs = (event.y - startY).absoluteValue
                val offsetXAbs = offsetX.absoluteValue
                if (offsetXAbs >= width * 0.1 && offsetYAbs <= 100f) {
                    val direction = if (offsetX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                    if (!isSwipeStarted || lastDirection != direction) {
                        lastDirection = direction
                        swipeBegan(direction)
                        isSwipeStarted = true
                    }
                    lastFraction = offsetXAbs / if (offsetX > 0) (width - startX) else startX
                    onSwipe(lastFraction, direction)
                }
                if (!isSwipeStarted) super.dispatchTouchEvent(event) else true
            }
            MotionEvent.ACTION_UP -> {
                val isStarted = isSwipeStarted
                if (isSwipeStarted) {
                    swipeEnd(lastFraction, lastDirection)
                    isSwipeStarted = false
                }
                if (!isStarted) super.dispatchTouchEvent(event) else true
            }
            else -> super.dispatchTouchEvent(event)
        }
    }

    abstract fun showSnackBar(builder: (View) -> Snackbar)
}
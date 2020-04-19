package com.zhufucdev.mcre.utility

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Service
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.ActionMenuView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.Processes
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.Name
import kotlin.concurrent.thread
import kotlin.math.absoluteValue

inline fun <T : RecyclerView.ViewHolder> RecyclerView.forEachHolder(l: (T) -> Unit) =
    forEachHolderIndexed<T> { t, _ -> l(t) }

inline fun <T : RecyclerView.ViewHolder> RecyclerView.forEachHolderIndexed(l: (T, Int) -> Unit) {
    for (i in 0 until childCount) l(findViewHolderForAdapterPosition(i) as T, i)
}

inline fun <T : RecyclerView.ViewHolder> RecyclerView.any(l: (T) -> Boolean): Boolean {
    for (i in 0 until childCount) if (l(findViewHolderForAdapterPosition(i) as T)) return true
    return false
}


fun <T : RecyclerView.ViewHolder> RecyclerView.get(position: Int) = findViewHolderForLayoutPosition(position) as T

inline fun <T : RecyclerView.ViewHolder> RecyclerView.countIf(l: (T) -> Boolean): Int {
    var count = 0
    forEachHolder<T> { if (l(it)) count++ }
    return count
}

fun BottomAppBar.getActionMenu(): ActionMenuView? =
    this.javaClass.declaredMethods.firstOrNull { it.name == "getActionMenuView" }?.apply {
        isAccessible = true
    }?.invoke(this) as ActionMenuView?

fun BottomAppBar.animateMenuChange(onDismiss: () -> Unit) {
    if (getActionMenu() == null) {
        menu.clear()
    }
    getActionMenu()?.apply {
        startAnimation(
            AlphaAnimation(1f, 0f).apply {
                duration = 150
                setAnimationListener(AnimationEndListener {
                    onDismiss()
                    startAnimation(
                        AlphaAnimation(0f, 1f).apply {
                            duration = 150
                        }
                    )
                })
            }
        )
    } ?: onDismiss()
}

fun CardView.setCardOnClickListenerWithPosition(
    onCardClickListener: ((x: Float, y: Float) -> Unit)? = null,
    onCardLongClickListener: ((x: Float, y: Float) -> Unit)? = null
) {
    var isUp: Boolean
    var isLongClick = false
    var lastDown = 0L
    val cardDefaultElevation =
        context.resources.getDimension(com.google.android.material.R.dimen.cardview_default_elevation)
    if (onCardClickListener == null && onCardLongClickListener == null) {
        setOnTouchListener(null)
    } else {
        setOnTouchListener { _, event ->
            fun startElevationAnimation(a: ValueAnimator) {
                a.apply {
                    duration = 150
                    addUpdateListener {
                        cardElevation = animatedValue as Float
                    }
                    start()
                }
            }

            val maxElevation = resources.getDimension(R.dimen.padding_normal)
            if (event.action == MotionEvent.ACTION_DOWN) {
                isUp = false
                if (System.currentTimeMillis() - lastDown <= 300) {
                    isUp = true
                }
                lastDown = System.currentTimeMillis()
                startElevationAnimation(ObjectAnimator.ofFloat(cardElevation, maxElevation))
                Handler().postDelayed({
                    if (!isUp) {
                        isLongClick = true
                        //On Long Click
                        onCardLongClickListener?.invoke(event.x, event.y)
                        (context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator).apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrate(
                                    VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                                )
                            } else {
                                vibrate(10)
                            }
                        }
                    } else {
                        isLongClick = false
                    }
                }, 300)
            } else if (event.action == MotionEvent.ACTION_UP) {
                isUp = true
                if (!isLongClick) {
                    //On Click
                    onCardClickListener?.invoke(event.x, event.y)
                } else {
                    isLongClick = false
                }
            }
            if (event.action != MotionEvent.ACTION_DOWN) {
                isUp = true
                startElevationAnimation(ObjectAnimator.ofFloat(cardElevation, cardDefaultElevation))
            }

            true
        }
    }
}

fun View.setOnClickListenerWithPosition(
    onCardClickListener: ((x: Float, y: Float) -> Unit)? = null,
    onCardLongClickListener: ((x: Float, y: Float) -> Unit)? = null
) {
    var isUp: Boolean
    var isLongClick = false
    var lastDown = 0L
    setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            isUp = false
            if (System.currentTimeMillis() - lastDown <= 300) {
                isUp = true
            }
            lastDown = System.currentTimeMillis()
            Handler().postDelayed({
                if (!isUp) {
                    isLongClick = true
                    //On Long Click
                    if (onCardLongClickListener != null) {
                        onCardLongClickListener.invoke(event.x, event.y)
                        (context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator).apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrate(
                                    VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                                )
                            } else {
                                vibrate(10)
                            }
                        }
                    }
                } else {
                    isLongClick = false
                }
            }, 300)
        } else if (event.action == MotionEvent.ACTION_UP) {
            isUp = true
            if (!isLongClick) {
                //On Click
                onCardClickListener?.invoke(event.x, event.y)
            } else {
                isLongClick = false
            }
        }
        if (event.action != MotionEvent.ACTION_DOWN) {
            isUp = true
        }

        true
    }
}

fun View.measure() = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED).let { measure(it, it) }

enum class SwipeDirection {
    LEFT, RIGHT
}

fun View.setHorizantalSwipeListener(l: (Float, SwipeDirection) -> Unit) {
    var startX = 0f
    Logger.info(Processes.Debug, "Width = $width")
    setOnTouchListener { _, event ->
        Logger.info(Processes.Debug, "Event = ${event.action}")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = event.x - startX
                val offsetXAbs = offsetX.absoluteValue
                l(offsetXAbs / (width - startX), if (offsetX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT)
                true
            }
            else -> false
        }
    }
}

inline fun FloatingActionButton.hideThen(crossinline action: FloatingActionButton.() -> Unit) {
    hide(object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            action.invoke(this@hideThen)
        }
    })
}

fun View.hideSoftKeyboard() {
    val manager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun String.nameify() = Name(this)
fun Int.nameify() = Name(this)

fun alert(error: Exception) {
    thread(name = "virtual") { throw error }
}

infix fun Int.orBigger(b: Int): Int = if (this >= b) this else b

var MenuItem.enabled
    get() = this.isEnabled
    set(value) {
        this.iconTintList =
            if (value) null
            else Env.presentActivity.getColorStateList(R.color.colorDisabled)
        this.isEnabled = value
    }
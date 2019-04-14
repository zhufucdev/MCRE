package com.zhufucdev.mcre.recycler_view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.mcre.Environment
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.pack.BedrockPack
import com.zhufucdev.mcre.pack.ResourcesPack
import com.zhufucdev.mcre.pack.ResourcesPack.Type.*
import kotlinx.android.synthetic.main.recycler_pack_holder.view.*
import java.io.File
import java.util.concurrent.TimeUnit

class PacksAdapter : RecyclerView.Adapter<PacksAdapter.PackHolder>() {
    class PackHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById<AppCompatImageView>(R.id.img_pack_icon)!!
        val name = itemView.findViewById<TextView>(R.id.text_pack_name)!!
        val description = itemView.findViewById<TextView>(R.id.text_pack_description)!!
        val buttonExpand = itemView.findViewById<AppCompatImageView>(R.id.btn_pack_expand)!!
        val infoGroup = itemView.findViewById<CardView>(R.id.group_info)!!
        val actionGroup = itemView.findViewById<LinearLayout>(R.id.layout_pack_actions)!!
        val card = itemView.findViewById<CardView>(R.id.outer_card)!!.also { defaultElevation = it.cardElevation }

        fun expendInfo() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buttonExpand.tooltipText = buttonExpand.context.getText(R.string.action_collapse)
            }
            infoGroup.visibility = View.VISIBLE
            ObjectAnimator.ofInt(0, targetHeight).apply {
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    val v = animatedValue as Int
                    infoGroup.alpha = v.toFloat() / targetHeight
                    infoGroup.layoutParams = (infoGroup.layoutParams as RelativeLayout.LayoutParams).apply {
                        height = v
                    }
                }
                doOnEnd {
                    buttonExpand.isEnabled = true
                }
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(0f, 180f).apply {
                addUpdateListener {
                    buttonExpand.rotation = animatedValue as Float
                }
                duration = 300
                start()
            }
        }

        fun collapseInfo() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buttonExpand.tooltipText = buttonExpand.context.getText(R.string.action_expend)
            }
            ObjectAnimator.ofInt(targetHeight, 0).apply {
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    val v = animatedValue as Int
                    infoGroup.alpha = v.toFloat() / targetHeight
                    infoGroup.layoutParams = (infoGroup.layoutParams as RelativeLayout.LayoutParams).apply {
                        height = v
                    }
                }
                doOnEnd {
                    infoGroup.visibility = View.GONE
                    buttonExpand.isEnabled = true
                }
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(180f, 0f).apply {
                addUpdateListener {
                    buttonExpand.rotation = animatedValue as Float
                }
                duration = 300
                start()
            }
        }

        val isCardSelected get() = card.cardBackgroundColor.defaultColor == card.context.resources.getColor(R.color.colorAccentLight)
        fun selectCard() {
            card.setCardBackgroundColor(card.context.resources.getColor(R.color.colorAccentLight))
        }

        fun unselectCard() {
            card.setCardBackgroundColor(card.context.resources.getColor(android.R.color.white))
        }

        val isActionGroupShown get() = actionGroup.isVisible
        fun showActions(x: Int, y: Int) {
            ViewAnimationUtils.createCircularReveal(
                actionGroup,
                x,
                y,
                0f,
                actionGroup.measuredWidth.toFloat()
            ).apply {
                duration = 170
                doOnEnd {
                    unselectCard()
                }
                start()
            }
        }
        fun hideActions(x: Int, y: Int) {
            ViewAnimationUtils.createCircularReveal(
                actionGroup,
                x,
                y,
                actionGroup.measuredWidth.toFloat(),
                0f
            ).apply {
                duration = 170
                doOnEnd {
                    actionGroup.visibility = View.INVISIBLE
                }
                start()
            }
        }

        companion object {
            var targetHeight = 0
            var defaultElevation = 0f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackHolder =
        PackHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_pack_holder, parent, false))

    override fun getItemCount(): Int = Environment.packs.size

    private var onCardClickListener: ((Int) -> Unit)? = null
    private var onCardLongClickListener: ((Int) -> Unit)? = null
    fun setOnCardClickListener(l: (Int) -> Unit) {
        onCardClickListener = l
    }

    fun setOnCardLongClickListener(l: (Int) -> Unit) {
        onCardLongClickListener = l
    }

    private fun forPack(holder: PackHolder, item: ResourcesPack) {
        holder.apply {
            if (item.icon.exists()) {
                icon.setImageBitmap(
                    Environment.threadPool.submit<Bitmap> { BitmapFactory.decodeStream(item.icon.inputStream()) }[5, TimeUnit.SECONDS]
                )
            } else {
                icon.setImageResource(R.drawable.ic_block_grey)
            }
            name.text = item.header.name
            description.text = item.header.description
            infoGroup.findViewById<TextView>(R.id.text_info_pack_version).text = infoGroup.context.let {
                it.getString(
                    R.string.info_pack_version,
                    it.getString(if (item is BedrockPack) R.string.version_bedrock else R.string.version_java)
                )
            }

            infoGroup.visibility = View.GONE
            buttonExpand.rotation = 0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buttonExpand.tooltipText = buttonExpand.context.getText(R.string.action_expend)
            }
            buttonExpand.setOnClickListener {
                //To expand the info card
                //=> Disable the expand button
                buttonExpand.isEnabled = false
                if (PackHolder.targetHeight == 0) {
                    //=> Measure the card's size
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED).let { infoGroup.measure(it, it) }
                    PackHolder.targetHeight = infoGroup.measuredHeight
                }
                //=> Animator
                buttonExpand.pivotX = buttonExpand.width / 2f
                buttonExpand.pivotY = buttonExpand.height / 2f
                if (infoGroup.visibility == View.GONE) {
                    //==> Calac data
                    val size = infoGroup.findViewById<TextView>(R.id.text_pack_size)
                    size.setText(R.string.info_calculating)
                    val handler = Handler {
                        when (it.what) {
                            0 -> {
                                size.text = infoGroup.context.getString(
                                    R.string.info_size,
                                    "${item.size.formatedValue} ${item.size.name}"
                                )
                                true
                            }
                            else -> false
                        }
                    }
                    Environment.threadPool.execute {
                        item.calcSize()
                        handler.sendEmptyMessage(0)
                    }

                    expendInfo()
                } else {
                    collapseInfo()
                }
            }
        }
    }

    private fun forUselessness(holder: PackHolder, file: File) {
        holder.apply {
            //Change the layout, meaning that this file is useless and should be removed.
            name.setText(R.string.info_useless_pack)
            description.setText(R.string.info_useless_pack_remove)
            buttonExpand.apply {
                setImageResource(R.drawable.ic_delete_black)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tooltipText = context.getText(R.string.action_delete)
                }
                setOnClickListener {
                    Environment.Packs.remove(file)
                }
            }
            icon.setImageResource(R.drawable.ic_block_grey)
            infoGroup.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initActions(holder: PackHolder, editable: Boolean, file: File, order: Int) {

        holder.apply {

            actionGroup.visibility = View.INVISIBLE
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED).let { actionGroup.measure(it, it) }
            var isExpend = false
            var isUp = false
            var isLongClick = false
            card.setOnTouchListener { _, event ->
                fun startElevationAnimation(a: ValueAnimator) {
                    a.apply {
                        duration = 150
                        addUpdateListener {
                            card.cardElevation = animatedValue as Float
                        }
                        start()
                    }
                }

                val maxElevation = card.resources.getDimension(R.dimen.padding_normal)
                if (event.action == MotionEvent.ACTION_DOWN) {
                    startElevationAnimation(ObjectAnimator.ofFloat(PackHolder.defaultElevation, maxElevation))
                    Handler().postDelayed({
                        if (!isUp) {
                            isLongClick = true
                            //On Long Click
                            onCardLongClickListener?.invoke(order)

                            if (actionGroup.isVisible)
                                return@postDelayed
                            if (!isCardSelected)
                                selectCard()
                            else
                                unselectCard()
                        } else {
                            isUp = false
                        }
                    }, 300)
                } else if (event.action == MotionEvent.ACTION_UP) {
                    isUp = true
                    startElevationAnimation(ObjectAnimator.ofFloat(maxElevation, PackHolder.defaultElevation))
                    if (!isLongClick) {
                        //On Click
                        onCardClickListener?.invoke(order)

                        if (!actionGroup.isVisible) {
                            isExpend = infoGroup.isVisible
                            if (isExpend) buttonExpand.performClick()
                            showActions(event.x.toInt(),event.y.toInt())
                            actionGroup.visibility = View.VISIBLE

                        } else {
                            hideActions(event.x.toInt(), event.y.toInt())
                            if (isExpend) buttonExpand.performClick()
                        }
                    } else {
                        isLongClick = false
                        isUp = false
                    }
                }

                true
            }
            actionGroup.btn_action_delete.setOnClickListener {
                Environment.Packs.remove(file)
            }
        }
    }

    override fun onBindViewHolder(holder: PackHolder, position: Int) {
        val item = Environment.packs[position]
        if (item.type == Bedrock || item.type == JavaVersion) {
            forPack(holder, item.instance!!)
            initActions(holder, true, item.file, position)
        } else if (item.type == Useless) {
            forUselessness(holder, item.file)
            initActions(holder, false, item.file, position)
        }
    }
}
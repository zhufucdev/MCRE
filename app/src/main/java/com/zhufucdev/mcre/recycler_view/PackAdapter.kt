package com.zhufucdev.mcre.recycler_view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.mcre.Env
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.pack.BedrockPack
import com.zhufucdev.mcre.pack.PackWrapper
import com.zhufucdev.mcre.pack.ResourcesPack
import com.zhufucdev.mcre.pack.ResourcesPack.Type.*
import com.zhufucdev.mcre.utility.MCTextRender
import com.zhufucdev.mcre.utility.measure
import com.zhufucdev.mcre.utility.setCardOnClickListenerWithPosition
import com.zhufucdev.mcre.view.SelectableIconView
import kotlinx.android.synthetic.main.recycler_pack_holder.view.*
import java.io.File

class PackAdapter : RecyclerView.Adapter<PackAdapter.PackHolder>() {
    class PackHolder(itemView: View) : SelectableCardHolder(itemView) {
        val icon = itemView.findViewById<SelectableIconView>(R.id.img_pack_icon)!!
        val name = itemView.findViewById<TextView>(R.id.text_pack_name)!!
        val description = itemView.findViewById<TextView>(R.id.text_pack_description)!!
        val buttonExpand = itemView.findViewById<AppCompatImageView>(R.id.btn_pack_expand)!!
        val infoGroup = itemView.findViewById<FrameLayout>(R.id.layout_info)!!
        val actionGroup = itemView.findViewById<LinearLayout>(R.id.layout_pack_actions)!!
        override val card = itemView.findViewById<CardView>(R.id.outer_card)!!

        fun render(name: String, description: String) {
            MCTextRender(this.name).doRender(name)
            MCTextRender(this.description).doRender(description)
        }

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

        override fun selectCard() {
            icon.select()
            super.selectCard()
        }

        override fun unselectCard() {
            icon.unselect()
            super.unselectCard()
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
                    if (isCardSelected)
                        unselectCard()
                }
                start()
            }
            buttonExpand.isEnabled = false
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
            buttonExpand.isEnabled = true
        }

        fun hideActions() = hideActions(actionGroup.width / 2, actionGroup.height / 2)

        companion object {
            var targetHeight = 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackHolder =
        PackHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_pack_holder, parent, false))

    override fun getItemCount(): Int = Env.packs.size

    private var onCardClickListener: ((Int) -> Boolean)? = null
    private var onCardLongClickListener: ((Int) -> Unit)? = null
    fun setOnCardClickListener(l: (Int) -> Boolean) {
        onCardClickListener = l
    }

    fun setOnCardLongClickListener(l: (Int) -> Unit) {
        onCardLongClickListener = l
    }

    private fun forPack(holder: PackHolder, item: ResourcesPack) {
        holder.apply {
            if (item.icon != null) {
                icon.setImageBitmap(item.icon)
            } else {
                icon.setImageResource(R.drawable.ic_block_grey)
            }
            render(item.header.name.toString(), item.header.description)
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
                    infoGroup.measure()
                    PackHolder.targetHeight = infoGroup.measuredHeight
                }
                //=> Animator
                buttonExpand.pivotX = buttonExpand.width / 2f
                buttonExpand.pivotY = buttonExpand.height / 2f
                if (infoGroup.visibility == View.GONE) {
                    //==> Calculate data
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
                    Env.threadPool.execute {
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
                    Env.Packs.remove(file)
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
            card.setOnClickListener { /*Do Nothing*/ }
            card.setCardOnClickListenerWithPosition({ x, y ->
                if (onCardClickListener?.invoke(order) != false) {
                    if (!isActionGroupShown) {
                        if (!isCardSelected) {
                            isExpend = infoGroup.isVisible
                            if (isExpend) buttonExpand.performClick()
                            showActions(x.toInt(), y.toInt())
                            actionGroup.visibility = View.VISIBLE
                        } else {
                            unselectCard()
                        }
                    } else {
                        hideActions(x.toInt(), y.toInt())
                        if (isExpend) buttonExpand.performClick()
                    }
                }
            }, { _, _ ->
                onCardLongClickListener?.invoke(order)
            })
            icon.setOnClickListener {
                onCardLongClickListener?.invoke(order)
            }
            actionGroup.btn_action_delete.setOnClickListener {
                Env.Packs.remove(file)
            }
        }
    }

    override fun onBindViewHolder(holder: PackHolder, position: Int) {
        val item = Env.packs[position]
        if (item.type == BedrockEdition || item.type == JavaEdition) {
            forPack(holder, item.instance!!)
            initActions(holder, true, item.file, position)
        } else if (item.type == Useless) {
            forUselessness(holder, item.file)
            initActions(holder, false, item.file, position)
        }
    }

    class DiffCallback(val old: List<PackWrapper>, val new: List<PackWrapper>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            (old[oldItemPosition] to new[newItemPosition]).let { it.first.file == it.second.file && it.first.type == it.second.type }

        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition].file == new[newItemPosition].file

    }
}
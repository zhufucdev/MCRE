package com.zhufucdev.mcre.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.project_edit.element.BaseElement
import kotlinx.android.synthetic.main.simple_item.view.*

class SimpleItemView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : this(context, attributes, 0)
    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int)
            : this(context, attributes, defStyleAttr, 0)

    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attributes, defStyleAttr, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.simple_item, this, true)
        val ta = context.obtainStyledAttributes(attributes, R.styleable.SimpleItemView)
        text_title.text = ta.getString(R.styleable.SimpleItemView_title)
        text_subtitle.text = ta.getString(R.styleable.SimpleItemView_subtitle)
        icon.setImageResource(ta.getResourceId(R.styleable.SimpleItemView_icon, android.R.color.transparent))
        ta.recycle()
    }

    var title: CharSequence
        get() = text_title.text
        set(value) {
            text_title.text = value
        }
    var subtitle: CharSequence
        get() = text_subtitle.text
        set(value) {
            text_subtitle.text = value
        }

    fun bind(element: BaseElement) {
        title = element.title.get(context)
        subtitle = element.description.get(context)
    }
}
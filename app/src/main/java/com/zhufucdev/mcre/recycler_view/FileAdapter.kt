package com.zhufucdev.mcre.recycler_view

import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import com.zhufucdev.mcre.R
import com.zhufucdev.mcre.exception.SharedStorageNotAvailableException
import com.zhufucdev.mcre.fragment.FileFragment
import com.zhufucdev.mcre.utility.setCardOnClickListenerWithPosition
import com.zhufucdev.mcre.utility.setOnClickListenerWithPosition
import com.zhufucdev.mcre.view.SelectableIconView
import java.io.File

class FileAdapter(
    var root: File,
    private val useAltButton: Boolean = true,
    private val altButtonText: Int = R.string.action_new_project
) :
    RecyclerView.Adapter<FileAdapter.FileHolder>() {
    class FileHolder(itemView: View) : SelectableCardHolder(itemView) {
        val icon = itemView.findViewById<SelectableIconView>(R.id.img_file_type)!!
        val name = itemView.findViewById<TextView>(R.id.text_file_name)!!
        override val card = itemView.findViewById<CardView>(R.id.card_file)!!
        fun directory() {
            (name.parent as LinearLayout).orientation = LinearLayout.HORIZONTAL
            icon.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                rebuildImageResource(R.drawable.ic_folder_black)
            }
        }

        fun file(type: String) {
            card.isVisible = true
            (name.parent as LinearLayout).orientation = LinearLayout.VERTICAL
            icon.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = context.resources.getDimension(R.dimen.padding_big).toInt()
                    updateMargins(top = margin, left = margin, right = margin, bottom = margin)
                }
                rebuildImageResource(
                    when (type) {
                        "zip" -> R.drawable.ic_zip_box_black
                        else -> R.drawable.ic_file_black
                    }
                )
            }
        }

        private fun standActionButton(disable: Boolean, icon1: Int, text: Int) {
            card.isVisible = true
            (name.parent as LinearLayout).orientation = LinearLayout.HORIZONTAL
            this.icon.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (disable) {
                    imageTintList = context.resources.getColorStateList(android.R.color.darker_gray)
                }
                rebuildImageResource(icon1)
            }
            name.setText(text)
        }

        fun upperLevel(disable: Boolean = false) {
            standActionButton(disable, R.drawable.ic_arrow_back, R.string.action_back)
        }

        fun altButton(disable: Boolean = false, text: Int) {
            standActionButton(disable, R.drawable.ic_add_white, text)
        }

        override fun selectCard() {
            if (isCardSelected) return
            icon.select()
            super.selectCard()
        }

        override fun unselectCard() {
            if (!isCardSelected) return
            icon.unselect()
            super.unselectCard()
        }
    }

    private fun files() = root.listFiles()?.let { arrayOfFiles ->
        val files = arrayListOf<File>()
        val directories = arrayListOf<File>()
        arrayOfFiles.forEach {
            if (it.isFile) files.add(it)
            else directories.add(it)
        }
        files.sort()
        directories.apply {
            sort()
            if (size % 2 != 0 && files.isNotEmpty()) {
                add(File("[EMPTY_POSITION]"))
            }
            addAll(files)
        }
        return@let directories
    }

    var list = files() ?: throw SharedStorageNotAvailableException()
        private set

    fun refresh() {
        files()?.apply { list = this }
        selectedFile = null
        notifyDataSetChanged()
        FileFragment.showing = root
    }

    var selectedFile: File? = null
        private set

    // Listeners
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(l: (Int) -> Unit) {
        onItemClickListener = l
    }

    private var onDirectoryChangedListener: (() -> Unit)? = null
    fun setOnDirectoryChangedListener(l: () -> Unit) {
        onDirectoryChangedListener = l
    }

    private var onNewProjectCreatedListener: ((Float, Float) -> Unit)? = null
    fun setOnAltButtonClickListener(l: (Float, Float) -> Unit) {
        onNewProjectCreatedListener = l
    }

    // UI
    fun giveUpperLevelListenersTo(card: CardView, disable: Boolean) {
        if (!disable)
            card.setCardOnClickListenerWithPosition (onCardClickListener = { _, _ ->
                root = root.parentFile!!
                refresh()
                onDirectoryChangedListener?.invoke()
            })
        else
            card.setOnClickListenerWithPosition()
    }

    fun giveAltButtonListenerTo(card: CardView) = card.setCardOnClickListenerWithPosition({ x, y ->
        onNewProjectCreatedListener?.invoke(x, y)
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder =
        FileHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_file_holder, parent, false))

    override fun getItemCount(): Int = list.size + 2
    var newProjectCard: CardView? = null
        private set

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        if (position <= 1) {
            when (position) {
                0 -> {
                    holder.apply {
                        val disable = root == Environment.getExternalStorageDirectory()
                        upperLevel(disable)
                        giveUpperLevelListenersTo(card, disable)
                    }
                }
                1 -> {
                    holder.altButton(!useAltButton, altButtonText)
                    newProjectCard = holder.card
                    holder.card.transitionName = "shared"
                    giveAltButtonListenerTo(holder.card)
                }
            }
        } else {
            val item = list[position - 2]
            holder.apply {
                if (item.name == "[EMPTY_POSITION]") {
                    card.isVisible = false
                    directory()
                } else {
                    unselectCard()
                    card.isVisible = true
                    name.text = item.name
                    if (item.isDirectory) {
                        directory()
                        fun notifySelect() {
                            selectedFile = if (selectedFile == item) {
                                unselectCard()
                                null
                            } else {
                                selectCard()
                                item
                            }
                            onItemClickListener?.invoke(position)
                        }
                        icon.setOnClickListener {
                            notifySelect()
                        }
                        card.setCardOnClickListenerWithPosition({ _, _ ->
                            // When card of directory clicked
                            if (selectedFile == item) {
                                // If selected
                                // => Unselect it
                                unselectCard()
                                selectedFile = null
                            } else {
                                // If not selected
                                // => Enter the directory
                                root = item
                                refresh()
                                onDirectoryChangedListener?.invoke()
                            }
                        }, { _, _ ->
                            notifySelect()
                        })
                    } else {
                        file(item.extension)
                        fun notifySelect() {
                            selectedFile = if (selectedFile == item) {
                                unselectCard()
                                null
                            } else {
                                selectCard()
                                item
                            }
                            onItemClickListener?.invoke(position)
                        }
                        card.setCardOnClickListenerWithPosition({ _, _ ->
                            notifySelect()
                        })
                        icon.setOnClickListener {
                            notifySelect()
                        }
                    }
                }
            }
        }
    }
}
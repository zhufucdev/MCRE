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
import com.zhufucdev.mcre.utility.setCardOnClickListenerWithPosition
import com.zhufucdev.mcre.views.SelectableIconView
import java.io.File

class FileAdapter(var root: File = Environment.getExternalStorageDirectory()) :
    RecyclerView.Adapter<FileAdapter.FileHolder>() {
    class FileHolder(itemView: View) : SelectableCardHolder(itemView) {
        val icon = itemView.findViewById<SelectableIconView>(R.id.img_file_type)
        val name = itemView.findViewById<TextView>(R.id.text_file_name)
        override val card = itemView.findViewById<CardView>(R.id.card_file)
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

        fun upperLevel(disable: Boolean = false) {
            (name.parent as LinearLayout).orientation = LinearLayout.HORIZONTAL
            icon.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (disable) {
                    imageTintList = context.resources.getColorStateList(android.R.color.darker_gray)
                }
                rebuildImageResource(R.drawable.ic_arrow_back)
            }
            name.setText(R.string.action_back)
        }

        fun newProject() {
            (name.parent as LinearLayout).orientation = LinearLayout.HORIZONTAL
            icon.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                rebuildImageResource(R.drawable.ic_add_white)
            }
            name.setText(R.string.action_new_project)
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

    private var list = files() ?: listOf<File>()

    fun refresh() {
        files()?.apply { list = this }
        selectedFile = null
        notifyDataSetChanged()
    }

    var selectedFile: File? = null
        private set

    // Listeners
    private var onDrawnListener: (() -> Unit)? = null

    fun setOnDrawnListener(l: () -> Unit) {
        onDrawnListener = l
    }

    private var onItemClickListener: ((Int) -> Unit)? = null
    fun setOnItemClickListener(l: (Int) -> Unit) {
        onItemClickListener = l
    }

    private var onDirectoryChangedListener: (() -> Unit)? = null
    fun setOnDirectoryChangedListener(l: () -> Unit) {
        onDirectoryChangedListener = l
    }

    // UI
    fun giveUpperLevelListenersTo(card: CardView, disable: Boolean) =
        card.setCardOnClickListenerWithPosition(onCardClickListener = if (!disable) { _, _ ->
            root = root.parentFile
            refresh()
            onDirectoryChangedListener?.invoke()
        } else null as ((Float, Float) -> Unit)?) // This cast is USEFUL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder =
        FileHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_file_holder, parent, false))

    override fun getItemCount(): Int = list.size + 2
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
                    holder.newProject()
                }
            }
        } else {
            val item = list[position - 2]
            holder.apply {
                if (item.name == "[EMPTY_POSITION]") {
                    card.isVisible = false
                    directory()
                } else {
                    card.isVisible = true
                    name.text = if (!item.isHidden) item.nameWithoutExtension else item.name
                    if (item.isDirectory) {
                        directory()
                        fun notifySelect() {
                            if (selectedFile == item) {
                                unselectCard()
                                selectedFile = null
                            } else {
                                selectCard()
                                selectedFile = item
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
                        card.setCardOnClickListenerWithPosition({ _, _ ->
                            if (selectedFile == item) {
                                unselectCard()
                                selectedFile = null
                            } else {
                                selectCard()
                                selectedFile = item
                            }
                            onItemClickListener?.invoke(position)
                        })
                    }
                }
            }
            if (position == itemCount - 1) {
                onDrawnListener?.invoke()
            }
        }
    }
}
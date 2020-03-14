package com.zhufucdev.mcre.utility

import com.zhufucdev.mcre.R

object TextUtil {
    const val KEY = '§'
    const val END = "§r"

    enum class TextColor(val code: String, val isColor: Boolean = true, val resourceID: Int = -1, val backgroundID: Int = -1) {
        BLACK("${KEY}0", resourceID = R.color.mcBlack, backgroundID = R.color.mcWhite),
        DARK_BLUE("${KEY}1", resourceID = R.color.mcDarkBlue),
        DARK_GREEN("${KEY}2", resourceID = R.color.mcDarkGreen), DARK_AQUA("${KEY}3", resourceID = R.color.mcDarkAqua),
        DARK_RED("${KEY}4", resourceID = R.color.mcDarkRed), DARK_PURPLE("${KEY}5", resourceID = R.color.mcDarkPurple),
        GOLD("${KEY}6", resourceID = R.color.mcGold), GRAY("${KEY}7", resourceID = R.color.mcGray),
        DARK_GRAY("${KEY}8", resourceID = R.color.mcDarkGray), BLUE("${KEY}9", resourceID = R.color.mcBlue),
        GREEN("${KEY}a", resourceID = R.color.mcGreen), AQUA("${KEY}b", resourceID = R.color.mcAqua),
        RED("${KEY}c", resourceID = R.color.mcRed), LIGHT_PURPLE("${KEY}d", resourceID = R.color.mcLightPurple),
        YELLOW("${KEY}e", resourceID = R.color.mcYellow),
        WHITE("${KEY}f", resourceID = R.color.mcWhite, backgroundID = R.color.mcWhiteBackground),
        BOLD("${KEY}l", false), UNDERLINED("${KEY}n", false), END("${KEY}r", false),
        RANDOM("${KEY}k", false), STRUCK_THROUGH("${KEY}m", false), ITALIC("${KEY}o", false);

        val isColorOrEnd: Boolean get() = isColor || this == END
        override fun toString(): String = code

        companion object {
            fun of(code: String) = values().first { it.code == code }
        }
    }

    fun getColoredText(
        t: String,
        color: TextColor,
        bold: Boolean = false,
        underlined: Boolean = false,
        italic: Boolean = false
    ): String {
        val sb = StringBuilder(color.code)
        if (italic) {
            sb.append(TextColor.ITALIC)
        }
        if (bold) {
            sb.append(TextColor.BOLD)
        }
        if (underlined) {
            sb.append(TextColor.UNDERLINED)
        }
        sb.append(t)
        return sb.toString()
    }
}
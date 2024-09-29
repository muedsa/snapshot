package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.FontFeature
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.DecorationStyle
import org.jetbrains.skia.paragraph.Shadow

data class TextStyle (
    var color: Int? = null,
    var foreground: Paint? = null,
    var background: Paint? = null,
    var decorationStyle: DecorationStyle? = null,
    var fontStyle: FontStyle? = null,
    var shadows: List<Shadow>? = null,
    var fontFeatures: List<FontFeature>? = null,
    var fontSize: Float? = null,
    var fontFamilies: List<String>? = null,
    var height: Float? = null,
    var topRatio: Float? = null,
    var letterSpacing: Float? = null,
    var wordSpacing: Float? = null,
    var typeface: Typeface? = null,
    var locale: String? = null,
    var baselineMode: BaselineMode? = null
) {

    fun isEmpty(): Boolean =
        color == null && foreground == null && background == null && decorationStyle == null
                && fontStyle == null && shadows == null && fontFeatures == null && fontSize == null
                && fontFamilies == null && height == null && topRatio == null && letterSpacing == null
                && wordSpacing == null && typeface == null && locale == null && baselineMode == null

    fun mergeFrom(style: TextStyle): TextStyle {
        return copy(
            color = color ?: style.color,
            foreground = foreground ?: style.foreground,
            background = background ?: style.background,
            decorationStyle = decorationStyle ?: style.decorationStyle,
            fontStyle = fontStyle ?: style.fontStyle,
            shadows = shadows ?: style.shadows,
            fontFeatures = fontFeatures ?: style.fontFeatures,
            fontSize = fontSize ?: style.fontSize,
            fontFamilies = fontFamilies ?: style.fontFamilies,
            height = height ?: style.height,
            topRatio = topRatio ?: style.topRatio,
            letterSpacing = letterSpacing ?: style.letterSpacing,
            wordSpacing = wordSpacing ?: style.wordSpacing,
            typeface = typeface ?: style.typeface,
            locale = locale ?: style.locale,
            baselineMode = baselineMode ?: style.baselineMode
        )
    }

    fun toSkikoTextStyle(): org.jetbrains.skia.paragraph.TextStyle? {
        if (isEmpty()) {
            return null
        }
        val textStyle = org.jetbrains.skia.paragraph.TextStyle()
        color?.let { textStyle.color = it }
        foreground?.let { textStyle.foreground = it }
        background?.let { textStyle.background = it }
        decorationStyle?.let { textStyle.decorationStyle = it }
        fontStyle?.let { textStyle.fontStyle = it }
        shadows?.forEach { textStyle.addShadow(it) }
        fontFeatures?.forEach { textStyle.addFontFeature(it) }
        fontSize?.let { textStyle.fontSize = it }
        fontFamilies?.let { textStyle.fontFamilies = it.toTypedArray() }
        height?.let { textStyle.height = it }
        topRatio?.let { textStyle.topRatio = it }
        letterSpacing?.let { textStyle.letterSpacing = it }
        wordSpacing?.let { textStyle.wordSpacing = it }
        typeface?.let { textStyle.typeface = it }
        locale?.let { textStyle.locale = it }
        baselineMode?.let { textStyle.baselineMode = it }
        return textStyle
    }
}
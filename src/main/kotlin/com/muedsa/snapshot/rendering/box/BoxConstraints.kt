package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Size
import kotlin.math.max


data class BoxConstraints(
    val minWidth: Float = 0f,
    val maxWidth: Float = Float.POSITIVE_INFINITY,
    val minHeight: Float = 0f,
    val maxHeight: Float = Float.POSITIVE_INFINITY
) {
    init {
        assert(maxWidth <= Float.POSITIVE_INFINITY)
        assert(maxHeight <= Float.POSITIVE_INFINITY)
        assert(minWidth in 0f..maxWidth)
        assert(minHeight in 0f..maxHeight)
    }

    fun copyWith(
        minWidth: Float? = null,
        maxWidth: Float? = null,
        minHeight: Float? = null,
        maxHeight: Float? = null
    ): BoxConstraints = BoxConstraints(
        minWidth = minWidth ?: this.minWidth,
        maxWidth = maxWidth ?: this.maxWidth,
        minHeight = minHeight ?: this.minHeight,
        maxHeight = maxHeight ?: this.maxHeight
    )

    fun deflate(edges: EdgeInsets): BoxConstraints {
        val horizontal = edges.horizontal
        val vertical = edges.vertical
        val deflatedMinWidth = max(0f, minWidth - horizontal)
        val deflatedMinHeight = max(0f, minHeight - vertical)
        return BoxConstraints(
            minWidth = deflatedMinWidth,
            maxWidth = max(deflatedMinWidth, maxWidth - horizontal),
            minHeight = deflatedMinHeight,
            maxHeight = max(deflatedMinHeight, maxHeight - vertical)
        )
    }

    fun loosen(): BoxConstraints = BoxConstraints(maxWidth = maxWidth, maxHeight = maxHeight)

    fun enforce(constraints: BoxConstraints): BoxConstraints = BoxConstraints(
        minWidth = minWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
        maxWidth = maxWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
        minHeight = minHeight.coerceIn(constraints.minHeight, constraints.maxHeight),
        maxHeight = maxHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
    )

    fun tighten(width: Float? = null, height: Float? = null): BoxConstraints = BoxConstraints(
        minWidth = width?.coerceIn(minWidth, maxWidth) ?: minWidth,
        maxWidth = width?.coerceIn(minWidth, maxWidth) ?: maxWidth,
        minHeight = height?.coerceIn(minHeight, maxHeight) ?: minHeight,
        maxHeight = height?.coerceIn(minHeight, maxHeight) ?: maxHeight
    )

    fun flipped(): BoxConstraints = BoxConstraints(
        minWidth = minWidth,
        maxWidth = maxWidth,
        minHeight = minHeight,
        maxHeight = maxHeight
    )

    fun widthConstraints(): BoxConstraints = BoxConstraints(minWidth = minWidth, maxWidth = maxWidth)

    fun heightConstraints(): BoxConstraints = BoxConstraints(minHeight = minHeight, maxHeight = maxHeight)


    fun constrainWidth(width: Float = Float.POSITIVE_INFINITY): Float = width.coerceIn(minWidth, maxWidth)

    fun constrainHeight(height: Float = Float.POSITIVE_INFINITY): Float = height.coerceIn(minHeight, maxHeight)

    fun constrain(size: Size): Size = Size(constrainWidth(size.width), constrainHeight(size.height))

    fun constrainDimensions(width: Float, height: Float) = Size(constrainWidth(width), constrainHeight(height))

    fun constrainSizeAndAttemptToPreserveAspectRatio(size: Size): Size {
        if (isTight) {
            return smallest
        }
        var width = size.width
        var height = size.height
        assert(width > 0)
        assert(height > 0)
        val aspectRatio = width / height

        if (width > maxWidth) {
            width = maxWidth
            height = width / aspectRatio
        }

        if (height > maxHeight) {
            height = maxHeight
            width = height * aspectRatio
        }

        if (width < minWidth) {
            width = minWidth
            height = width / aspectRatio
        }

        if (height < minHeight) {
            height = minHeight
            width = height * aspectRatio
        }

        return Size(width, height)
    }

    override fun toString(): String {
        return "BoxConstraints(minWidth=$minWidth, maxWidth=$maxWidth, minHeight=$minHeight, maxHeight=$maxHeight)"
    }

    val biggest: Size by lazy {
        Size(constrainWidth(), constrainHeight())
    }

    val smallest: Size by lazy {
        Size(constrainWidth(0f), constrainHeight(0f))
    }

    val hasTightWidth: Boolean by lazy {
        minWidth >= maxWidth
    }

    val hasTightHeight: Boolean by lazy {
        minHeight >= maxHeight
    }

    val isTight: Boolean by lazy {
        hasTightWidth && hasTightHeight
    }

    val hasBoundedWidth: Boolean by lazy {
        maxWidth < Float.POSITIVE_INFINITY
    }

    val hasBoundedHeight: Boolean by lazy {
        maxHeight < Float.POSITIVE_INFINITY
    }

    val hasInfiniteWidth: Boolean by lazy {
        minWidth >= Float.POSITIVE_INFINITY
    }

    val hasInfiniteHeight: Boolean by lazy {
        minHeight >= Float.POSITIVE_INFINITY
    }

    val isNormalized: Boolean by lazy {
        minWidth in 0f..maxWidth && minHeight in 0f..maxHeight
    }


    companion object {

        @JvmStatic
        fun tight(size: Size) = BoxConstraints(
            minWidth = size.width,
            maxWidth = size.width,
            minHeight = size.height,
            maxHeight = size.height
        )

        @JvmStatic
        fun tightFor(width: Float? = null, height: Float? = null) = BoxConstraints(
            minWidth = width ?: 0f,
            maxWidth = width ?: Float.POSITIVE_INFINITY,
            minHeight = height ?: 0f,
            maxHeight = height ?: Float.POSITIVE_INFINITY
        )

        @JvmStatic
        fun tightForFinite(
            width: Float = Float.POSITIVE_INFINITY,
            height: Float = Float.POSITIVE_INFINITY
        ) = BoxConstraints(
            minWidth = if (width != Float.POSITIVE_INFINITY) width else 0f,
            maxWidth = if (width != Float.POSITIVE_INFINITY) width else Float.POSITIVE_INFINITY,
            minHeight = if (height != Float.POSITIVE_INFINITY) height else 0f,
            maxHeight = if (height != Float.POSITIVE_INFINITY) height else Float.POSITIVE_INFINITY,
        )

        @JvmStatic
        fun loose(size: Size) = BoxConstraints(
            minWidth = 0f,
            maxWidth = size.width,
            minHeight = 0f,
            maxHeight = size.height
        )

        @JvmStatic
        fun expand(width: Float? = null, height: Float? = null) = BoxConstraints(
            minWidth = width ?: Float.POSITIVE_INFINITY,
            maxWidth = width ?: Float.POSITIVE_INFINITY,
            minHeight = height ?: Float.POSITIVE_INFINITY,
            maxHeight = height ?: Float.POSITIVE_INFINITY
        )
    }
}
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.stack.RelativeRect
import com.muedsa.snapshot.rendering.stack.StackParentData
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.Direction

inline fun Stack.Positioned(
    left: Float? = null,
    top: Float? = null,
    right: Float? = null,
    bottom: Float? = null,
    width: Float? = null,
    height: Float? = null,
    content: Positioned.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Positioned(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            width = width,
            height = height,
        ).apply(content)
    )
}

class Positioned(
    var left: Float? = null,
    var top: Float? = null,
    var right: Float? = null,
    var bottom: Float? = null,
    var width: Float? = null,
    var height: Float? = null,
) : ParentDataWidget() {

    init {
        require(left == null || right == null || width == null) {
            "At most two of left, right, and width may be non-null"
        }
        require(top == null || bottom == null || height == null) {
            "At most two of top, bottom, and height may be non-null"
        }
    }

    override fun applyParentData(renderBox: RenderBox) {
        require(renderBox.parentData is StackParentData) { "renderBox.parentData must be StackParentData" }
        val parentData: StackParentData = renderBox.parentData as StackParentData
        if (parentData.left != left) {
            parentData.left = left
        }
        if (parentData.top != top) {
            parentData.top = top
        }
        if (parentData.right != right) {
            parentData.right = right
        }
        if (parentData.bottom != bottom) {
            parentData.bottom = bottom
        }
        if (parentData.width != width) {
            parentData.width = width
        }
        if (parentData.height != height) {
            parentData.height = height
        }
    }

    companion object {

        @JvmStatic
        fun fromRect(rect: Rect): Positioned = Positioned(
            left = rect.left,
            top = rect.top,
            right = null,
            bottom = null,
            width = rect.width,
            height = rect.height,
        )

        @JvmStatic
        fun fromRelativeRect(rect: RelativeRect): Positioned = Positioned(
            left = rect.left,
            top = rect.top,
            right = rect.right,
            bottom = rect.bottom,
            width = null,
            height = null,
        )

        @JvmStatic
        fun fill(
            left: Float = 0f,
            top: Float = 0f,
            right: Float = 0f,
            bottom: Float = 0f,
        ): Positioned = Positioned(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            width = null,
            height = null,
        )

        @JvmStatic
        fun directional(
            textDirection: Direction,
            start: Float? = null,
            top: Float? = null,
            end: Float? = null,
            bottom: Float? = null,
            width: Float? = null,
            height: Float? = null,
        ): Positioned {
            val left: Float?
            val right: Float?
            when (textDirection) {
                Direction.RTL -> {
                    left = end
                    right = start
                }

                Direction.LTR -> {
                    left = start
                    right = end
                }
            }
            return Positioned(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                width = width,
                height = height,
            )
        }
    }
}

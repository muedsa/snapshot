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
    buildChild(
        widget = Positioned(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            width = width,
            height = height,
            parent = this
        ),
        content = content
    )
}

class Positioned(
    val left: Float? = null,
    val top: Float? = null,
    val right: Float? = null,
    val bottom: Float? = null,
    val width: Float? = null,
    val height: Float? = null,
    parent: Widget? = null,
) : ParentDataWidget(parent = parent) {

    init {
        assert(left == null || right == null || width == null)
        assert(top == null || bottom == null || height == null)
    }

    override fun applyParentData(renderBox: RenderBox) {
        assert(renderBox.parentData is StackParentData)
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
        fun fromRect(rect: Rect, parent: Widget? = null): Positioned = Positioned(
            left = rect.left,
            top = rect.top,
            right = null,
            bottom = null,
            width = rect.width,
            height = rect.height,
            parent = parent
        )

        @JvmStatic
        fun fromRelativeRect(rect: RelativeRect, parent: Widget? = null): Positioned = Positioned(
            left = rect.left,
            top = rect.top,
            right = rect.right,
            bottom = rect.bottom,
            width = null,
            height = null,
            parent = parent
        )

        @JvmStatic
        fun fill(
            left: Float = 0f,
            top: Float = 0f,
            right: Float = 0f,
            bottom: Float = 0f,
            parent: Widget? = null,
        ): Positioned = Positioned(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            width = null,
            height = null,
            parent = parent
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
            parent: Widget? = null,
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
                parent = parent
            )
        }
    }
}
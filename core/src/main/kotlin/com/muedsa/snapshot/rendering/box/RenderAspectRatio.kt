package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Size

/** 按宽高比从父约束中确定尺寸，并给子节点传入紧约束。 */
class RenderAspectRatio(
    var aspectRatio: Float,
) : RenderSingleChildBox() {

    override fun performLayout() {
        require(aspectRatio.isFinite() && aspectRatio > 0f) {
            "aspectRatio must be finite and positive"
        }
        val constraints = definiteConstraints
        require(constraints.hasBoundedWidth || constraints.hasBoundedHeight) {
            "AspectRatio needs a finite maximum width or height"
        }

        val resolvedSize = if (constraints.isTight) {
            constraints.smallest
        } else {
            var width = constraints.maxWidth
            var height = constraints.maxHeight

            if (width.isFinite()) {
                height = width / aspectRatio
            } else {
                width = height * aspectRatio
            }

            if (height > constraints.maxHeight) {
                height = constraints.maxHeight
                width = height * aspectRatio
            }
            if (width < constraints.minWidth) {
                width = constraints.minWidth
                height = width / aspectRatio
            }
            if (height < constraints.minHeight) {
                height = constraints.minHeight
                width = height * aspectRatio
            }

            constraints.constrainDimensions(width, height)
        }
        require(resolvedSize.isFinite) { "AspectRatio resolved to a non-finite size: $resolvedSize" }

        size = resolvedSize
        child?.layout(BoxConstraints.tight(resolvedSize))
    }
}

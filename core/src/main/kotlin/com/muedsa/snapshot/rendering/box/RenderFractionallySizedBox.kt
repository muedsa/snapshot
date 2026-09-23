package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Size

/** 按父约束的最大尺寸给子节点指定宽度或高度，自身仍受父约束限制。 */
class RenderFractionallySizedBox(
    var widthFactor: Float? = null,
    var heightFactor: Float? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
) : RenderAligningBox(alignment = alignment) {

    override fun performLayout() {
        val parentConstraints = definiteConstraints
        val childWidth = fractionalDimension(parentConstraints.maxWidth, widthFactor, "widthFactor")
        val childHeight = fractionalDimension(parentConstraints.maxHeight, heightFactor, "heightFactor")
        val childConstraints = BoxConstraints(
            minWidth = childWidth ?: parentConstraints.minWidth,
            maxWidth = childWidth ?: parentConstraints.maxWidth,
            minHeight = childHeight ?: parentConstraints.minHeight,
            maxHeight = childHeight ?: parentConstraints.maxHeight,
        )

        val currentChild = child
        if (currentChild != null) {
            currentChild.layout(childConstraints)
            size = parentConstraints.constrain(currentChild.definiteSize)
            alignChild()
        } else {
            size = parentConstraints.constrain(Size(childWidth ?: 0f, childHeight ?: 0f))
        }
    }

    private fun fractionalDimension(maximum: Float, factor: Float?, name: String): Float? {
        if (factor == null) return null
        require(factor.isFinite() && factor >= 0f) { "$name must be finite and non-negative" }
        require(maximum.isFinite()) { "$name needs a finite maximum size" }
        val dimension = maximum * factor
        require(dimension.isFinite()) { "$name resolved to a non-finite size" }
        return dimension
    }
}

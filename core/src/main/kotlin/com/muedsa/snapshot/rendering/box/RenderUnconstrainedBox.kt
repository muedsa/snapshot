package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.Axis

/** 解除子节点的父约束；[constrainedAxis] 指定需要保留约束的方向。 */
class RenderUnconstrainedBox(
    var constrainedAxis: Axis? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
) : RenderAligningBox(alignment = alignment) {

    override fun performLayout() {
        val parentConstraints = definiteConstraints
        val childConstraints = when (constrainedAxis) {
            Axis.HORIZONTAL -> parentConstraints.widthConstraints()
            Axis.VERTICAL -> parentConstraints.heightConstraints()
            null -> BoxConstraints()
        }

        val currentChild = child
        if (currentChild == null) {
            size = parentConstraints.smallest
            return
        }

        currentChild.layout(childConstraints)
        val childSize = currentChild.definiteSize
        require(childSize.isFinite) { "UnconstrainedBox child must have a finite size: $childSize" }
        size = parentConstraints.constrain(childSize)
        require(definiteSize.isFinite) { "UnconstrainedBox resolved to a non-finite size: $definiteSize" }
        alignChild()
    }
}

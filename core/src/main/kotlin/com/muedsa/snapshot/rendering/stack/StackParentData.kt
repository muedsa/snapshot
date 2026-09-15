package com.muedsa.snapshot.rendering.stack

import com.muedsa.snapshot.rendering.box.ContainerBoxParentData

class StackParentData : ContainerBoxParentData() {
    /**
     * 子节点上边缘相对堆叠容器上边缘的内缩距离。
     */
    var top: Float? = null

    /**
     * 子节点右边缘相对堆叠容器右边缘的内缩距离。
     */
    var right: Float? = null

    /**
     * 子节点下边缘相对堆叠容器下边缘的内缩距离。
     */
    var bottom: Float? = null

    /**
     * 子节点左边缘相对堆叠容器左边缘的内缩距离。
     */
    var left: Float? = null

    /**
     * 子节点的宽度。
     *
     * 当 [left] 与 [right] 均不为空时忽略此值。
     */
    var width: Float? = null


    /**
     * 子节点的高度。
     *
     * 当 [top] 与 [bottom] 均不为空时忽略此值。
     */
    var height: Float? = null

    /**
     * 以 [RelativeRect] 的形式获取或设置当前位置值。
     */
    var rect: RelativeRect
        get() = RelativeRect.fromLTRB(left!!, top!!, right!!, bottom!!)
        set(value) {
            top = value.top
            right = value.right
            bottom = value.bottom
            left = value.left
        }


    /**
     * 当前子节点是否属于定位子节点。
     *
     * 只要 [top]、[right]、[bottom]、[left]、[width] 或 [height] 中任一值不为空，
     * 子节点就属于定位子节点。定位子节点不参与确定堆叠容器的尺寸，而是相对容器中的
     * 非定位子节点进行放置。
     */
    val isPositioned: Boolean
        get() = top != null || right != null || bottom != null || left != null || width != null || height != null

}

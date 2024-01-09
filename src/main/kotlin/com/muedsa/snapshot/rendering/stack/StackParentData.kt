package com.muedsa.snapshot.rendering.stack

import com.muedsa.snapshot.rendering.box.ContainerBoxParentData

class StackParentData : ContainerBoxParentData() {
    /**
     * The distance by which the child's top edge is inset from the top of the stack.
     */
    var top: Float? = null

    /**
     * The distance by which the child's right edge is inset from the right of the stack.
     */
    var right: Float? = null

    /**
     * The distance by which the child's bottom edge is inset from the bottom of the stack.
     */
    var bottom: Float? = null

    /**
     * The distance by which the child's left edge is inset from the left of the stack.
     */
    var left: Float? = null

    /**
     * The child's width.
     *
     * Ignored if both top and bottom are non-null.
     */
    var width: Float? = null


    /**
     * The child's height.
     *
     * Ignored if both top and bottom are non-null.
     */
    var height: Float? = null

    /**
     * Get or set the current values in terms of a RelativeRect object.
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
     *  Whether this child is considered positioned.
     *
     *  A child is positioned if any of the top, right, bottom, or left properties
     *  are non-null. Positioned children do not factor into determining the size
     *  of the stack but are instead placed relative to the non-positioned
     *  children in the stack.
     */
    val isPositioned: Boolean
        get() = top != null || right != null || bottom != null || left != null || width != null || height != null

}
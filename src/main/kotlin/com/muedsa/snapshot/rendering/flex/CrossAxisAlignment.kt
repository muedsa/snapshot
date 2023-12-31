package com.muedsa.snapshot.rendering.flex

enum class CrossAxisAlignment {
    // Place the children with their start edge aligned with the start side of
    // the cross axis.
    //
    // For example, in a column (a flex with a vertical axis) whose
    // [TextDirection] is [TextDirection.ltr], this aligns the left edge of the
    // children along the left edge of the column.
    //
    // If this value is used in a horizontal direction, a [TextDirection] must be
    // available to determine if the start is the left or the right.
    //
    // If this value is used in a vertical direction, a [VerticalDirection] must be
    // available to determine if the start is the top or the bottom.
    START,

    // Place the children as close to the end of the cross axis as possible.
    //
    // For example, in a column (a flex with a vertical axis) whose
    // [TextDirection] is [TextDirection.ltr], this aligns the right edge of the
    // children along the right edge of the column.
    //
    // If this value is used in a horizontal direction, a [TextDirection] must be
    // available to determine if the end is the left or the right.
    //
    // If this value is used in a vertical direction, a [VerticalDirection] must be
    // available to determine if the end is the top or the bottom.
    END,

    // Place the children so that their centers align with the middle of the
    // cross axis.
    //
    // This is the default cross-axis alignment.
    CENTER,

    // Require the children to fill the cross axis.
    //
    // This causes the constraints passed to the children to be tight in the
    // cross axis.
    STRETCH,

    // Place the children along the cross axis such that their baselines match.
    //
    // Because baselines are always horizontal, this alignment is intended for
    // horizontal main axes. If the main axis is vertical, then this value is
    // treated like [start].
    //
    // For horizontal main axes, if the minimum height constraint passed to the
    // flex layout exceeds the intrinsic height of the cross axis, children will
    // be aligned as close to the top as they can be while honoring the baseline
    // alignment. In other words, the extra space will be below all the children.
    //
    // Children who report no baseline will be top-aligned.
    BASELINE,
}
package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.snapshot.paint.decoration.Decoration
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.DecorationPosition
import com.muedsa.snapshot.rendering.box.RenderBox

inline fun ChildSlot.Container(
    alignment: BoxAlignment? = null,
    padding: EdgeInsets? = null,
    color: Int? = null,
    decoration: Decoration? = null,
    foregroundDecoration: Decoration? = null,
    width: Float? = null,
    height: Float? = null,
    constraints: BoxConstraints? = null,
    margin: EdgeInsets? = null,
    transform: Matrix44CMO? = null,
    transformAlignment: BoxAlignment? = null,
    clipBehavior: ClipBehavior = ClipBehavior.NONE,
    content: Container.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Container(
            alignment = alignment,
            padding = padding,
            color = color,
            decoration = decoration,
            foregroundDecoration = foregroundDecoration,
            width = width,
            height = height,
            constraints = constraints,
            margin = margin,
            transform = transform,
            transformAlignment = transformAlignment,
            clipBehavior = clipBehavior,
        ).apply(content)
    )
}


class Container(
    var alignment: BoxAlignment? = null,
    var padding: EdgeInsets? = null,
    var color: Int? = null,
    var decoration: Decoration? = null,
    var foregroundDecoration: Decoration? = null,
    var width: Float? = null,
    var height: Float? = null,
    var constraints: BoxConstraints? = null,
    var margin: EdgeInsets? = null,
    var transform: Matrix44CMO? = null,
    var transformAlignment: BoxAlignment? = null,
    var clipBehavior: ClipBehavior = ClipBehavior.NONE,
) : SingleChildWidget() {

    init {
        check(decoration != null || clipBehavior == ClipBehavior.NONE)
        check(color == null || decoration == null) {
            """
                Cannot provide both a color and a decoration
                To provide both, use `decoration = BoxDecoration(color = color)`.
            """.trimIndent()
        }
        constraints = if (width != null || height != null)
            constraints?.tighten(width = width, height = height) ?: BoxConstraints.tightFor(
                width = width,
                height = height
            )
        else constraints
    }

    private fun composeWidget(): Widget {
        var current: Widget? = child
        val snapshotAlignment = alignment
        val snapshotPadding = padding
        val snapshotColor = color
        val snapshotClipBehavior = clipBehavior
        val snapshotDecoration = decoration
        val snapshotForegroundDecoration = foregroundDecoration
        val snapshotConstraints = constraints
        val snapshotTransform = transform
        val snapshotTransformAlignment = transformAlignment

        if (current == null && (constraints == null || constraints!!.isTight)) {
            current = com.muedsa.snapshot.widget.LimitedBox(maxWidth = 0f, maxHeight = 0f)
                .apply {
                    attach(com.muedsa.snapshot.widget.ConstrainedBox(constraints = BoxConstraints.expand()))
                }
        } else if (snapshotAlignment != null) {
            current = com.muedsa.snapshot.widget.Align(alignment = snapshotAlignment)
                .apply { current?.let { attach(it) } }
        }

        if (snapshotPadding != null) {
            current = com.muedsa.snapshot.widget.Padding(padding = snapshotPadding)
                .apply { current?.let { attach(it) } }
        }

        if (snapshotColor != null) {
            current = com.muedsa.snapshot.widget.ColoredBox(color = snapshotColor)
                .apply { current?.let { attach(it) } }
        }


        if (snapshotClipBehavior != ClipBehavior.NONE) {
            checkNotNull(snapshotDecoration)
            current = com.muedsa.snapshot.widget.ClipPath(
                clipper = { snapshotDecoration.getClipPath(Offset.ZERO combine it) },
                clipBehavior = snapshotClipBehavior,
            ).apply { current?.let { attach(it) } }
        }

        if (snapshotDecoration != null) {
            current = com.muedsa.snapshot.widget.DecoratedBox(
                decoration = snapshotDecoration,
                position = DecorationPosition.BACKGROUND,
            ).apply { current?.let { attach(it) } }
        }

        if (snapshotForegroundDecoration != null) {
            current = com.muedsa.snapshot.widget.DecoratedBox(
                decoration = snapshotForegroundDecoration,
                position = DecorationPosition.FOREGROUND,
            ).apply { current?.let { attach(it) } }
        }

        if (snapshotConstraints != null) {
            current = com.muedsa.snapshot.widget.ConstrainedBox(constraints = snapshotConstraints)
                .apply { current?.let { attach(it) } }
        }

        val snapshotMargin = margin
        if (snapshotMargin != null) {
            current = com.muedsa.snapshot.widget.Padding(padding = snapshotMargin)
                .apply { current?.let { attach(it) } }
        }

        if (snapshotTransform != null) {
            current = com.muedsa.snapshot.widget.Transform(
                transform = snapshotTransform,
                alignment = snapshotTransformAlignment,
            ).apply { current?.let { attach(it) } }
        }

        return current!!
    }

    override fun createRenderBox(child: Widget?): RenderBox = composeWidget().createRenderBox()
}

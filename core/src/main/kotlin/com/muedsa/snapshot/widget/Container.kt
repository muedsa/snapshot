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

inline fun Widget.Container(
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
    buildChild(
        widget = Container(
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
            parent = this
        ),
        content = content
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
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

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
            current = LimitedBox(
                maxWidth = 0f,
                maxHeight = 0f,
                parent = null
            ).bind(ConstrainedBox(constraints = BoxConstraints.expand(), parent = null))
        } else if (snapshotAlignment != null) {
            current = Align(alignment = snapshotAlignment, parent = null)
                .bind(current)
        }

        if (snapshotPadding != null) {
            current = Padding(padding = snapshotPadding, parent = null)
                .bind(current)
        }

        if (snapshotColor != null) {
            current = ColoredBox(color = snapshotColor, parent = null).bind(current)
        }


        if (snapshotClipBehavior != ClipBehavior.NONE) {
            checkNotNull(snapshotDecoration)
            current = ClipPath(
                clipper = { snapshotDecoration.getClipPath(Offset.ZERO combine it) },
                clipBehavior = snapshotClipBehavior,
                parent = null
            ).bind(current)
        }

        if (snapshotDecoration != null) {
            current = DecoratedBox(
                decoration = snapshotDecoration,
                position = DecorationPosition.BACKGROUND,
                parent = null
            ).bind(current)
        }

        if (snapshotForegroundDecoration != null) {
            current = DecoratedBox(
                decoration = snapshotForegroundDecoration,
                position = DecorationPosition.FOREGROUND,
                parent = null
            ).bind(current)
        }

        if (snapshotConstraints != null) {
            current = ConstrainedBox(constraints = snapshotConstraints, parent = null)
                .bind(current)
        }

        val snapshotMargin = margin
        if (snapshotMargin != null) {
            current = Padding(padding = snapshotMargin, parent = null).bind(current)
        }

        if (snapshotTransform != null) {
            current = Transform(
                transform = snapshotTransform,
                alignment = snapshotTransformAlignment,
                parent = null
            ).bind(current)
        }

        return current!!
    }

    override fun createRenderBox(child: Widget?): RenderBox = composeWidget().createRenderBox()
}
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
    val alignment: BoxAlignment? = null,
    val padding: EdgeInsets? = null,
    val color: Int? = null,
    val decoration: Decoration? = null,
    val foregroundDecoration: Decoration? = null,
    val width: Float? = null,
    val height: Float? = null,
    var constraints: BoxConstraints? = null,
    val margin: EdgeInsets? = null,
    val transform: Matrix44CMO? = null,
    val transformAlignment: BoxAlignment? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.NONE,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    init {
        constraints = if (width != null || height != null)
            constraints?.tighten(width = width, height = height) ?: BoxConstraints.tightFor(
                width = width,
                height = height
            )
        else constraints
    }

    private fun composeWidget(): Widget {
        var current: Widget? = child

        if (child == null && (constraints == null || constraints!!.isTight)) {
            current = LimitedBox(
                maxWidth = 0f,
                maxHeight = 0f,
                parent = null
            ).bind(ConstrainedBox(constraints = BoxConstraints.expand(), parent = null))
        } else if (alignment != null) {
            current = Align(alignment = alignment, parent = null)
                .bind(current)
        }

        if (padding != null) {
            current = Padding(padding = padding, parent = null)
                .bind(current)
        }

        if (color != null) {
            current = ColoredBox(color = color, parent = null).bind(current)
        }

        if (clipBehavior != ClipBehavior.NONE) {
            assert(decoration != null)
            current = ClipPath(
                clipper = { decoration!!.getClipPath(Offset.ZERO combine it) },
                clipBehavior = clipBehavior,
                parent = null
            ).bind(current)
        }

        if (decoration != null) {
            current = DecoratedBox(
                decoration = decoration,
                position = DecorationPosition.BACKGROUND,
                parent = null
            ).bind(current)
        }

        if (foregroundDecoration != null) {
            current = DecoratedBox(
                decoration = foregroundDecoration,
                position = DecorationPosition.FOREGROUND,
                parent = null
            ).bind(current)
        }

        if (constraints != null) {
            current = ConstrainedBox(constraints = constraints!!, parent = null)
                .bind(current)
        }

        if (margin != null) {
            current = Padding(padding = margin, parent = null).bind(current)
        }

        if (transform != null) {
            current = Transform(
                transform = transform,
                alignment = transformAlignment,
                parent = null
            ).bind(current)
        }

        return current!!
    }

    override fun createRenderBox(child: Widget?): RenderBox = composeWidget().createRenderBox()
}

private fun Widget.bind(
    child: Widget?,
): Widget {
    child?.let {
        when (this) {
            is ProxyWidget -> this.widget = it
            is SingleChildWidget -> this.child = it
            is MultiChildWidget -> this.children.add(it)
            else -> throw IllegalCallerException()
        }
        it.parent = this
    }
    return this
}
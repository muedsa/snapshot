package com.muedsa.snapshot.widget

import com.muedsa.geometry.Alignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.snapshot.paint.decoration.Decoration
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.DecorationPosition
import com.muedsa.snapshot.rendering.box.RenderBox

class Container(
    val alignment: Alignment? = null,
    val padding: EdgeInsets? = null,
    val color: Int? = null,
    val decoration: Decoration? = null,
    val foregroundDecoration: Decoration? = null,
    val width: Float? = null,
    val height: Float? = null,
    var constraints: BoxConstraints? = null,
    val margin: EdgeInsets? = null,
    val transform: Matrix44CMO? = null,
    val transformAlignment: Alignment? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.NONE,
    childBuilder: SingleWidgetBuilder? = null
) : SingleChildWidget(childBuilder = childBuilder) {

    init {
        constraints = if (width != null || height != null)
            constraints?.tighten(width = width, height = height) ?: BoxConstraints.tightFor(
                width = width,
                height = height
            )
        else constraints
    }

    private val compositedWidget: Widget by lazy {
        var current: Widget? = child

        if (child == null && (constraints == null || constraints!!.isTight)) {
            current = LimitedBox(
                maxWidth = 0f,
                maxHeight = 0f
            ) {
                ConstrainedBox(constraints = BoxConstraints.expand())
            }
        } else if (alignment != null) {
            current = Align(alignment = alignment) {
                current
            }
        }

        if (padding != null) {
            current = Padding(padding = padding) {
                current
            }
        }

        if (color != null) {
            current = ColoredBox(color = color) {
                current
            }
        }

        if (clipBehavior != ClipBehavior.NONE) {
            assert(decoration != null)
            current = ClipPath(
                clipper = {
                    decoration!!.getClipPath(Offset.ZERO combine it)
                },
                clipBehavior = clipBehavior
            ) {
                current
            }
        }

        if (decoration != null) {
            current = DecoratedBox(
                decoration = decoration,
                position = DecorationPosition.BACKGROUND
            ) {
                current
            }
        }

        if (foregroundDecoration != null) {
            current = DecoratedBox(
                decoration = foregroundDecoration,
                position = DecorationPosition.FOREGROUND
            ) {
                current
            }
        }

        if (constraints != null) {
            current = ConstrainedBox(constraints = constraints!!) {
                current
            }
        }

        if (margin != null) {
            current = Padding(padding = margin) {
                current
            }
        }

        if (transform != null) {
            current = Transform(
                transform = transform,
                alignment = transformAlignment
            ) {
                current
            }
        }

        current!!
    }

    override fun createRenderTree(): RenderBox = compositedWidget.createRenderTree()
}
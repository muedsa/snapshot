package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.shift
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.*

class PaintingContext(
    val bounds: Rect,
    var debug: Boolean = false,
) : ClipContext() {

    override val canvas: Canvas
        get() = recorderCanvas

    private lateinit var recorder: PictureRecorder

    private lateinit var recorderCanvas: Canvas

    init {
        record()
    }

    fun stopRecord(): Picture = recorder.finishRecordingAsPicture()

    fun record() {
        recorder = PictureRecorder()
        recorderCanvas = recorder.beginRecording(bounds)
    }



    fun paintChild(child: RenderBox, offset: Offset) {
        child.paint(this, offset)
        if (debug) {
            child.debugPaint(this, offset)
        }
    }

    fun doClipPath(
        offset: Offset,
        bounds: Rect,
        clipPath: Path,
        clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
        painter: (PaintingContext, Offset) -> Unit,
    ) {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return
        }
        val offsetBounds: Rect = bounds.shift(offset)
        val offsetClipPath: Path = Path().also {
            clipPath.offset(offset.x, offset.y, it)
        }
        clipPathAndPaint(offsetClipPath, clipBehavior, offsetBounds) {
            painter(this, offset)
        }
    }

    fun doClipRect(
        offset: Offset,
        clipRect: Rect,
        clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
        painter: (PaintingContext, Offset) -> Unit,
    ) {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return
        }
        val offsetClipRect: Rect = clipRect.shift(offset)
        clipRectAndPaint(offsetClipRect, clipBehavior, offsetClipRect) {
            painter(this, offset)
        }
    }

    fun doClipRRect(
        offset: Offset,
        bounds: Rect,
        clipRRect: RRect,
        clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
        painter: (PaintingContext, Offset) -> Unit,
    ) {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return
        }
        val offsetBounds: Rect = bounds.shift(offset)
        val offsetClipRRect: RRect = clipRRect.shift(offset)
        clipRRectAndPaint(offsetClipRRect, clipBehavior, offsetBounds) {
            painter(this, offset)
        }
    }

    fun doTransform(offset: Offset, transform: Matrix44CMO, painter: (PaintingContext, Offset) -> Unit) {
        val effectiveTransform: Matrix44CMO = Matrix44CMO.translationValues(
            x = offset.x,
            y = offset.y,
            z = 0f
        ).apply {
            multiply(transform)
            translate(-offset.x, -offset.y)
        }
        canvas.save()
        // skia的Matrix44为行顺序
        canvas.concat(effectiveTransform.toRMO())
        painter(this, offset)
        canvas.restore()
    }

    fun pushBackdrop(
        offset: Offset,
        clipRect: Rect,
        imageFilter: ImageFilter,
        blendMode: BlendMode = BlendMode.SRC_OVER,
    ) {
        val rect = clipRect.shift(offset)
        val picture = stopRecord()
        record()
        canvas.clipRect(rect)
        canvas.saveLayer(bounds = bounds, paint = Paint().apply {
            this@apply.imageFilter = imageFilter
            this@apply.blendMode = blendMode
            this@apply.isAntiAlias = true
        })
        canvas.drawPicture(picture)
        canvas.restore() // saveLayer
        canvas.restore() // clipRect
        val backdropPicture = stopRecord()
        record()
        canvas.drawPicture(picture = picture)
        canvas.drawPicture(picture = backdropPicture)
    }
}
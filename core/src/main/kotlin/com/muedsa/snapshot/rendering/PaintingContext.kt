package com.muedsa.snapshot.rendering

import com.muedsa.annotations.CallSuper
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.shift
import com.muedsa.snapshot.debugCurrentRepaintColor
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.*

class PaintingContext private constructor(
    val containerLayer: ContainerLayer,
    val estimatedBounds: Rect,
    var debug: Boolean = false,
) : ClipContext() {

    override val canvas: Canvas
        get() {
            if (recorderCanvas == null) {
                startRecording()
            }
            assert(currentLayer != null)
            return recorderCanvas!!
        }

    fun paintChild(child: RenderBox, offset: Offset) {
        child.paint(this, offset)
        if (debug) {
            child.debugPaint(this, offset)
        }
    }

    protected fun appendLayer(layer: Layer) {
        assert(!isRecording)
        containerLayer.append(layer)
    }


    private var currentLayer: PictureLayer? = null
    private var recorder: PictureRecorder? = null
    private var recorderCanvas: Canvas? = null

    val isRecording: Boolean
        get() {
            val hasCanvas: Boolean = recorderCanvas != null
            if (hasCanvas) {
                assert(currentLayer != null)
                assert(recorder != null)
                assert(recorderCanvas != null)
            } else {
                assert(currentLayer == null)
                assert(recorder == null)
                assert(recorderCanvas == null)
            }
            return hasCanvas
        }


    protected fun startRecording() {
        assert(!isRecording)
        currentLayer = PictureLayer()
        recorder = PictureRecorder()
        recorderCanvas = recorder!!.beginRecording(estimatedBounds)
        containerLayer.append(currentLayer!!)
    }

    @CallSuper
    protected fun stopRecordingIfNeeded() {
        if (!isRecording) {
            return
        }

        // debug paint
        if (debug) {
            canvas.drawRect(estimatedBounds.inflate(-3f), Paint().also {
                it.mode = PaintMode.STROKE
                it.strokeWidth = 6f
                it.color = debugCurrentRepaintColor
            })

            canvas.drawRect(estimatedBounds, Paint().also {
                it.mode = PaintMode.STROKE
                it.strokeWidth = 1f
                it.color = 0xFF_FF_98_00.toInt()
            })
        }

        currentLayer!!.picture = recorder!!.finishRecordingAsPicture()
        currentLayer = null
        recorder = null
        recorderCanvas = null
    }

    fun pushLayer(
        childLayer: ContainerLayer,
        painter: (PaintingContext, Offset) -> Unit,
        offset: Offset,
        childPaintBounds: Rect? = null,
    ) {
        stopRecordingIfNeeded()
        appendLayer(childLayer)
        val childContext: PaintingContext = createChildContext(childLayer, childPaintBounds ?: estimatedBounds)
        painter(childContext, offset)
        childContext.stopRecordingIfNeeded()
    }

    protected fun createChildContext(childLayer: ContainerLayer, bounds: Rect): PaintingContext {
        return PaintingContext(childLayer, bounds)
    }

    fun pushClipPath(
        offset: Offset,
        bounds: Rect,
        clipPath: Path,
        clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
        painter: (PaintingContext, Offset) -> Unit,
    ): ClipPathLayer? {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return null
        }
        val offsetBounds: Rect = bounds.shift(offset)
        val offsetClipPath: Path = Path().also {
            clipPath.offset(offset.x, offset.y, it)
        }
        val layer = ClipPathLayer(
            clipPath = offsetClipPath,
            clipBehavior = clipBehavior
        )
        pushLayer(layer, painter, offset, childPaintBounds = offsetBounds)
        return layer
    }

    fun pushClipRect(
        offset: Offset,
        clipRect: Rect,
        clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
        painter: (PaintingContext, Offset) -> Unit,
    ): ClipRectLayer? {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return null
        }
        val offsetClipRect: Rect = clipRect.shift(offset)
        val layer = ClipRectLayer(
            clipRect = offsetClipRect,
            clipBehavior = clipBehavior
        )
        pushLayer(layer, painter, offset, childPaintBounds = offsetClipRect)
        return layer
    }

    fun pushClipRRect(
        offset: Offset,
        bounds: Rect,
        clipRRect: RRect,
        clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
        painter: (PaintingContext, Offset) -> Unit,
    ): ClipRRectLayer? {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return null
        }
        val offsetBounds: Rect = bounds.shift(offset)
        val offsetClipRRect: RRect = clipRRect.shift(offset)
        val layer = ClipRRectLayer(
            clipRRect = offsetClipRRect,
            clipBehavior = clipBehavior
        )
        pushLayer(layer, painter, offset, childPaintBounds = offsetBounds)
        return layer
    }

    fun pushTransform(
        offset: Offset,
        transform: Matrix44CMO,
        painter: (PaintingContext, Offset) -> Unit,
    ): TransformLayer {
        val effectiveTransform: Matrix44CMO = Matrix44CMO.translationValues(
            x = offset.x,
            y = offset.y,
            z = 0f
        ).apply {
            multiply(transform)
            translate(-offset.x, -offset.y)
        }
        val layer = TransformLayer(
            transform = effectiveTransform
        )
        pushLayer(layer, painter, offset, childPaintBounds = estimatedBounds)
        return layer
    }

    fun pushBackDropFilter(
        offset: Offset,
        imageFilter: ImageFilter,
        blendMode: BlendMode = BlendMode.SRC_OVER,
        painter: (PaintingContext, Offset) -> Unit,
    ): BackdropFilterLayer {
        val layer = BackdropFilterLayer(
            bounds = estimatedBounds,
            filter = imageFilter,
            blendMode = blendMode,
        )
        pushLayer(layer, painter, offset, childPaintBounds = estimatedBounds)
        return layer
    }

    fun pushImageFilter(
        offset: Offset,
        imageFilter: ImageFilter,
        painter: (PaintingContext, Offset) -> Unit,
    ): ImageFilterLayer {
        val layer = ImageFilterLayer(filter = imageFilter)
        pushLayer(layer, painter, offset, childPaintBounds = estimatedBounds)
        return layer
    }

    fun pushColorFilter(
        offset: Offset,
        colorFilter: ColorFilter,
        painter: (PaintingContext, Offset) -> Unit,
    ): ColorFilterLayer {
        val layer = ColorFilterLayer(filter = colorFilter)
        pushLayer(layer, painter, offset, childPaintBounds = estimatedBounds)
        return layer
    }

    fun pushOpacity(
        offset: Offset,
        opacity: Float = 1f,
        painter: (PaintingContext, Offset) -> Unit,
    ): OpacityLayer {
        val layer = OpacityLayer(opacity = opacity)
        pushLayer(layer, painter, offset, childPaintBounds = estimatedBounds)
        return layer
    }

    fun composite(layerPaintContext: LayerPaintContext) {
        stopRecordingIfNeeded()
        containerLayer.paint(layerPaintContext)
    }

    companion object {

        fun paintRoot(bounds: Rect, renderBox: RenderBox, debug: Boolean = false): PaintingContext {
            val paintingContext = PaintingContext(
                containerLayer = ContainerLayer(),
                estimatedBounds = bounds,
                debug = debug
            )
            paintingContext.paintChild(renderBox, Offset.ZERO)
            paintingContext.stopRecordingIfNeeded()
            return paintingContext
        }
    }
}
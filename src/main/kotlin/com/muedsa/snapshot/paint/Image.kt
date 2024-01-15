package com.muedsa.snapshot.paint

import com.muedsa.geometry.*
import org.jetbrains.skia.*
import kotlin.math.ceil
import kotlin.math.floor


fun paintImage(
    canvas: Canvas,
    rect: Rect,
    image: Image,
    // debugImageLabel: String? = null,
    scale: Float = 1f,
    opacity: Float = 1f,
    colorFilter: ColorFilter? = null,
    fit: BoxFit? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    centerSlice: Rect? = null,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    flipHorizontally: Boolean = false,
    isAntiAlias: Boolean = false,
    blendMode: BlendMode = BlendMode.SRC_OVER,
) {
    assert(!image.isClosed) { "Cannot paint an image that is disposed." }
    if (image.isEmpty) {
        return
    }
    var outputSize: Size = rect.size
    var inputSize = Size(image.width.toFloat(), image.height.toFloat())
    var sliceBorder: Offset? = null
    if (centerSlice != null) {
        sliceBorder = inputSize / scale - centerSlice.size
        outputSize -= sliceBorder
        inputSize -= sliceBorder
    }
    val imageFit = fit ?: if (centerSlice != null) {
        BoxFit.SCALE_DOWN
    } else {
        BoxFit.FILL
    }
    assert(centerSlice == null || (imageFit != BoxFit.NONE && imageFit != BoxFit.COVER))
    val fittedSizes: FittedSizes =
        FittedSizes.applyBoxFit(fit = imageFit, inputSize = inputSize, outputSize = outputSize)
    val sourceSize: Size = fittedSizes.source * scale
    var destinationSize: Size = fittedSizes.destination
    if (centerSlice != null) {
        outputSize += sliceBorder!!
        destinationSize += sliceBorder
        // We don't have the ability to draw a subset of the image at the same time
        // as we apply a nine-patch stretch.
        assert(sourceSize == inputSize) { "centerSlice was used with a BoxFit that does not guarantee that the image is fully visible." }
    }

    var imageRepeat = repeat
    if (repeat != ImageRepeat.NO_REPEAT && destinationSize == outputSize) {
        // There's no need to repeat the image because we're exactly filling the
        // output rect with the image.
        imageRepeat = ImageRepeat.NO_REPEAT
    }

    val paint = Paint().apply {
        this.isAntiAlias = isAntiAlias
        this.colorFilter = colorFilter
        color = Color.makeARGB((255 * opacity.coerceIn(0f, 1f)).toInt(), 0, 0, 0)
        // `this.filterQuality = filterQuality` replaced by `canvas.drawImageRect(samplingMode = )`
        this.blendMode = blendMode
    }

    val halfWidthDelta: Float = (outputSize.width - destinationSize.width) / 2f
    val halfHeightDelta: Float = (outputSize.height - destinationSize.height) / 2f
    val dx: Float = halfWidthDelta + (if (flipHorizontally) -alignment.x else alignment.x) * halfWidthDelta
    val dy: Float = halfHeightDelta + alignment.y * halfHeightDelta
    val destinationPosition: Offset = rect.topLeft.translate(dx, dy)
    val destinationRect: Rect = destinationPosition combine destinationSize

    // Set to true if we added a saveLayer to the canvas to invert/flip the image.
    val invertedCanvas: Boolean = false
    // Output size and destination rect are fully calculated.

    val needSave: Boolean = centerSlice != null || imageRepeat != ImageRepeat.NO_REPEAT || flipHorizontally
    if (needSave) {
        canvas.save()
    }
    if (imageRepeat != ImageRepeat.NO_REPEAT) {
        canvas.clipRect(rect)
    }
    if (flipHorizontally) {
        val flipHorizontallyDx: Float = -(rect.left + rect.width / 2f)
        canvas.translate(-flipHorizontallyDx, 0f)
        canvas.scale(-1f, 1f)
        canvas.translate(flipHorizontallyDx, 0f)
    }
    if (centerSlice == null) {
        val sourceRect: Rect = alignment.inscribe(
            sourceSize, Offset.ZERO combine inputSize,
        )
        if (imageRepeat == ImageRepeat.NO_REPEAT) {
            canvas.drawImageRect(
                image = image,
                src = sourceRect,
                dst = destinationRect,
                paint = paint
            )
        } else {
            for (tileRect in generateImageTileRects(rect, destinationRect, imageRepeat)) {
                canvas.drawImageRect(
                    image = image,
                    sourceRect,
                    tileRect,
                    paint = paint
                )
            }
        }
    } else {
        canvas.scale(1f / scale, 1f / scale)
        if (imageRepeat == ImageRepeat.NO_REPEAT) {
            canvas.drawImageNine(
                image = image,
                center = centerSlice.scale(scale).toIRect(),
                dst = destinationRect.scale(scale),
                filterMode = FilterMode.LINEAR,
                paint = paint
            )
        } else {
            for (tileRect in generateImageTileRects(rect, destinationRect, imageRepeat)) {
                canvas.drawImageNine(
                    image = image,
                    centerSlice.scale(scale).toIRect(),
                    tileRect.scale(scale),
                    filterMode = FilterMode.LINEAR,
                    paint = paint
                )
            }
        }
    }
    if (needSave) {
        canvas.restore()
    }

    if (invertedCanvas) {
        canvas.restore()
    }
}

internal fun generateImageTileRects(outputRect: Rect, fundamentalRect: Rect, repeat: ImageRepeat): List<Rect> {
    var startX = 0
    var startY = 0
    var stopX = 0
    var stopY = 0
    val strideX = fundamentalRect.width
    val strideY = fundamentalRect.height

    if (repeat == ImageRepeat.REPEAT || repeat == ImageRepeat.REPEAT_X) {
        startX = floor((outputRect.left - fundamentalRect.left) / strideX).toInt()
        stopX = ceil((outputRect.right - fundamentalRect.right) / strideX).toInt()
    }

    if (repeat == ImageRepeat.REPEAT || repeat == ImageRepeat.REPEAT_Y) {
        startY = floor((outputRect.top - fundamentalRect.top) / strideY).toInt()
        stopY = ceil((outputRect.bottom - fundamentalRect.bottom) / strideY).toInt()
    }

    return buildList {
        for (i in startX..stopX) {
            for (j in startY..stopY) {
                add(fundamentalRect.shift(Offset(i * strideX, j * strideY)))
            }
        }
    }
}
package com.muedsa.snapshot.paint

import com.muedsa.geometry.Size
import kotlin.math.min

data class FittedSizes(val source: Size, val destination: Size) {

    companion object {
        val ZERO = FittedSizes(source = Size.ZERO, destination = Size.ZERO)

        @JvmStatic
        fun applyBoxFit(fit: BoxFit, inputSize: Size, outputSize: Size): FittedSizes {
            if (inputSize.height <= 0f || inputSize.width <= 0f || outputSize.height <= 0f || outputSize.width <= 0f) {
                return ZERO
            }
            val sourceSize: Size
            var destinationSize: Size

            when (fit) {
                BoxFit.FILL -> {
                    sourceSize = inputSize
                    destinationSize = outputSize
                }

                BoxFit.CONTAIN -> {
                    sourceSize = inputSize
                    destinationSize = if (outputSize.width / outputSize.height > sourceSize.width / sourceSize.height) {
                        Size(sourceSize.width * outputSize.height / sourceSize.height, outputSize.height)
                    } else {
                        Size(outputSize.width, sourceSize.height * outputSize.width / sourceSize.width)
                    }
                }

                BoxFit.COVER -> {
                    sourceSize = if (outputSize.width / outputSize.height > inputSize.width / inputSize.height) {
                        Size(inputSize.width, inputSize.width * outputSize.height / outputSize.width)
                    } else {
                        Size(inputSize.height * outputSize.width / outputSize.height, inputSize.height)
                    }
                    destinationSize = outputSize
                }

                BoxFit.FIT_WIDTH -> {
                    if (outputSize.width / outputSize.height > inputSize.width / inputSize.height) {
                        // Like "cover"
                        sourceSize = Size(inputSize.width, inputSize.width * outputSize.height / outputSize.width)
                        destinationSize = outputSize
                    } else {
                        // Like "contain"
                        sourceSize = inputSize
                        destinationSize =
                            Size(outputSize.width, sourceSize.height * outputSize.width / sourceSize.width)
                    }
                }

                BoxFit.FIT_HEIGHT -> {
                    if (outputSize.width / outputSize.height > inputSize.width / inputSize.height) {
                        // Like "contain"
                        sourceSize = inputSize
                        destinationSize =
                            Size(sourceSize.width * outputSize.height / sourceSize.height, outputSize.height)
                    } else {
                        // Like "cover"
                        sourceSize = Size(inputSize.height * outputSize.width / outputSize.height, inputSize.height)
                        destinationSize = outputSize
                    }
                }

                BoxFit.NONE -> {
                    sourceSize = Size(min(inputSize.width, outputSize.width), min(inputSize.height, outputSize.height))
                    destinationSize = sourceSize
                }

                BoxFit.SCALE_DOWN -> {
                    sourceSize = inputSize
                    destinationSize = inputSize
                    val aspectRatio: Float = inputSize.width / inputSize.height
                    if (destinationSize.height > outputSize.height) {
                        destinationSize = Size(outputSize.height * aspectRatio, outputSize.height)
                    }
                    if (destinationSize.width > outputSize.width) {
                        destinationSize = Size(outputSize.width, outputSize.width / aspectRatio)
                    }
                }
            }
            return FittedSizes(source = sourceSize, destination = destinationSize)
        }
    }
}
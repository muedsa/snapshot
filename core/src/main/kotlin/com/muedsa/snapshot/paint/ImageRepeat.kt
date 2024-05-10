package com.muedsa.snapshot.paint

enum class ImageRepeat {
    // Repeat the image in both the x and y directions until the box is filled.
    REPEAT,

    // Repeat the image in the x direction until the box is filled horizontally.
    REPEAT_X,

    // Repeat the image in the y direction until the box is filled vertically.
    REPEAT_Y,

    // Leave uncovered portions of the box transparent.
    NO_REPEAT,
}
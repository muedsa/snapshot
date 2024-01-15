package com.muedsa.snapshot.paint

enum class Axis {
    // Left and right.
    HORIZONTAL,

    // Up and down.
    VERTICAL,
    ;

    fun flipAxis(): Axis = when (this) {
        HORIZONTAL -> VERTICAL
        VERTICAL -> HORIZONTAL
    }
}
package com.muedsa.snapshot.paint.text

enum class TextOverflow {
    /// Clip the overflowing text to fix its container.
    CLIP,

    // Fade the overflowing text to transparent.
    FADE,

    // Use an ellipsis to indicate that the text has overflowed.
    ELLIPSIS,

    // Render overflowing text outside of its container.
    VISIBLE,
}
package com.muedsa.snapshot.paint.text

enum class TextWidthBasis {
    // multiline text will take up the full width given by the parent. For single
    // line text, only the minimum amount of width needed to contain the text
    // will be used. A common use case for this is a standard series of
    // paragraphs.
    PARENT,

    // The width will be exactly enough to contain the longest line and no
    // longer. A common use case for this is chat bubbles.
    LONGESTLINE,
}
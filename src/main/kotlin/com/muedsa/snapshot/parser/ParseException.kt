package com.muedsa.snapshot.parser

class ParseException(
    val pos: TrackPos,
    message: String,
) : RuntimeException(message) {

    constructor(reader: CharacterReader, message: String) : this(
        TrackPos(
            reader.pos(),
            reader.currentLineNumber(),
            reader.currentColumnNumber()
        ), message
    )

    override fun toString(): String = "$pos: $message"
}
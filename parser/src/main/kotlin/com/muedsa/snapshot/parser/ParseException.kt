package com.muedsa.snapshot.parser

class ParseException(
    val pos: TrackPos,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    constructor(reader: CharacterReader, message: String, cause: Throwable? = null) : this(
        TrackPos(
            reader.pos(),
            reader.currentLineNumber(),
            reader.currentColumnNumber()
        ), message, cause
    )

    override fun toString(): String = "$pos: $message"
}
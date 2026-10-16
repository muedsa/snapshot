package com.muedsa.snapshot.parser

class ParseException(
    val pos: TrackPos,
    private val detailMessage: String,
    cause: Throwable?,
    val attributeDiagnostic: AttributeDiagnostic?,
) : RuntimeException(
    detailMessage + (attributeDiagnostic?.let { "\n${it.describe()}" } ?: ""),
    cause,
) {

    constructor(pos: TrackPos, message: String, cause: Throwable? = null) : this(pos, message, cause, null)

    constructor(reader: CharacterReader, message: String, cause: Throwable? = null) : this(
        TrackPos(
            reader.pos(),
            reader.currentLineNumber(),
            reader.currentColumnNumber()
        ), message, cause
    )

    internal fun withElementContext(name: String, position: TrackPos): ParseException {
        val diagnostic = attributeDiagnostic ?: return this
        if (diagnostic.elementName != null) return this
        return ParseException(
            pos.takeIf { it.hasSetting() }?.copy() ?: position.copy(),
            detailMessage,
            cause,
            diagnostic.copy(elementName = name),
        ).also { it.stackTrace = stackTrace }
    }

    override fun toString(): String = "$pos: $message"
}

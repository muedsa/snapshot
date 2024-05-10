package com.muedsa.snapshot.parser.token

import com.muedsa.snapshot.parser.TrackPos

data class RawAttr(
    val name: String,
    val value: String? = null,
//    val rawName: String,
//    val rawValue: String? = null,
    val nameStartPos: TrackPos = TrackPos(),

    val nameEndPos: TrackPos = TrackPos(),

    val valueStartPos: TrackPos = TrackPos(),

    val valueEndPos: TrackPos = TrackPos(),
) {
    override fun toString(): String {
        return if (value != null)
            "$name($nameStartPos)=$value($valueStartPos)"
        else
            "$name($nameStartPos)"
    }
}
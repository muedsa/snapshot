package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.token.RawAttr
import org.jetbrains.skia.EncodedImageFormat
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.expect

class SnapshotElementTest {

    @Test
    fun attr_parse_test() {
        expect("png") {
            SnapshotElement.ATTR_TYPE.parseValue(RawAttr(SnapshotElement.ATTR_TYPE.name, "png"))
        }
        expect("jpg") {
            SnapshotElement.ATTR_TYPE.parseValue(RawAttr(SnapshotElement.ATTR_TYPE.name, "jpg"))
        }
        expect("webp") {
            SnapshotElement.ATTR_TYPE.parseValue(RawAttr(SnapshotElement.ATTR_TYPE.name, "webp"))
        }
        assertThrows<Throwable> {
            SnapshotElement.ATTR_TYPE.parseValue(RawAttr(SnapshotElement.ATTR_TYPE.name, "233"))
        }
    }

    @Test
    fun other_val_test() {
        expect(EncodedImageFormat.PNG) {
            SnapshotElement(
                attrs = mutableMapOf(
                    SnapshotElement.ATTR_TYPE.name to RawAttr(SnapshotElement.ATTR_TYPE.name, "png")
                ),
                pos = TrackPos()
            ).format
        }
        expect(EncodedImageFormat.JPEG) {
            SnapshotElement(
                attrs = mutableMapOf(
                    SnapshotElement.ATTR_TYPE.name to RawAttr(SnapshotElement.ATTR_TYPE.name, "jpg")
                ),
                pos = TrackPos()
            ).format
        }
        expect(EncodedImageFormat.WEBP) {
            SnapshotElement(
                attrs = mutableMapOf(
                    SnapshotElement.ATTR_TYPE.name to RawAttr(SnapshotElement.ATTR_TYPE.name, "webp")
                ),
                pos = TrackPos()
            ).format
        }
    }
}
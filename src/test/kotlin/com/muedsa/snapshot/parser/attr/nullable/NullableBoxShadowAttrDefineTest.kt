package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.material.ELEVATION_MAP
import com.muedsa.snapshot.paint.decoration.BoxShadow
import com.muedsa.snapshot.parser.token.RawAttr
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterBlurMode
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContentEquals

class NullableBoxShadowAttrDefineTest {

    @Test
    fun parse_test() {
        val attrName = "test"
        val attr = NullableBoxShadowAttrDefine(attrName)

        assertContentEquals(ELEVATION_MAP[1], attr.parseValue(RawAttr(attrName, "ELEVATION_1")))

        assertThrows<Throwable> {
            attr.parseValue(RawAttr(attrName, "ELEVATION_111"))
        }

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = Color.BLACK,
                    offset = Offset(12f, 23f),
                    blurRadius = 0f
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = Color.BLACK,
                    offset = Offset(12f, 23f),
                    blurRadius = 3f
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 3"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = 0xFF_FF_00_00.toInt(),
                    offset = Offset(12f, 23f),
                    blurRadius = 0f
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 #FFFF0000"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = Color.BLACK,
                    offset = Offset(12f, 23f),
                    blurRadius = 0f,
                    blurStyle = FilterBlurMode.INNER
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 INNER"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = Color.BLACK,
                    offset = Offset(12f, 23f),
                    blurRadius = 3f,
                    spreadRadius = 6f
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 3 6"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = 0xFF_FF_00_00.toInt(),
                    offset = Offset(12f, 23f),
                    blurRadius = 3f,
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 3 #FFFF0000"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = Color.BLACK,
                    offset = Offset(12f, 23f),
                    blurRadius = 3f,
                    blurStyle = FilterBlurMode.INNER
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 3 INNER"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = 0xFF_FF_00_00.toInt(),
                    offset = Offset(12f, 23f),
                    blurRadius = 3f,
                    spreadRadius = 6f
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 3 6 #FFFF0000"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = Color.BLACK,
                    offset = Offset(12f, 23f),
                    blurRadius = 3f,
                    spreadRadius = 6f,
                    blurStyle = FilterBlurMode.INNER
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 3 6 INNER"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = 0xFF_FF_00_00.toInt(),
                    offset = Offset(12f, 23f),
                    blurRadius = 3f,
                    spreadRadius = 6f,
                    blurStyle = FilterBlurMode.INNER
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23 3 6 #FFFF0000 INNER"))
        )

        assertContentEquals(
            arrayOf(
                BoxShadow(
                    color = Color.BLACK,
                    offset = Offset(12f, 23f),
                    blurRadius = 0f,
                ),
                BoxShadow(
                    color = Color.BLACK,
                    offset = Offset(13f, 14f),
                    blurRadius = 0f
                )
            ),
            attr.parseValue(RawAttr(attrName, "12 23,13 14"))
        )
    }
}
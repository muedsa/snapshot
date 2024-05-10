package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BorderStyle
import com.muedsa.snapshot.parser.token.RawAttr
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.expect

class NullableBorderSideAttrDefineTest {

    @Test
    fun borderSideAttrDefine_test() {
        val attrName = "test"
        val attr = NullableBorderSideAttrDefine(attrName)
        expect(
            BorderSide(
                color = Color.GREEN,
                width = 2f,
                style = BorderStyle.SOLID
            )
        ) {
            attr.parseValue(RawAttr(attrName, "2 SOLID #FF00FF00"))
        }

        expect(
            BorderSide(
                color = Color.WHITE,
                width = 1.1f,
                style = BorderStyle.SOLID
            )
        ) {
            attr.parseValue(RawAttr(attrName, "1.1 SOLID #FFFFFF"))
        }
    }
}
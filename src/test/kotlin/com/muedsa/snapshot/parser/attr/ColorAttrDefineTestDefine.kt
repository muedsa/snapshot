package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.Test
import kotlin.test.expect

class ColorAttrDefineTestDefine {

    @Test
    fun parse_test() {
        val attrName = "test"
        val attr = ColorAttrDefine(attrName)

        expect(0xFF_FF_FF_FF.toInt()) {
            attr.parseValue(RawAttr(attrName, "#FFFFFFFF"))
        }

        expect(0xFF_00_00_00.toInt()) {
            attr.parseValue(RawAttr(attrName, "#FF000000"))
        }
    }
}
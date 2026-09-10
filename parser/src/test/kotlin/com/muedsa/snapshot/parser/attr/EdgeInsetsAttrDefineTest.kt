package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.assertFailsWith
import kotlin.test.Test
import kotlin.test.expect

class EdgeInsetsAttrDefineTest {

    @Test
    fun parse_test() {
        val attrName = "test"
        val attr = EdgeInsetsAttrDefine(attrName, EdgeInsets(1f, 2f, 3f, 4f))

        DefaultValueAttrDefineTest.tryDefaultValueTest(attr)

        expect(EdgeInsets.all(4f)) {
            attr.parseValue(RawAttr(attrName, "4"))
        }

        expect(EdgeInsets.all(5.5f)) {
            attr.parseValue(RawAttr(attrName, "5.5"))
        }

        expect(EdgeInsets.symmetric(5f, 4f)) {
            attr.parseValue(RawAttr(attrName, "(5,4)"))
        }

        expect(EdgeInsets.symmetric(5.5f, 4.4f)) {
            attr.parseValue(RawAttr(attrName, "(5.5,4.4)"))
        }

        expect(EdgeInsets(7f, 11.65f, 8.9f, 20f)) {
            attr.parseValue(RawAttr(attrName, "(7,11.65,8.9,20)"))
        }

        assertFailsWith<Throwable> {
            attr.parseValue(RawAttr(attrName, "(7,11.65,8.9)"))
        }

        assertFailsWith<Throwable> {
            attr.parseValue(RawAttr(attrName, "7,11.65"))
        }

        assertFailsWith<Throwable> {
            attr.parseValue(RawAttr(attrName, "asd"))
        }
    }
}
package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Radius
import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.assertFailsWith
import kotlin.test.Test
import kotlin.test.expect

class NullableRadiusAttrDefineTest {

    @Test
    fun radiusAttrDefine_test() {
        val attrName = "test"
        val attr = NullableRadiusAttrDefine(attrName)
        expect(Radius(5f, 5f)) {
            attr.parseValue(RawAttr(attr.name, "5"))
        }

        expect(Radius(2.1f, 3.4f)) {
            attr.parseValue(RawAttr(attr.name, "(2.1,3.4)"))
        }

        assertFailsWith<Throwable> {
            attr.parseValue(RawAttr(attrName, "(8.9)"))
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
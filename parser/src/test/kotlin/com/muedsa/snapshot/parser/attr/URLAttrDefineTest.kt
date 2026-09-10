package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.assertFailsWith
import kotlin.test.Test

class URLAttrDefineTest {

    @Test
    fun parse_test() {
        assertFailsWith<Throwable> { CommonAttrDefine.URL.parseValue(RawAttr(CommonAttrDefine.URL.name, "123")) }
        assertFailsWith<Throwable> { CommonAttrDefine.URL.parseValue(RawAttr(CommonAttrDefine.URL.name, null)) }
        CommonAttrDefine.URL.parseValue(RawAttr(CommonAttrDefine.URL.name, "https://github.com/"))
    }
}
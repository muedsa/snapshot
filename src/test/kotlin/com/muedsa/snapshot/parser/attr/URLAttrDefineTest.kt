package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.token.RawAttr
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class URLAttrDefineTest {

    @Test
    fun parse_test() {
        assertThrows<Throwable> { CommonAttrDefine.URL.parseValue(RawAttr(CommonAttrDefine.URL.name, "123")) }
        assertThrows<Throwable> { CommonAttrDefine.URL.parseValue(RawAttr(CommonAttrDefine.URL.name, null)) }
        CommonAttrDefine.URL.parseValue(RawAttr(CommonAttrDefine.URL.name, "https://github.com/"))
    }
}
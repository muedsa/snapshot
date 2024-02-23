package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.Test

class TextParserTest {

    @Test
    fun parse_text_attr_test() {
        CommonAttrDefine.TEXT.parseValue(RawAttr(CommonAttrDefine.TEXT.name, "123 123132✅1ada asda 🤣"))
    }

    //@Test
    fun buildWidget_test() {
        //Tag.TEXT.buildWidget()
    }
}
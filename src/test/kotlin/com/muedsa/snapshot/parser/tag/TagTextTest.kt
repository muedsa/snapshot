package com.muedsa.snapshot.parser.tag

import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.Test

class TagTextTest {

    @Test
    fun parse_text_attr_test() {
        CommonAttrDefine.TEXT.parseValue(RawAttr(CommonAttrDefine.TEXT.name, "123 1231321ada asda"))
    }

    //@Test
    fun buildWidget_test() {
        //Tag.TEXT.buildWidget()
    }
}
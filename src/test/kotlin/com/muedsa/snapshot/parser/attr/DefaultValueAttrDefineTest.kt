package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.attr.required.AttrDefine
import kotlin.test.expect

class DefaultValueAttrDefineTest {

    companion object {

        fun <T> tryDefaultValueTest(attrDefine: AttrDefine<T>) {
            if (attrDefine is DefaultValueAttrDefine<T>) {
                tryDefaultValueTest(attrDefine)
            }
        }

        fun <T> tryDefaultValueTest(attrDefine: DefaultValueAttrDefine<T>) {
            expect(attrDefine.defaultValue) {
                attrDefine.parseValue(null)
            }
        }
    }
}
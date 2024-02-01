package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.enums.enumEntries

class EnumAttrDefineTest {

    @Test
    fun mainAxisAlignment_test() {
        DefaultValueAttrDefineTest.tryDefaultValueTest(CommonAttrDefine.MAIN_AXIS_ALIGNMENT)
        parseEnumTest(CommonAttrDefine.MAIN_AXIS_ALIGNMENT)
    }

    @Test
    fun mainAxisSize_test() {
        DefaultValueAttrDefineTest.tryDefaultValueTest(CommonAttrDefine.MAIN_AXIS_SIZE)
        parseEnumTest(CommonAttrDefine.MAIN_AXIS_SIZE)
    }

    @Test
    fun cross_axis_alignment_test() {
        DefaultValueAttrDefineTest.tryDefaultValueTest(CommonAttrDefine.CROSS_AXIS_ALIGNMENT)
        parseEnumTest(CommonAttrDefine.CROSS_AXIS_ALIGNMENT)
    }

    @Test
    fun direction_test() {
        DefaultValueAttrDefineTest.tryDefaultValueTest(CommonAttrDefine.DIRECTION)
        parseEnumTest(CommonAttrDefine.DIRECTION)
    }

    @Test
    fun vertical_direction_test() {
        DefaultValueAttrDefineTest.tryDefaultValueTest(CommonAttrDefine.VERTICAL_DIRECTION)
        parseEnumTest(CommonAttrDefine.VERTICAL_DIRECTION)
    }

    @Test
    fun baseline_test() {
        DefaultValueAttrDefineTest.tryDefaultValueTest(CommonAttrDefine.BASELINE)
        parseEnumTest(CommonAttrDefine.BASELINE)
    }

    @Test
    fun nullable_box_fit_test() {
        DefaultValueAttrDefineTest.tryDefaultValueTest(CommonAttrDefine.FIT_N)
        parseNullableEnumTest(CommonAttrDefine.FIT_N)
    }


    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        inline fun <reified T : Enum<T>> parseEnumTest(attrDefine: AttrDefine<T>) {
            enumEntries<T>().forEach {
                attrDefine.parseValue(RawAttr(attrDefine.name, it.name))
            }

            assertThrows<Throwable> {
                attrDefine.parseValue(RawAttr(attrDefine.name, "🤣🤣🤣"))
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        inline fun <reified T : Enum<T>> parseNullableEnumTest(attrDefine: AttrDefine<T?>) {
            enumEntries<T>().forEach {
                attrDefine.parseValue(RawAttr(attrDefine.name, it.name))
            }

            assertThrows<Throwable> {
                attrDefine.parseValue(RawAttr(attrDefine.name, "🤣🤣🤣"))
            }
        }
    }
}
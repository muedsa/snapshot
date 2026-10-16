package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.SnapshotElement
import com.muedsa.snapshot.parser.attr.nullable.*
import com.muedsa.snapshot.parser.attr.required.*
import com.muedsa.snapshot.parser.image.SimpleDataUriImageDecoder
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.parser.widget.ClipPathParser
import com.muedsa.snapshot.parser.widget.TextParser
import com.muedsa.snapshot.parser.widget.UnconstrainedBoxParser
import com.muedsa.snapshot.parser.widget.WidgetParser
import org.jetbrains.skia.PaintMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AttributeValueInfoTest {
    private val definitions: List<AttrDefine<*>> = listOf(
        AlignmentAttrDefine("value"),
        BaselineAttrDefine("value"),
        BlendModeAttrDefine("value"),
        BooleanAttrDefine("value"),
        BoxShapeAttrDefine("value"),
        ClipBehaviorAttrDefine("value"),
        ColorAttrDefine("value"),
        CrossAxisAlignmentAttrDefine("value"),
        DecorationPositionAttrDefine("value"),
        DirectionAttrDefine("value"),
        EdgeInsetsAttrDefine("value"),
        FilterTileModeAttrDefine("value"),
        FlexFitAttrDefine("value"),
        FloatAttrDefine("value"),
        FontStyleAttrDefine("value"),
        ImageRepeatAttrDefine("value"),
        IntAttrDefine("value"),
        MainAxisAlignmentAttrDefine("value"),
        MainAxisSizeAttrDefine("value"),
        ParagraphAlignmentAttrDefine("value"),
        PlaceholderAlignmentAttrDefine("value"),
        StackFitAttrDefine("value"),
        TextOverflowAttrDefine("value"),
        TextWidthBasisAttrDefine("value"),
        VerticalDirectionAttrDefine("value"),
        NullableAlignmentAttrDefine("value"),
        NullableBaselineAttrDefine("value"),
        NullableBlendModeAttrDefine("value"),
        NullableBooleanAttrDefine("value"),
        NullableBorderSideAttrDefine("value"),
        NullableBoxFitAttrDefine("value"),
        NullableBoxShadowAttrDefine("value"),
        NullableColorAttrDefine("value"),
        NullableColorListAttrDefine("value"),
        NullableDecorationLineStyleAttrDefine("value"),
        NullableEdgeInsetsAttrDefine("value"),
        NullableFloatAttrDefine("value"),
        NullableFloatListAttrDefine("value"),
        NullableFontEdgingAttrDefine("value"),
        NullableFontFeatureListAttrDefine("value"),
        NullableFontHintingAttrDefine("value"),
        NullableFontStyleAttrDefine("value"),
        NullableGradientTypeAttrDefine("value"),
        NullableHeightModeAttrDefine("value"),
        NullableIntAttrDefine("value"),
        NullableMatrix44CMOAttrDefine("value"),
        NullableOffsetAttrDefine("value"),
        NullableRadiusAttrDefine("value"),
        NullableStringAttrDefine("value"),
        NullableTextDecorationAttrDefine("value"),
        NullableTextShadowAttrDefine("value"),
        NullableViewBoxAttrDefine("value"),
        RequiredAxisAttrDefine("value"),
        RequiredFloatAttrDefine("value"),
        RequiredMatrix44CMOAttrDefine("value"),
        RequiredStringAttrDefine("value"),
        RequiredSvgPathAttrDefine("value"),
        StringAttrDefine("value", ""),
        TextParser.ATTR_FOREGROUND_MODE,
        TextParser.ATTR_FOREGROUND_STROKE_CAP,
        TextParser.ATTR_FOREGROUND_STROKE_JOIN,
        UnconstrainedBoxParser().constrainedAxis,
        SnapshotElement.ATTR_TYPE,
        ClipPathParser.FIT,
        ClipPathParser.FILL_RULE,
        CommonAttrDefine.URL,
    )

    @Test
    fun built_in_examples_are_accepted_before_and_after_copy_with() {
        for (definition in definitions) {
            val info = assertNotNull(definition.valueInfo, definition.toString())
            assertTrue(info.format.isNotBlank())
            assertTrue(info.examples.isNotEmpty())
            val renamed = definition.copyWith("renamed")
            assertEquals(info, renamed.valueInfo, definition.toString())
            for (value in info.examples) {
                definition.parseValue(RawAttr(definition.name, value))
                renamed.parseValue(RawAttr(renamed.name, value))
            }
        }
    }

    @Test
    fun every_advertised_single_choice_is_accepted() {
        for (definition in definitions) {
            val info = assertNotNull(definition.valueInfo)
            if (info.format != "以下取值之一，区分大小写") continue
            for (value in info.allowedValues) {
                definition.parseValue(RawAttr(definition.name, value))
            }
        }
    }

    @Test
    fun data_uri_example_decodes_with_the_default_implementation() {
        for (value in AttributeValueFormats.DATA_URI.examples) {
            SimpleDataUriImageDecoder.decode(value).use { image ->
                assertEquals(1, image.width)
                assertEquals(1, image.height)
            }
        }
    }

    @Test
    fun legacy_constructors_defaults_and_boolean_behavior_remain_available() {
        val string = StringAttrDefine("value", "default") { _, value -> require(value != "bad") }
        assertEquals("default", string.parseValue(null as RawAttr?))
        assertEquals("test", string.parseValue(RawAttr("value", "test")))
        val enum = NullableEnumAttrDefine("value", PaintMode::valueOf)
        assertNull(enum.parseValue(null as RawAttr?))
        assertEquals(PaintMode.FILL, enum.parseValue(RawAttr("value", "FILL")))
        val bool = BooleanAttrDefine("flag", true)
        assertTrue(bool.parseValue(null as RawAttr?))
        assertTrue(bool.parseValue(RawAttr("flag")))
        assertFalse(bool.parseValue(RawAttr("flag", "typo")))
    }

    @Test
    fun custom_string_help_and_checker_survive_copy_with() {
        val info = AttributeValueInfo("偶数字符串", listOf("2"))
        val definition = StringAttrDefine("value", "2", info) { _, value ->
            require(value?.toInt()?.rem(2) == 0) { "必须填写偶数" }
        }.copyWith("renamed")
        val error = assertFailsWith<ParseException> {
            WidgetParser.parseAttrValue(definition, mapOf("renamed" to RawAttr("renamed", "3")))
        }
        assertEquals(info, error.attributeDiagnostic?.valueInfo)
        assertEquals("renamed", error.attributeDiagnostic?.attributeName)
        val required = RequiredStringAttrDefine("value", info) { _, value ->
            require(value?.toInt()?.rem(2) == 0)
        }.copyWith("renamed")
        assertFailsWith<IllegalArgumentException> { required.parseValue(RawAttr("renamed", "3")) }
        assertEquals(info, required.valueInfo)
    }
}

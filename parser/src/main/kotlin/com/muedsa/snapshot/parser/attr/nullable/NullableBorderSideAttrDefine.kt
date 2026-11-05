package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.EnumAttributeSyntax

import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BorderStyle
import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.CssColorSyntax
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.NumericAttributeSyntax

class NullableBorderSideAttrDefine(name: String) :
    DefaultValueAttrDefine<BorderSide?>(name = name, defaultValue = null) {

    override val valueInfo get() = AttributeValueFormats.BORDER

    override fun parseValue(valueStr: String?): BorderSide {
        requireNotNull(valueStr)
        if (valueStr.trim().equals("none", ignoreCase = true)) return BorderSide.NONE
        val valueArr = CssColorSyntax.splitOnWhitespace(valueStr)
        require(valueArr.size in 2..3) { "Attr [$name] requires width [style] color, or none" }
        return BorderSide(
            width = NumericAttributeSyntax.parse(valueArr[0], name, false).single(),
            style = if (valueArr.size == 3) BorderStyle.valueOf(EnumAttributeSyntax.normalize(valueArr[1])) else BorderStyle.SOLID,
            color = ColorAttrDefine.parseColorFromText(valueArr.last(), this),
        )
    }

    override fun copyWith(name: String, defaultValue: BorderSide?): NullableBorderSideAttrDefine =
        NullableBorderSideAttrDefine(name)
}

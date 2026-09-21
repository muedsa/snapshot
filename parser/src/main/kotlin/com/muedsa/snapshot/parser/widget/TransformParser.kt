package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableOffsetAttrDefine
import com.muedsa.snapshot.parser.attr.required.RequiredMatrix44CMOAttrDefine
import com.muedsa.snapshot.widget.Transform
import com.muedsa.snapshot.widget.Widget

open class TransformParser : WidgetParser {

    override val id: String = "Transform"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        Transform(
            transform = WidgetParser.parseAttrValue(MATRIX, element.attrs),
            origin = WidgetParser.parseAttrValue(ORIGIN, element.attrs),
            alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT_N, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }

    companion object {
        val MATRIX = RequiredMatrix44CMOAttrDefine("matrix")
        val ORIGIN = NullableOffsetAttrDefine("origin")
    }
}

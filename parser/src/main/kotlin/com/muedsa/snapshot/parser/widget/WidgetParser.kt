package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.bind

interface WidgetParser {

    val id: String

    val containerMode: ContainerMode

    fun buildWidget(element: Element): Widget

    companion object {

        fun <T> parseAttrValue(attrDefine: AttrDefine<T>, rawAttrMap: Map<String, RawAttr>): T {
            val rawAttr: RawAttr? = rawAttrMap[attrDefine.name]
            try {
                return attrDefine.parseValue(rawAttr)
            } catch (pex: ParseException) {
                throw pex
            } catch (t: Throwable) {
                if (rawAttr != null) throw ParseException(
                    rawAttr.nameStartPos,
                    t.message ?: "Parse attr [${attrDefine.name}] error",
                    t
                )
                else throw t
            }
        }

        fun createWidgetForChildElement(widget: Widget, children: List<Element>) {
            children.forEach {
                widget.bind(it.createWidget())
            }
        }
    }
}
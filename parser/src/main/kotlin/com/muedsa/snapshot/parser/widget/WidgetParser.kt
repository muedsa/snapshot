package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.widget.ChildSlot
import com.muedsa.snapshot.widget.Widget

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
                if (rawAttr == null) throw t

                val detail = t.message?.takeIf { it.isNotBlank() }
                val message = if (detail?.startsWith("Attr [${attrDefine.name}]") == true) {
                    detail
                } else {
                    "Attr [${attrDefine.name}] value is invalid" + (detail?.let { ": $it" } ?: "")
                }
                val position = rawAttr.valueStartPos.takeIf { it.hasSetting() } ?: rawAttr.nameStartPos
                throw ParseException(position.copy(), message, t)
            }
        }

        fun createWidgetForChildElement(widget: Widget, children: List<Element>) {
            if (children.isEmpty()) return
            val slot = widget as? ChildSlot
                ?: error(
                    "${widget::class.simpleName} has no child slot but got ${children.size} child element(s)"
                )
            children.forEach { slot.attach(it.createWidget()) }
        }
    }
}

package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.parser.widget.WidgetParser
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.bind

open class Element(
    val widgetParser: WidgetParser,
    val attrs: MutableMap<String, RawAttr>,
    val pos: TrackPos,
) {
    var owner: SnapshotElement? = null
    var parent: Element? = null
    private val _children: MutableList<Element> = mutableListOf()
    val children: List<Element> = _children

    fun appendChild(element: Element) {
        when (widgetParser.containerMode) {
            ContainerMode.NONE -> {
                throw IllegalStateException("Tag ${widgetParser.id} can not have child element ${element.widgetParser.id}")
            }

            ContainerMode.SINGLE -> {
                if (children.isNotEmpty()) {
                    throw IllegalStateException("Tag ${widgetParser.id} only can have one child, but get other ${element.widgetParser.id}")
                }
            }

            ContainerMode.MULTIPLE -> Unit
        }
        _children.add(element)
        element.parent = this
    }

    open fun createWidget(): Widget {
        val widget = try {
            widgetParser.buildWidget(this)
        } catch (pex: ParseException) {
            throw pex
        } catch (t: Throwable) {
            throw ParseException(pos, t.message ?: "Parse Tag ${widgetParser.id} error", t)
        }
        return widget
    }

    fun toTreeString(depth: Int): String {
        val prefix = if (depth == 0) "" else " ".repeat(depth * 2) + "|-"
        var s = "$prefix${widgetParser.id}(attrs=${attrs.values})"
        s += children.joinToString("", "\n") {
            it.toTreeString(depth + 1)
        }
        return s
    }

    override fun toString(): String = "${widgetParser.id}(\nattrs=$attrs\n){$children}"
}
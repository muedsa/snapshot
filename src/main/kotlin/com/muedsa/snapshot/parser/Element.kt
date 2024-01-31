package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.bind

open class Element(
    val tag: Tag,
    val attrs: Map<String, RawAttr>,
    val pos: TrackPos,
) {
    private val _children: MutableList<Element> = mutableListOf()
    val children: List<Element> = _children

    fun appendChild(element: Element) {
        when (tag.containerMode) {
            ContainerMode.NONE -> {
                throw IllegalStateException("Tag ${tag.id} can not have child element ${element.tag.id}")
            }

            ContainerMode.SINGLE -> {
                if (children.isNotEmpty()) {
                    throw IllegalStateException("Tag ${tag.id} only can have one child, but get other ${element.tag.id}")
                }
            }

            ContainerMode.MULTIPLE -> Unit
        }
        _children.add(element)
    }

    open fun createWidget(): Widget {
        val widget = try {
            tag.buildWidget(attrs)
        } catch (pex: ParseException) {
            throw pex
        } catch (t: Throwable) {
            throw ParseException(pos, t.message ?: "Parse Tag ${tag.id} error")
        }
        children.forEach {
            widget.bind(it.createWidget())
        }
        return widget
    }

    fun toTreeString(depth: Int): String {
        val prefix = if (depth == 0) "" else " ".repeat(depth * 2) + "|-"
        var s = "$prefix${tag.id}(attrs=${attrs.values})"
        s += children.joinToString("", "\n") {
            it.toTreeString(depth + 1)
        }
        return s
    }

    override fun toString(): String = "${tag.id}(\nattrs=$attrs\n){$children}"
}
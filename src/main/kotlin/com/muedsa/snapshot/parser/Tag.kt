package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.attr.*
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Widget


enum class Tag(
    val containerMode: ContainerMode,
) {
    SNAPSHOT(
        containerMode = ContainerMode.SINGLE
    ) {
        override fun buildWidget(rawAttrMap: Map<String, RawAttr>): Widget {
            throw IllegalCallerException("SNAPSHOT cant not buildWidget")
        }
    },
    CONTAINER(
        containerMode = ContainerMode.SINGLE
    ) {
        val alignment: AlignmentNullableAttrDefine = AlignmentNullableAttrDefine(name = "alignment")
        val padding: EdgeInsetsNullableAttrDefine = EdgeInsetsNullableAttrDefine("padding")
        val color: ColorNullableAttrDefine = ColorNullableAttrDefine(name = "color")
        val width: FloatNullableAttrDefine = FloatNullableAttrDefine(name = "width")
        val height: FloatNullableAttrDefine = FloatNullableAttrDefine(name = "height")
        val minWidth: FloatAttrDefine = FloatAttrDefine(name = "minWidth", defaultValue = 0f)
        val maxWidth: FloatAttrDefine = FloatAttrDefine(name = "maxWidth", defaultValue = Float.POSITIVE_INFINITY)
        val minHeight: FloatAttrDefine = FloatAttrDefine(name = "minHeight", defaultValue = 0f)
        val maxHeight: FloatAttrDefine = FloatAttrDefine(name = "maxHeight", defaultValue = Float.POSITIVE_INFINITY)
        val margin: EdgeInsetsNullableAttrDefine = EdgeInsetsNullableAttrDefine("margin")

        override fun buildWidget(rawAttrMap: Map<String, RawAttr>): Widget {
            val constraints: BoxConstraints? = if (
                rawAttrMap.containsKey(minWidth.name)
                || rawAttrMap.containsKey(maxWidth.name)
                || rawAttrMap.containsKey(minHeight.name)
                || rawAttrMap.containsKey(maxHeight.name)
            ) {
                BoxConstraints(
                    minWidth = parseAttrValue(minWidth, rawAttrMap),
                    maxWidth = parseAttrValue(maxWidth, rawAttrMap),
                    minHeight = parseAttrValue(minHeight, rawAttrMap),
                    maxHeight = parseAttrValue(maxHeight, rawAttrMap)
                )
            } else null

            return Container(
                alignment = parseAttrValue(alignment, rawAttrMap),
                padding = parseAttrValue(padding, rawAttrMap),
                color = parseAttrValue(color, rawAttrMap),
                width = parseAttrValue(width, rawAttrMap),
                height = parseAttrValue(height, rawAttrMap),
                constraints = constraints,
                margin = parseAttrValue(margin, rawAttrMap)
            )
        }
    }
    ;

    val id: String = name.split("_").joinToString("") {
        it.lowercase().replaceFirstChar { c -> c.uppercase() }
    }

    abstract fun buildWidget(rawAttrMap: Map<String, RawAttr>): Widget

    companion object {

        val TAG_MAP: Map<String, Tag> = entries.associateBy { it.id }.also {
            check(it.size == entries.size) {
                "Duplicate tag id"
            }
        }

        fun <T> parseAttrValue(attrDefine: AttrDefine<T>, rawAttrMap: Map<String, RawAttr>): T {
            val rawAttr: RawAttr? = rawAttrMap[attrDefine.name]
            try {
                return attrDefine.parseValue(rawAttr)
            } catch (pex: ParseException) {
                throw pex
            } catch (t: Throwable) {
                if (rawAttr != null) throw ParseException(
                    rawAttr.nameStartPos,
                    t.message ?: "Parse attr ${attrDefine.name} error"
                )
                else throw t
            }
        }
    }
}
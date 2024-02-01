package com.muedsa.snapshot.parser

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.widget.*


enum class Tag(
    val containerMode: ContainerMode,
) {
    SNAPSHOT(
        containerMode = ContainerMode.SINGLE
    ) {
        override fun buildWidget(element: Element): Widget {
            throw IllegalCallerException("SNAPSHOT cant not buildWidget")
        }
    },
    CONTAINER(
        containerMode = ContainerMode.SINGLE
    ) {
        override fun buildWidget(element: Element): Widget {
            val constraints: BoxConstraints? = if (
                element.attrs.containsKey(CommonAttrDefine.MIN_WIDTH.name)
                || element.attrs.containsKey(CommonAttrDefine.MAX_WIDTH.name)
                || element.attrs.containsKey(CommonAttrDefine.MIN_HEIGHT.name)
                || element.attrs.containsKey(CommonAttrDefine.MAX_HEIGHT.name)
            ) {
                BoxConstraints(
                    minWidth = parseAttrValue(CommonAttrDefine.MIN_WIDTH, element.attrs),
                    maxWidth = parseAttrValue(CommonAttrDefine.MAX_WIDTH, element.attrs),
                    minHeight = parseAttrValue(CommonAttrDefine.MIN_HEIGHT, element.attrs),
                    maxHeight = parseAttrValue(CommonAttrDefine.MAX_HEIGHT, element.attrs)
                )
            } else null

            return Container(
                alignment = parseAttrValue(CommonAttrDefine.ALIGNMENT_N, element.attrs),
                padding = parseAttrValue(CommonAttrDefine.PADDING_N, element.attrs),
                color = parseAttrValue(CommonAttrDefine.COLOR_N, element.attrs),
                width = parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs),
                height = parseAttrValue(CommonAttrDefine.HEIGHT_N, element.attrs),
                constraints = constraints,
                margin = parseAttrValue(CommonAttrDefine.MARGIN_N, element.attrs)
            )
        }
    },
    ROW(
        containerMode = ContainerMode.MULTIPLE
    ) {
        val textDirection = CommonAttrDefine.DIRECTION.copyWith("textDirection")
        val textBaseline = CommonAttrDefine.BASELINE.copyWith("textBaseline")

        override fun buildWidget(element: Element): Widget =
            Row(
                mainAxisAlignment = parseAttrValue(CommonAttrDefine.MAIN_AXIS_ALIGNMENT, element.attrs),
                mainAxisSize = parseAttrValue(CommonAttrDefine.MAIN_AXIS_SIZE, element.attrs),
                crossAxisAlignment = parseAttrValue(CommonAttrDefine.CROSS_AXIS_ALIGNMENT, element.attrs),
                textDirection = parseAttrValue(textDirection, element.attrs),
                verticalDirection = parseAttrValue(CommonAttrDefine.VERTICAL_DIRECTION, element.attrs),
                textBaseline = parseAttrValue(textBaseline, element.attrs)
            )

    },
    COLUMN(
        containerMode = ContainerMode.MULTIPLE
    ) {
        val textDirection = CommonAttrDefine.DIRECTION.copyWith("textDirection")
        val textBaseline = CommonAttrDefine.BASELINE.copyWith("textBaseline")

        override fun buildWidget(element: Element): Widget = Column(
            mainAxisAlignment = parseAttrValue(CommonAttrDefine.MAIN_AXIS_ALIGNMENT, element.attrs),
            mainAxisSize = parseAttrValue(CommonAttrDefine.MAIN_AXIS_SIZE, element.attrs),
            crossAxisAlignment = parseAttrValue(CommonAttrDefine.CROSS_AXIS_ALIGNMENT, element.attrs),
            textDirection = parseAttrValue(textDirection, element.attrs),
            verticalDirection = parseAttrValue(CommonAttrDefine.VERTICAL_DIRECTION, element.attrs),
            textBaseline = parseAttrValue(textBaseline, element.attrs)
        )
    },
    STACK(
        containerMode = ContainerMode.MULTIPLE
    ) {
        val alignment = CommonAttrDefine.ALIGNMENT.copyWith(name = "alignment", defaultValue = BoxAlignment.TOP_LEFT)
        val textDirection = CommonAttrDefine.DIRECTION.copyWith("textDirection")

        override fun buildWidget(element: Element): Widget = Stack(
            alignment = parseAttrValue(alignment, element.attrs),
            textDirection = parseAttrValue(textDirection, element.attrs)
        )
    },
    IMAGE(
        containerMode = ContainerMode.NONE
    ) {
        val colorBlendMode = CommonAttrDefine.BLEND_MODE_N.copyWith("colorBlendMode")

        override fun buildWidget(element: Element): Widget = CachedNetworkImage(
            url = parseAttrValue(CommonAttrDefine.URL, element.attrs),
            width = parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs),
            height = parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs),
            alignment = parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs),
            repeat = parseAttrValue(CommonAttrDefine.REPEAT, element.attrs),
            scale = parseAttrValue(CommonAttrDefine.SCALE, element.attrs),
            opacity = parseAttrValue(CommonAttrDefine.OPACITY, element.attrs),
            color = parseAttrValue(CommonAttrDefine.COLOR_N, element.attrs),
            colorBlendMode = parseAttrValue(colorBlendMode, element.attrs)
            // TODO 为SnapshotElement单独提供一个可以限制大小的NetworkImageCache
            // cache = element.owner!!.networkImageCache
        )
    },
    TEXT(
        containerMode = ContainerMode.NONE
    ) {
        @OptIn(ExperimentalStdlibApi::class)
        override fun buildWidget(element: Element): Widget =
            SimpleText(
                text = parseAttrValue(CommonAttrDefine.TEXT, element.attrs),
                color = parseAttrValue(CommonAttrDefine.COLOR, element.attrs),
                fontSize = parseAttrValue(CommonAttrDefine.FONT_SIZE, element.attrs),
                fontFamilyName = parseAttrValue(CommonAttrDefine.FONT_FAMILY_N, element.attrs)?.split(",")
                    ?.toTypedArray(),
                fontStyle = parseAttrValue(CommonAttrDefine.FONT_STYLE, element.attrs)
            )
    },
    ;

    val id: String = name.split("_").joinToString("") {
        it.lowercase().replaceFirstChar { c -> c.uppercase() }
    }

    abstract fun buildWidget(element: Element): Widget

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
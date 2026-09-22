package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.MATH_PI
import com.muedsa.snapshot.paint.gradient.Gradient
import com.muedsa.snapshot.paint.gradient.GradientRotation
import com.muedsa.snapshot.paint.gradient.LinearGradient
import com.muedsa.snapshot.paint.gradient.RadialGradient
import com.muedsa.snapshot.paint.gradient.SweepGradient
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.GradientType
import com.muedsa.snapshot.parser.attr.nullable.NullableColorListAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFloatAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFloatListAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableGradientTypeAttrDefine

object GradientParser {

    private val TYPE = NullableGradientTypeAttrDefine("gradientType")
    private val COLORS = NullableColorListAttrDefine("gradientColors")
    private val STOPS = NullableFloatListAttrDefine("gradientStops")
    private val ROTATION = NullableFloatAttrDefine("gradientRotation")
    private val BEGIN = CommonAttrDefine.ALIGNMENT_N.copyWith("gradientBegin")
    private val END = CommonAttrDefine.ALIGNMENT_N.copyWith("gradientEnd")
    private val CENTER = CommonAttrDefine.ALIGNMENT_N.copyWith("gradientCenter")
    private val RADIUS = NullableFloatAttrDefine("gradientRadius")
    private val FOCAL = CommonAttrDefine.ALIGNMENT_N.copyWith("gradientFocal")
    private val FOCAL_RADIUS = NullableFloatAttrDefine("gradientFocalRadius")
    private val START_ANGLE = NullableFloatAttrDefine("gradientStartAngle")
    private val END_ANGLE = NullableFloatAttrDefine("gradientEndAngle")
    private val TILE_MODE = CommonAttrDefine.FILTER_TILE_MODE.copyWith("gradientTileMode")

    private val ALL_ATTR_NAMES = listOf(
        TYPE.name,
        COLORS.name,
        STOPS.name,
        ROTATION.name,
        BEGIN.name,
        END.name,
        CENTER.name,
        RADIUS.name,
        FOCAL.name,
        FOCAL_RADIUS.name,
        START_ANGLE.name,
        END_ANGLE.name,
        TILE_MODE.name,
    )

    fun parseGradient(element: Element, prefix: String = ""): Gradient? {
        val type = WidgetParser.parseAttrValue(TYPE.copyWith(prefixedName(prefix, TYPE.name)), element.attrs)
        if (type == null) {
            val unexpectedName = ALL_ATTR_NAMES
                .asSequence()
                .drop(1)
                .map { prefixedName(prefix, it) }
                .firstOrNull(element.attrs::containsKey)
            require(unexpectedName == null) {
                "Attr [${prefixedName(prefix, TYPE.name)}] is required when [$unexpectedName] is specified"
            }
            return null
        }

        val colors = requireNotNull(
            WidgetParser.parseAttrValue(COLORS.copyWith(prefixedName(prefix, COLORS.name)), element.attrs)
        ) { "Attr [${prefixedName(prefix, COLORS.name)}] is required" }
        val stops = WidgetParser.parseAttrValue(STOPS.copyWith(prefixedName(prefix, STOPS.name)), element.attrs)
        validateStops(colors, stops, prefix)

        val rotation = WidgetParser.parseAttrValue(
            ROTATION.copyWith(prefixedName(prefix, ROTATION.name)),
            element.attrs
        )?.also { value ->
            require(value.isFinite()) { "Attr [${prefixedName(prefix, ROTATION.name)}] must be finite" }
        }?.let(::GradientRotation)
        val tileMode = WidgetParser.parseAttrValue(
            TILE_MODE.copyWith(prefixedName(prefix, TILE_MODE.name)),
            element.attrs
        )

        return when (type) {
            GradientType.LINEAR -> {
                requireAttrsAbsent(
                    element,
                    prefix,
                    CENTER.name,
                    RADIUS.name,
                    FOCAL.name,
                    FOCAL_RADIUS.name,
                    START_ANGLE.name,
                    END_ANGLE.name,
                )
                LinearGradient(
                    begin = WidgetParser.parseAttrValue(
                        BEGIN.copyWith(prefixedName(prefix, BEGIN.name)),
                        element.attrs
                    ) ?: BoxAlignment.CENTER_LEFT,
                    end = WidgetParser.parseAttrValue(
                        END.copyWith(prefixedName(prefix, END.name)),
                        element.attrs
                    ) ?: BoxAlignment.CENTER_RIGHT,
                    colors = colors,
                    stops = stops,
                    tileMode = tileMode,
                    transform = rotation,
                )
            }

            GradientType.RADIAL -> {
                requireAttrsAbsent(element, prefix, BEGIN.name, END.name, START_ANGLE.name, END_ANGLE.name)
                val radius = parsePositiveFloat(element, prefix, RADIUS, 0.5f)
                val focal = WidgetParser.parseAttrValue(
                    FOCAL.copyWith(prefixedName(prefix, FOCAL.name)),
                    element.attrs
                )
                require(focal != null || !element.attrs.containsKey(prefixedName(prefix, FOCAL_RADIUS.name))) {
                    "Attr [${prefixedName(prefix, FOCAL.name)}] is required when " +
                            "[${prefixedName(prefix, FOCAL_RADIUS.name)}] is specified"
                }
                val focalRadius = parseNonNegativeFloat(element, prefix, FOCAL_RADIUS, 0.5f)
                RadialGradient(
                    center = WidgetParser.parseAttrValue(
                        CENTER.copyWith(prefixedName(prefix, CENTER.name)),
                        element.attrs
                    ) ?: BoxAlignment.CENTER,
                    radius = radius,
                    colors = colors,
                    stops = stops,
                    tileMode = tileMode,
                    focal = focal,
                    focalRadius = focalRadius,
                    transform = rotation,
                )
            }

            GradientType.SWEEP -> {
                requireAttrsAbsent(
                    element,
                    prefix,
                    BEGIN.name,
                    END.name,
                    RADIUS.name,
                    FOCAL.name,
                    FOCAL_RADIUS.name,
                )
                val startAngle = parseFiniteFloat(element, prefix, START_ANGLE, 0f)
                val endAngle = parseFiniteFloat(element, prefix, END_ANGLE, MATH_PI * 2)
                require(startAngle < endAngle) {
                    "Attr [${prefixedName(prefix, START_ANGLE.name)}] must be less than " +
                            "[${prefixedName(prefix, END_ANGLE.name)}]"
                }
                SweepGradient(
                    center = WidgetParser.parseAttrValue(
                        CENTER.copyWith(prefixedName(prefix, CENTER.name)),
                        element.attrs
                    ) ?: BoxAlignment.CENTER,
                    startAngle = startAngle,
                    endAngle = endAngle,
                    colors = colors,
                    stops = stops,
                    tileMode = tileMode,
                    transform = rotation,
                )
            }
        }
    }

    private fun validateStops(colors: IntArray, stops: FloatArray?, prefix: String) {
        if (stops == null) return
        val attrName = prefixedName(prefix, STOPS.name)
        require(stops.size == colors.size) { "Attr [$attrName] count must match gradient colors" }
        require(stops.all { it in 0f..1f }) { "Attr [$attrName] values must be between 0 and 1" }
        require(stops.asList().zipWithNext().all { (left, right) -> left <= right }) {
            "Attr [$attrName] values must be in ascending order"
        }
    }

    private fun parseNonNegativeFloat(
        element: Element,
        prefix: String,
        attrDefine: NullableFloatAttrDefine,
        defaultValue: Float,
    ): Float {
        val value = parseFiniteFloat(element, prefix, attrDefine, defaultValue)
        require(value >= 0f) { "Attr [${prefixedName(prefix, attrDefine.name)}] must not be negative" }
        return value
    }

    private fun parsePositiveFloat(
        element: Element,
        prefix: String,
        attrDefine: NullableFloatAttrDefine,
        defaultValue: Float,
    ): Float {
        val value = parseFiniteFloat(element, prefix, attrDefine, defaultValue)
        require(value > 0f) { "Attr [${prefixedName(prefix, attrDefine.name)}] must be positive" }
        return value
    }

    private fun parseFiniteFloat(
        element: Element,
        prefix: String,
        attrDefine: NullableFloatAttrDefine,
        defaultValue: Float,
    ): Float {
        val value = WidgetParser.parseAttrValue(
            attrDefine.copyWith(prefixedName(prefix, attrDefine.name)),
            element.attrs
        ) ?: defaultValue
        require(value.isFinite()) { "Attr [${prefixedName(prefix, attrDefine.name)}] must be finite" }
        return value
    }

    private fun requireAttrsAbsent(element: Element, prefix: String, vararg attrNames: String) {
        val unexpectedName = attrNames
            .asSequence()
            .map { prefixedName(prefix, it) }
            .firstOrNull(element.attrs::containsKey)
        require(unexpectedName == null) { "Attr [$unexpectedName] is not supported by this gradient type" }
    }

    private fun prefixedName(prefix: String, name: String): String =
        if (prefix.isEmpty()) name else prefix + name.replaceFirstChar { it.uppercaseChar() }
}

package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.material.ELEVATION_MAP
import com.muedsa.snapshot.paint.decoration.BoxShadow
import com.muedsa.snapshot.parser.attr.AttrStrValueConst
import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterBlurMode

class NullableBoxShadowAttrDefine(name: String) :
    DefaultValueAttrDefine<Array<BoxShadow>?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): Array<BoxShadow> {
        requireNotNull(valueStr)
        return if (valueStr.startsWith(ELEVATION_PREFIX)) {
            val result: MatchResult? = ELEVATION_REGEX.find(valueStr)
            check(result != null) { "elevation only allow use $ELEVATION_REGEX${ELEVATION_MAP.keys}" }
            val e = result.groupValues[1].toInt()
            val boxShadows = ELEVATION_MAP[e]
            check(boxShadows != null) {
                "not exist elevation $e, only use $ELEVATION_REGEX${ELEVATION_MAP.keys}"
            }
            boxShadows
        } else {
            val shadowStrArr = valueStr.split(",")
            Array(shadowStrArr.size) {
                val params = shadowStrArr[it].split(" ")
                check(params.size in 2..6) { "boxShadows format error" }
                val offset = Offset(
                    x = params[0].toFloat(),
                    y = params[1].toFloat(),
                )
                var color: Int = Color.BLACK
                var blurRadius = 0f
                var spreadRadius = 0f
                var blurStyle = FilterBlurMode.NORMAL
                var i = 2
                while (i < params.size) {
                    if (AttrStrValueConst.ONE_FLOAT_VALUE_REGEX.matches(params[i])) {
                        check(i < 4) { "boxShadows format error" }
                        if (i == 2) {
                            blurRadius = params[i].toFloat()
                        } else {
                            spreadRadius = params[i].toFloat()
                        }
                    } else if (params[i].startsWith("#")) {
                        color = ColorAttrDefine.parseColorFromText(params[i], this)
                    } else {
                        blurStyle = FilterBlurMode.valueOf(params[i])
                    }
                    i++
                }
                BoxShadow(
                    color = color,
                    offset = offset,
                    blurRadius = blurRadius,
                    spreadRadius = spreadRadius,
                    blurStyle = blurStyle
                )
            }
        }
    }

    override fun copyWith(name: String, defaultValue: Array<BoxShadow>?): NullableBoxShadowAttrDefine =
        NullableBoxShadowAttrDefine(name)

    companion object {
        const val ELEVATION_PREFIX = "ELEVATION_"
        val ELEVATION_REGEX: Regex = """^$ELEVATION_PREFIX(\d+)$""".toRegex()
    }
}

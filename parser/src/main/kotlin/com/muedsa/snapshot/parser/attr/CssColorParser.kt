package com.muedsa.snapshot.parser.attr

import org.jetbrains.skia.Color
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

/** 将常用 CSS 颜色表示转换为 Skia 使用的 ARGB Int。 */
internal object CssColorParser {

    fun parse(value: String, attrName: String): Int {
        val text = value.trim()
        require(text.isNotEmpty()) { "Attr [$attrName] color value can not be empty" }
        if (text.startsWith('#')) return parseHex(text, attrName)
        val lower = text.lowercase(Locale.ROOT)
        if (lower == "transparent") return Color.TRANSPARENT
        namedColors[lower]?.let { return it }
        return parseFunction(text, attrName)
    }

    fun isColorCandidate(value: String): Boolean {
        val lower = value.lowercase(Locale.ROOT)
        return value.startsWith('#') || value.contains('(') || lower == "transparent" || lower in namedColors
    }

    private fun parseHex(text: String, attrName: String): Int {
        val hex = text.substring(1)
        require(hex.length in listOf(3, 4, 6, 8) && hex.all { it.isHexDigit() }) {
            "Attr [$attrName] color must be #RGB, #RGBA, #RRGGBB or #RRGGBBAA"
        }
        val components = if (hex.length <= 4) {
            hex.map { it.digitToInt(16) * 17 }
        } else {
            hex.chunked(2).map { it.toInt(16) }
        }
        return Color.makeARGB(
            a = components.getOrElse(3) { 255 },
            r = components[0],
            g = components[1],
            b = components[2],
        )
    }

    private fun parseFunction(text: String, attrName: String): Int {
        val open = text.indexOf('(')
        require(open > 0 && text.endsWith(')')) { "Attr [$attrName] unsupported CSS color [$text]" }
        val name = text.substring(0, open).lowercase(Locale.ROOT)
        require(name in listOf("rgb", "rgba", "hsl", "hsla")) {
            "Attr [$attrName] unsupported CSS color function [$name]"
        }
        val body = text.substring(open + 1, text.length - 1)
        val (channels, alphaText) = splitFunctionArguments(body, attrName)
        val alpha = alphaText?.let { parseAlpha(it, attrName) } ?: 255
        return when (name) {
            "rgb", "rgba" -> Color.makeARGB(
                alpha,
                parseRgbChannel(channels[0], attrName),
                parseRgbChannel(channels[1], attrName),
                parseRgbChannel(channels[2], attrName),
            )
            else -> parseHsl(channels, alpha, attrName)
        }
    }

    private fun splitFunctionArguments(body: String, attrName: String): Pair<List<String>, String?> {
        val channels: List<String>
        val alpha: String?
        if (',' in body) {
            require('/' !in body) { "Attr [$attrName] can not mix comma and slash color syntax" }
            val parts = CssColorSyntax.splitOn(body, ',')
            require(parts.size in 3..4) { "Attr [$attrName] color function needs three channels and optional alpha" }
            channels = parts.take(3)
            alpha = parts.getOrNull(3)
        } else {
            val parts = CssColorSyntax.splitOn(body, '/')
            require(parts.size in 1..2) { "Attr [$attrName] color function has too many alpha separators" }
            channels = CssColorSyntax.splitOnWhitespace(parts[0])
            alpha = parts.getOrNull(1)
        }
        require(channels.size == 3 && channels.none(String::isEmpty) && alpha != "") {
            "Attr [$attrName] color function needs three channels and optional alpha"
        }
        return channels to alpha
    }

    private fun parseRgbChannel(text: String, attrName: String): Int {
        val value = if (text.endsWith('%')) {
            finiteNumber(text.dropLast(1), attrName).coerceIn(0.0, 100.0) * 255.0 / 100.0
        } else {
            finiteNumber(text, attrName).coerceIn(0.0, 255.0)
        }
        return value.roundToInt()
    }

    private fun parseAlpha(text: String, attrName: String): Int {
        val value = if (text.endsWith('%')) {
            finiteNumber(text.dropLast(1), attrName) / 100.0
        } else {
            finiteNumber(text, attrName)
        }
        return (value.coerceIn(0.0, 1.0) * 255.0).roundToInt()
    }

    private fun parseHsl(channels: List<String>, alpha: Int, attrName: String): Int {
        val hue = parseHue(channels[0], attrName).mod(360.0)
        val saturation = parsePercentage(channels[1], attrName)
        val lightness = parsePercentage(channels[2], attrName)
        val chroma = (1.0 - abs(2.0 * lightness - 1.0)) * saturation
        val x = chroma * (1.0 - abs((hue / 60.0).mod(2.0) - 1.0))
        val match = lightness - chroma / 2.0
        val (r, g, b) = when {
            hue < 60.0 -> Triple(chroma, x, 0.0)
            hue < 120.0 -> Triple(x, chroma, 0.0)
            hue < 180.0 -> Triple(0.0, chroma, x)
            hue < 240.0 -> Triple(0.0, x, chroma)
            hue < 300.0 -> Triple(x, 0.0, chroma)
            else -> Triple(chroma, 0.0, x)
        }
        return Color.makeARGB(
            alpha,
            ((r + match) * 255.0).roundToInt().coerceIn(0, 255),
            ((g + match) * 255.0).roundToInt().coerceIn(0, 255),
            ((b + match) * 255.0).roundToInt().coerceIn(0, 255),
        )
    }

    private fun parseHue(text: String, attrName: String): Double {
        val lower = text.lowercase(Locale.ROOT)
        val (number, multiplier) = when {
            lower.endsWith("turn") -> lower.dropLast(4) to 360.0
            lower.endsWith("grad") -> lower.dropLast(4) to 0.9
            lower.endsWith("rad") -> lower.dropLast(3) to (180.0 / PI)
            lower.endsWith("deg") -> lower.dropLast(3) to 1.0
            else -> lower to 1.0
        }
        val degrees = finiteNumber(number, attrName) * multiplier
        require(degrees.isFinite()) { "Attr [$attrName] invalid CSS hue [$text]" }
        return degrees
    }

    private fun parsePercentage(text: String, attrName: String): Double {
        require(text.endsWith('%')) { "Attr [$attrName] HSL saturation and lightness must be percentages" }
        return (finiteNumber(text.dropLast(1), attrName) / 100.0).coerceIn(0.0, 1.0)
    }

    private fun finiteNumber(text: String, attrName: String): Double {
        val value = text.toDoubleOrNull()
        require(value != null && value.isFinite()) { "Attr [$attrName] invalid CSS color number [$text]" }
        return value
    }

    private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

    private val namedColors: Map<String, Int> = """
        aliceblue:F0F8FF antiquewhite:FAEBD7 aqua:00FFFF aquamarine:7FFFD4 azure:F0FFFF beige:F5F5DC bisque:FFE4C4 black:000000 blanchedalmond:FFEBCD blue:0000FF blueviolet:8A2BE2 brown:A52A2A burlywood:DEB887 cadetblue:5F9EA0 chartreuse:7FFF00 chocolate:D2691E coral:FF7F50 cornflowerblue:6495ED cornsilk:FFF8DC crimson:DC143C cyan:00FFFF
        darkblue:00008B darkcyan:008B8B darkgoldenrod:B8860B darkgray:A9A9A9 darkgreen:006400 darkgrey:A9A9A9 darkkhaki:BDB76B darkmagenta:8B008B darkolivegreen:556B2F darkorange:FF8C00 darkorchid:9932CC darkred:8B0000 darksalmon:E9967A darkseagreen:8FBC8F darkslateblue:483D8B darkslategray:2F4F4F darkslategrey:2F4F4F darkturquoise:00CED1 darkviolet:9400D3 deeppink:FF1493 deepskyblue:00BFFF dimgray:696969 dimgrey:696969 dodgerblue:1E90FF
        firebrick:B22222 floralwhite:FFFAF0 forestgreen:228B22 fuchsia:FF00FF gainsboro:DCDCDC ghostwhite:F8F8FF gold:FFD700 goldenrod:DAA520 gray:808080 green:008000 greenyellow:ADFF2F grey:808080 honeydew:F0FFF0 hotpink:FF69B4 indianred:CD5C5C indigo:4B0082 ivory:FFFFF0 khaki:F0E68C lavender:E6E6FA lavenderblush:FFF0F5 lawngreen:7CFC00 lemonchiffon:FFFACD lightblue:ADD8E6 lightcoral:F08080 lightcyan:E0FFFF lightgoldenrodyellow:FAFAD2
        lightgray:D3D3D3 lightgreen:90EE90 lightgrey:D3D3D3 lightpink:FFB6C1 lightsalmon:FFA07A lightseagreen:20B2AA lightskyblue:87CEFA lightslategray:778899 lightslategrey:778899 lightsteelblue:B0C4DE lightyellow:FFFFE0 lime:00FF00 limegreen:32CD32 linen:FAF0E6 magenta:FF00FF maroon:800000 mediumaquamarine:66CDAA mediumblue:0000CD mediumorchid:BA55D3 mediumpurple:9370DB mediumseagreen:3CB371 mediumslateblue:7B68EE mediumspringgreen:00FA9A mediumturquoise:48D1CC mediumvioletred:C71585
        midnightblue:191970 mintcream:F5FFFA mistyrose:FFE4E1 moccasin:FFE4B5 navajowhite:FFDEAD navy:000080 oldlace:FDF5E6 olive:808000 olivedrab:6B8E23 orange:FFA500 orangered:FF4500 orchid:DA70D6 palegoldenrod:EEE8AA palegreen:98FB98 paleturquoise:AFEEEE palevioletred:DB7093 papayawhip:FFEFD5 peachpuff:FFDAB9 peru:CD853F pink:FFC0CB plum:DDA0DD powderblue:B0E0E6 purple:800080 rebeccapurple:663399 red:FF0000 rosybrown:BC8F8F royalblue:4169E1 saddlebrown:8B4513
        salmon:FA8072 sandybrown:F4A460 seagreen:2E8B57 seashell:FFF5EE sienna:A0522D silver:C0C0C0 skyblue:87CEEB slateblue:6A5ACD slategray:708090 slategrey:708090 snow:FFFAFA springgreen:00FF7F steelblue:4682B4 tan:D2B48C teal:008080 thistle:D8BFD8 tomato:FF6347 turquoise:40E0D0 violet:EE82EE wheat:F5DEB3 white:FFFFFF whitesmoke:F5F5F5 yellow:FFFF00 yellowgreen:9ACD32
    """.trimIndent().split(Regex("\\s+")).associate { entry ->
        val (name, hex) = entry.split(':')
        name to (0xFF000000.toInt() or hex.toInt(16))
    }
}

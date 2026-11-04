package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.Matrix44CMO
import java.util.Locale
import kotlin.math.PI

/** 按书写顺序右乘变换，与 Matrix44CMO 的 translate/rotateZ/scale 一致。 */
internal object TransformAttributeSyntax {
    private val FUNCTION = Regex("([A-Za-z]+)\\s*\\(([^()]*)\\)")
    private val ANGLE = Regex("(.+?)(deg|rad)", RegexOption.IGNORE_CASE)

    fun parse(value: String, attrName: String): Matrix44CMO {
        val text = value.trim()
        if (text.equals("none", ignoreCase = true)) return Matrix44CMO.identity()
        if (text.firstOrNull()?.isLetter() != true) {
            val body = if (text.startsWith('(') && text.endsWith(')')) text.substring(1, text.lastIndex) else text
            val numbers = NumericAttributeSyntax.parse(body, attrName, ',' in body)
            require(numbers.size == 16) { "Attr [$attrName] must contain 16 values" }
            return Matrix44CMO(*numbers.toFloatArray())
        }

        val matrix = Matrix44CMO.identity()
        var index = 0
        while (index < text.length) {
            if (text[index].isWhitespace()) {
                index++
                continue
            }
            val function = FUNCTION.find(text, index)
            require(function != null && function.range.first == index) { "Attr [$attrName] invalid transform at position $index" }
            val name = function.groupValues[1].lowercase(Locale.ROOT)
            val arguments = function.groupValues[2].trim()
            when (name) {
                "translate", "scale" -> {
                    val numbers = NumericAttributeSyntax.parse(arguments, attrName, ',' in arguments)
                    require(numbers.size in 1..2) { "Attr [$attrName] $name requires 1 or 2 numbers" }
                    if (name == "translate") matrix.translate(numbers[0], numbers.getOrElse(1) { 0f })
                    else matrix.scale(numbers[0], numbers.getOrElse(1) { numbers[0] }, 1f)
                }
                "rotate" -> {
                    val angle = ANGLE.matchEntire(arguments)
                    requireNotNull(angle) { "Attr [$attrName] rotate requires an angle with deg or rad" }
                    val numbers = NumericAttributeSyntax.parse(angle.groupValues[1], attrName, false)
                    require(numbers.size == 1) { "Attr [$attrName] rotate requires one angle" }
                    val radians = if (angle.groupValues[2].equals("deg", ignoreCase = true)) {
                        (numbers[0].toDouble() * PI / 180.0).toFloat()
                    } else numbers[0]
                    matrix.rotateZ(radians)
                }
                else -> throw IllegalArgumentException("Attr [$attrName] unknown transform [$name]; use translate, rotate or scale")
            }
            require(matrix.mat.all { it.isFinite() }) { "Attr [$attrName] composed matrix values must be finite" }
            index = function.range.last + 1
        }
        return matrix
    }
}

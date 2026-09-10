package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Radius
import com.muedsa.snapshot.paint.decoration.*
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.DecoratedBox
import org.jetbrains.skia.FilterBlurMode
import kotlin.test.Test
import kotlin.test.assertTrue

class BorderParserTest {

    @Test
    fun build_widget_test() {
        val text = """
            <Snapshot>
                <Border border="2 SOLID #FF00FF00" 
                        borderLeft="1 SOLID #FFFFFF00" 
                        borderRadius="(1.2,2.3)" 
                        borderRadiusBottomLeft="5"
                        boxShadow="5 7 3 2 #FF00FF00 SOLID"/>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = ParserTest.parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is DecoratedBox, "widget is DecoratedBox")
        val decoratedBox: DecoratedBox = widget as DecoratedBox
        assertTrue(decoratedBox.decoration is BoxDecoration, "decoratedBox.decoration is BoxDecoration")
        val boxDecorate: BoxDecoration = decoratedBox.decoration as BoxDecoration
        assertTrue(boxDecorate.border is Border, "boxDecorate.border is Border")
        val border: Border = boxDecorate.border as Border
        assertTrue(
            border.left == BorderSide(
                width = 1f,
                style = BorderStyle.SOLID,
                color = 0xFF_FF_FF_00.toInt()
            ),
            "border.left 应等于声明的 BorderSide"
        )
        val defaultBorderSide = BorderSide(
            width = 2f,
            style = BorderStyle.SOLID,
            color = 0xFF_00_FF_00.toInt()
        )
        assertTrue(border.top == defaultBorderSide, "border.top == defaultBorderSide")
        assertTrue(border.right == defaultBorderSide, "border.right == defaultBorderSide")
        assertTrue(border.bottom == defaultBorderSide, "border.bottom == defaultBorderSide")
        val borderRadius = boxDecorate.borderRadius!!
        val defaultRadius = Radius(1.2f, 2.3f)
        assertTrue(borderRadius.topLeft == defaultRadius, "borderRadius.topLeft == defaultRadius")
        assertTrue(borderRadius.topRight == defaultRadius, "borderRadius.topRight == defaultRadius")
        assertTrue(borderRadius.bottomLeft == Radius(5f, 5f), "borderRadius.bottomLeft == Radius(5f, 5f)")
        assertTrue(borderRadius.bottomRight == defaultRadius, "borderRadius.bottomRight == defaultRadius")
        assertTrue(boxDecorate.boxShadow != null && boxDecorate.boxShadow!!.size == 1, "boxDecorate.boxShadow != null && boxDecorate.boxShadow!!.size == 1")
        assertTrue(
            boxDecorate.boxShadow!![0] == BoxShadow(
                color = 0xFF_00_FF_00.toInt(),
                offset = Offset(5f, 7f),
                blurRadius = 3f,
                spreadRadius = 2f,
                blurStyle = FilterBlurMode.SOLID
            ),
            "boxShadow[0] 应等于声明的 BoxShadow"
        )
    }

    @Test
    fun build_widget_without_default_test() {
        val text = """
            <Snapshot>
                <Border borderLeft="1 SOLID #FFFFFF00" borderRadiusBottomLeft="(2.1,3.4)"/>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = ParserTest.parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is DecoratedBox, "widget is DecoratedBox")
        val decoratedBox: DecoratedBox = widget as DecoratedBox
        assertTrue(decoratedBox.decoration is BoxDecoration, "decoratedBox.decoration is BoxDecoration")
        val boxDecorate: BoxDecoration = decoratedBox.decoration as BoxDecoration
        assertTrue(boxDecorate.border is Border, "boxDecorate.border is Border")
        val border: Border = boxDecorate.border as Border
        assertTrue(
            border.left == BorderSide(
                width = 1f,
                style = BorderStyle.SOLID,
                color = 0xFF_FF_FF_00.toInt()
            ),
            "border.left 应等于声明的 BorderSide"
        )
        assertTrue(border.top == BorderSide.NONE, "border.top == BorderSide.NONE")
        assertTrue(border.right == BorderSide.NONE, "border.right == BorderSide.NONE")
        assertTrue(border.bottom == BorderSide.NONE, "border.bottom == BorderSide.NONE")
        val borderRadius = boxDecorate.borderRadius!!
        assertTrue(borderRadius.topLeft == Radius.ZERO, "borderRadius.topLeft == Radius.ZERO")
        assertTrue(borderRadius.topRight == Radius.ZERO, "borderRadius.topRight == Radius.ZERO")
        assertTrue(borderRadius.bottomLeft == Radius(2.1f, 3.4f), "borderRadius.bottomLeft == Radius(2.1f, 3.4f)")
        assertTrue(borderRadius.bottomRight == Radius.ZERO, "borderRadius.bottomRight == Radius.ZERO")
        assertTrue(boxDecorate.boxShadow == null, "boxDecorate.boxShadow == null")
    }
}
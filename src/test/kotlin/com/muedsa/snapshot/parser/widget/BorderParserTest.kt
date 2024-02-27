package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Radius
import com.muedsa.snapshot.paint.decoration.*
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.DecoratedBox
import org.jetbrains.skia.FilterBlurMode
import kotlin.test.Test

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
        println(text)
        val snapshotElement = ParserTest.parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is DecoratedBox)
        val decoratedBox: DecoratedBox = widget as DecoratedBox
        assert(decoratedBox.decoration is BoxDecoration)
        val boxDecorate: BoxDecoration = decoratedBox.decoration as BoxDecoration
        assert(boxDecorate.border is Border)
        val border: Border = boxDecorate.border as Border
        assert(
            border.left == BorderSide(
                width = 1f,
                style = BorderStyle.SOLID,
                color = 0xFF_FF_FF_00.toInt()
            )
        )
        val defaultBorderSide = BorderSide(
            width = 2f,
            style = BorderStyle.SOLID,
            color = 0xFF_00_FF_00.toInt()
        )
        assert(border.top == defaultBorderSide)
        assert(border.right == defaultBorderSide)
        assert(border.bottom == defaultBorderSide)
        val borderRadius = boxDecorate.borderRadius!!
        val defaultRadius = Radius(1.2f, 2.3f)
        assert(borderRadius.topLeft == defaultRadius)
        assert(borderRadius.topRight == defaultRadius)
        assert(borderRadius.bottomLeft == Radius(5f, 5f))
        assert(borderRadius.bottomRight == defaultRadius)
        assert(boxDecorate.boxShadow != null && boxDecorate.boxShadow!!.size == 1)
        assert(
            boxDecorate.boxShadow!![0] == BoxShadow(
                color = 0xFF_00_FF_00.toInt(),
                offset = Offset(5f, 7f),
                blurRadius = 3f,
                spreadRadius = 2f,
                blurStyle = FilterBlurMode.SOLID
            )
        )
    }

    @Test
    fun build_widget_without_default_test() {
        val text = """
            <Snapshot>
                <Border borderLeft="1 SOLID #FFFFFF00" borderRadiusBottomLeft="(2.1,3.4)"/>
            </Snapshot>
        """.trimIndent()
        println(text)
        val snapshotElement = ParserTest.parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is DecoratedBox)
        val decoratedBox: DecoratedBox = widget as DecoratedBox
        assert(decoratedBox.decoration is BoxDecoration)
        val boxDecorate: BoxDecoration = decoratedBox.decoration as BoxDecoration
        assert(boxDecorate.border is Border)
        val border: Border = boxDecorate.border as Border
        assert(
            border.left == BorderSide(
                width = 1f,
                style = BorderStyle.SOLID,
                color = 0xFF_FF_FF_00.toInt()
            )
        )
        assert(border.top == BorderSide.NONE)
        assert(border.right == BorderSide.NONE)
        assert(border.bottom == BorderSide.NONE)
        val borderRadius = boxDecorate.borderRadius!!
        assert(borderRadius.topLeft == Radius.ZERO)
        assert(borderRadius.topRight == Radius.ZERO)
        assert(borderRadius.bottomLeft == Radius(2.1f, 3.4f))
        assert(borderRadius.bottomRight == Radius.ZERO)
        assert(boxDecorate.boxShadow == null)
    }
}
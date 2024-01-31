package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.Test
import kotlin.test.expect

class AlignmentAttrDefineTest {

    @Test
    fun parse_special_test() {
        val attrName = "test"
        val attr = AlignmentAttrDefine(attrName)

        expect(BoxAlignment.TOP_LEFT) {
            attr.parseValue(RawAttr(attrName, "TOP_LEFT"))
        }
        expect(BoxAlignment.TOP_CENTER) {
            attr.parseValue(RawAttr(attrName, "TOP_CENTER"))
        }
        expect(BoxAlignment.TOP_RIGHT) {
            attr.parseValue(RawAttr(attrName, "TOP_RIGHT"))
        }
        expect(BoxAlignment.CENTER_LEFT) {
            attr.parseValue(RawAttr(attrName, "CENTER_LEFT"))
        }
        expect(BoxAlignment.CENTER) {
            attr.parseValue(RawAttr(attrName, "CENTER"))
        }
        expect(BoxAlignment.CENTER_RIGHT) {
            attr.parseValue(RawAttr(attrName, "CENTER_RIGHT"))
        }
        expect(BoxAlignment.BOTTOM_LEFT) {
            attr.parseValue(RawAttr(attrName, "BOTTOM_LEFT"))
        }
        expect(BoxAlignment.BOTTOM_CENTER) {
            attr.parseValue(RawAttr(attrName, "BOTTOM_CENTER"))
        }
        expect(BoxAlignment.BOTTOM_RIGHT) {
            attr.parseValue(RawAttr(attrName, "BOTTOM_RIGHT"))
        }
    }

    @Test
    fun parse_number_test() {
        val attrName = "test"
        val attr = AlignmentAttrDefine(attrName)
        expect(BoxAlignment.TOP_LEFT) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.TOP_LEFT)))
        }
        expect(BoxAlignment.TOP_CENTER) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.TOP_CENTER)))
        }
        expect(BoxAlignment.TOP_RIGHT) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.TOP_RIGHT)))
        }
        expect(BoxAlignment.CENTER_LEFT) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.CENTER_LEFT)))
        }
        expect(BoxAlignment.CENTER) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.CENTER)))
        }
        expect(BoxAlignment.CENTER_RIGHT) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.CENTER_RIGHT)))
        }
        expect(BoxAlignment.BOTTOM_LEFT) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.BOTTOM_LEFT)))
        }
        expect(BoxAlignment.BOTTOM_CENTER) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.BOTTOM_CENTER)))
        }
        expect(BoxAlignment.BOTTOM_RIGHT) {
            attr.parseValue(RawAttr(attrName, buildValueString(BoxAlignment.BOTTOM_RIGHT)))
        }

        expect(BoxAlignment(0.5f, 0.3f)) {
            attr.parseValue(RawAttr(attrName, "(0.5,0.3)"))
        }
        expect(BoxAlignment(-0.5f, 0.3f)) {
            attr.parseValue(RawAttr(attrName, "(-0.5,0.3)"))
        }
        expect(BoxAlignment(0.5f, 0.3f)) {
            attr.parseValue(RawAttr(attrName, "(0.5,+0.3)"))
        }
        expect(BoxAlignment(-0.5f, -0.3f)) {
            attr.parseValue(RawAttr(attrName, "(-0.5,-0.3)"))
        }

        expect(BoxAlignment(-0.5f, 0f)) {
            attr.parseValue(RawAttr(attrName, "(-0.5,-0)"))
        }

        expect(BoxAlignment(2f, 0f)) {
            attr.parseValue(RawAttr(attrName, "(2,0)"))
        }

        expect(BoxAlignment(2f, -3f)) {
            attr.parseValue(RawAttr(attrName, "(2,-3)"))
        }
    }

    private fun buildValueString(boxAlignment: BoxAlignment): String = "(${boxAlignment.x},${boxAlignment.y})"
}
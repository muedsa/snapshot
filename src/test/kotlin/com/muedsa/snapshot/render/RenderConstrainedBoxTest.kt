package com.muedsa.snapshot.render

import com.muedsa.snapshot.rendering.BoxConstraints
import com.muedsa.snapshot.noLimitedLayout
import com.muedsa.snapshot.rendering.RenderConstrainedBox
import kotlin.test.expect

class RenderConstrainedBoxTest {

    fun layoutTest() {
        val child = RenderConstrainedBox(additionalConstraints = BoxConstraints.tightFor(height = 300f, width = 400f))
        val parent = RenderConstrainedBox(
            additionalConstraints = BoxConstraints.tightFor(height = 300f, width = 400f),
            child = child
        )
        noLimitedLayout(parent)

        expect(100f) { child.definiteSize.height }
        expect(200f) {
            child.definiteSize.width
        }

    }
}
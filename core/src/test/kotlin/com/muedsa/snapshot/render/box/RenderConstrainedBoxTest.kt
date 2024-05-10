package com.muedsa.snapshot.render.box

import com.muedsa.snapshot.noLimitedLayout
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import kotlin.test.Test
import kotlin.test.expect

class RenderConstrainedBoxTest {

    @Test
    fun layoutTest() {
        val child = RenderConstrainedBox(additionalConstraints = BoxConstraints.tightFor(height = 300f, width = 400f))
        val parent = RenderConstrainedBox(
            additionalConstraints = BoxConstraints.tightFor(height = 100f, width = 200f),
        ).apply {
            appendChild(child)
        }
        noLimitedLayout(parent)

        expect(100f) { child.definiteSize.height }
        expect(200f) { child.definiteSize.width }

    }
}
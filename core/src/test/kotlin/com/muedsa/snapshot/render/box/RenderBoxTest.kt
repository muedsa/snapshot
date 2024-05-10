package com.muedsa.snapshot.render.box

import com.muedsa.snapshot.noLimitedLayout
import com.muedsa.snapshot.rendering.box.RenderBox

class RenderBoxTest {

    class FakeMissingSizeRenderBox : RenderBox() {
        override fun performLayout() {
            size = definiteConstraints.biggest
        }

    }


    class MissSetSizeRenderBox : RenderBox() {
        override fun performLayout() {

        }
    }

    fun testMissSetSizeRenderBox() {
        val box = MissSetSizeRenderBox()
        noLimitedLayout(box)
        //box.paint()
    }
}
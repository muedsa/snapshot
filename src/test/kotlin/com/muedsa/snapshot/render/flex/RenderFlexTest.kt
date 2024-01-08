package com.muedsa.snapshot.render.flex

import com.muedsa.snapshot.noLimitedLayout
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.flex.FlexParentData
import com.muedsa.snapshot.rendering.flex.RenderFlex
import kotlin.test.Test

class RenderFlexTest {

    @Test
    fun setupParentDataTest() {
        val child1 = RenderConstrainedBox(
            BoxConstraints.expand(width = 100f, 100f)
        )
        val child2 = RenderConstrainedBox(
            BoxConstraints.expand(width = 120f, 80f)
        )
        val child3 = RenderConstrainedBox(
            BoxConstraints.expand(width = 80f, 120f)
        )
        val renderFlex = RenderFlex(
            children = arrayOf(child1, child2, child3)
        )
        renderFlex.children!!.forEach { child ->
            assert(child.parentData is FlexParentData)
        }
    }

    @Test
    fun layoutTest() {
        val child1 = RenderConstrainedBox(
            BoxConstraints.expand(width = 100f, 100f)
        )
        val child2 = RenderConstrainedBox(
            BoxConstraints.expand(width = 120f, 80f)
        )
        val child3 = RenderConstrainedBox(
            BoxConstraints.expand(width = 80f, 120f)
        )
        val renderFlex = RenderFlex(
            children = arrayOf(child1, child2, child3)
        )
        noLimitedLayout(renderFlex)
        println("#renderFlex: ${renderFlex.definiteSize}")
        renderFlex.children?.forEachIndexed{
            index: Int, child: RenderBox ->
            println("##child: $index, ${child.definiteConstraints}, ${child.definiteSize}, ${child.parentData!!.offset}")
        }
    }

    @Test
    fun paintTest() {
        val child1 = RenderConstrainedBox(
            BoxConstraints.expand(width = 100f, 100f)
        )
        val child2 = RenderConstrainedBox(
            BoxConstraints.expand(width = 120f, 80f)
        )
        val child3 = RenderConstrainedBox(
            BoxConstraints.expand(width = 80f, 120f)
        )
        val renderFlex = RenderFlex(
            children = arrayOf(child1, child2, child3)
        )
        noLimitedLayout(renderFlex)
        println("#renderFlex: ${renderFlex.definiteSize}")
        renderFlex.children?.forEachIndexed{
                index: Int, child: RenderBox ->
            println("##child: $index, ${child.definiteConstraints}, ${child.definiteSize}, ${child.parentData!!.offset}")
        }
    }
}
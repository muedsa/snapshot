package com.muedsa.snapshot.render.box

import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame

class RenderBoxParentTest {

    private fun box() = RenderConstrainedBox(additionalConstraints = BoxConstraints())

    /**
     * 回归:把子节点挂到一个"自己已经有父节点"的 RenderBox 上必须能返回。
     * 修复前 setter 里的 `temp = value?.parent` 使 temp 不再前进,此处会死循环。
     * 用带超时的工作线程来跑,避免测试 JVM 直接挂死。
     */
    @Test
    fun attaching_to_an_already_parented_box_terminates() {
        val grandParent = box()
        val parent = box()
        val child = box()
        grandParent.appendChild(parent)   // parent.parent == grandParent

        val worker = Thread { parent.appendChild(child) }
        worker.isDaemon = true
        worker.start()
        worker.join(5_000)

        assertFalse(worker.isAlive, "attaching to an already parented box did not terminate")
        assertSame(parent, child.parent)
    }
}
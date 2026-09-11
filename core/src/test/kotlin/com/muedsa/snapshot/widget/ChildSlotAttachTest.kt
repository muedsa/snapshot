package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ChildSlotAttachTest {

    @Test
    fun single_child_widget_attaches_child() {
        val parent = Padding(padding = EdgeInsets.all(1f))
        val child = SizedBox(width = 1f, height = 1f)

        parent.attach(child)

        assertSame(child, parent.child)
    }

    @Test
    fun single_child_widget_rejects_second_attach() {
        val parent = Padding(padding = EdgeInsets.all(1f))
        val first = SizedBox(width = 1f, height = 1f)
        val second = SizedBox(width = 2f, height = 2f)

        parent.attach(first)
        val error = assertFailsWith<IllegalStateException> { parent.attach(second) }

        assertTrue(
            error.message!!.contains("Padding"),
            "message should contain parent class name, but was: ${error.message}"
        )
        assertTrue(
            error.message!!.contains("SizedBox"),
            "message should contain child class name, but was: ${error.message}"
        )
        assertSame(first, parent.child, "the first child must not be silently replaced")
    }

    @Test
    fun proxy_widget_attaches_widget() {
        val parent = ProxyWidget()
        val child = SizedBox(width = 1f, height = 1f)

        parent.attach(child)

        assertSame(child, parent.widget)
    }

    @Test
    fun proxy_widget_rejects_second_attach() {
        val parent = ProxyWidget()
        val first = SizedBox(width = 1f, height = 1f)
        val second = SizedBox(width = 2f, height = 2f)

        parent.attach(first)
        val error = assertFailsWith<IllegalStateException> { parent.attach(second) }

        assertTrue(
            error.message!!.contains("ProxyWidget"),
            "message should contain parent class name, but was: ${error.message}"
        )
        assertSame(first, parent.widget, "the first widget must not be silently replaced")
    }

    @Test
    fun multi_child_widget_appends_two_distinct_children() {
        val parent = Row()
        val first = SizedBox(width = 1f, height = 1f)
        val second = SizedBox(width = 2f, height = 2f)

        parent.attach(first)
        parent.attach(second)

        assertEquals(listOf(first, second), parent.children)
    }

    @Test
    fun multi_child_widget_rejects_duplicate_attach() {
        val parent = Row()
        val child = SizedBox(width = 1f, height = 1f)

        parent.attach(child)

        assertFailsWith<IllegalStateException> { parent.attach(child) }
        assertEquals(listOf(child), parent.children)
    }

    @Test
    fun attach_allows_re_attach_to_another_slot() {
        // Container.composeWidget() 依赖此行为:把已挂载的子节点重新挂到新建的包装节点上。
        val first = Padding(padding = EdgeInsets.all(1f))
        val second = Padding(padding = EdgeInsets.all(2f))
        val child = SizedBox(width = 1f, height = 1f)

        first.attach(child)
        second.attach(child)

        assertSame(child, second.child)
    }
}

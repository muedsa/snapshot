package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

open class ProxyWidget(parent: Widget? = null) : Widget(parent = parent), ChildSlot {

    var widget: Widget? = null
        protected set

    override fun attach(child: Widget) {
        check(widget == null) {
            "${this::class.simpleName} already has a widget, can not attach ${child::class.simpleName}"
        }
        this.widget = child
        child.parent = this
    }

    final override fun createRenderBox(): RenderBox {
        val widget = this.widget
        assert(widget != null) { "proxy null widget ??" }
        return widget!!.createRenderBox()
    }

    companion object {
        fun buildWidget(
            content: Widget.() -> Unit
        ): Widget {
            val widget = ProxyWidget().apply(content).widget
            checkNotNull(widget)
            return widget
        }
    }
}

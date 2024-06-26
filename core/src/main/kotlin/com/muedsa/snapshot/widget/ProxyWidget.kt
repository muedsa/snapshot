package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

open class ProxyWidget(parent: Widget? = null) : Widget(parent = parent) {

    var widget: Widget? = null

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
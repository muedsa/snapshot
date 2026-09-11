package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

open class ProxyWidget : Widget(), ChildSlot {

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
        checkNotNull(widget) { "ProxyWidget has no widget, can not create render box" }
        return widget.createRenderBox()
    }

    companion object {

        /** 用一段 DSL 构造一棵游离的 Widget 树,返回其根节点。 */
        @PublishedApi
        internal inline fun buildWidget(content: ChildSlot.() -> Unit): Widget {
            val proxy = ProxyWidget()
            proxy.content()
            val widget = proxy.widget
            checkNotNull(widget) { "buildWidget produced an empty widget tree" }
            return widget
        }
    }
}

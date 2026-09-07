package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox
import com.muedsa.snapshot.rendering.box.RenderSingleChildBox
import org.jetbrains.skia.Rect

/**
 * 只读布局自省:给出 layout 后每个节点的全局几何。
 * 无行为变更、不参与渲染;供测试/调试读取。
 * children 在构建期间由内部可变列表填充;构建完成后对外只读(以 List 视图暴露)。
 *
 * offset/结构(父子关系、offsetFromParent)在调用 [RenderBox.toLayoutNode] 时快照固化;
 * 而 [size]/[rect] 读取的是实时的 [definiteSize]。用途是 post-layout 冻结树,重建前不要改动渲染树。
 */
class LayoutNode internal constructor(
    val renderBox: RenderBox,
    val offsetFromParent: Offset,
    parent: LayoutNode?,
    childrenBacking: MutableList<LayoutNode> = mutableListOf(),
) {
    val parent: LayoutNode? = parent
    val children: List<LayoutNode> = childrenBacking
    val size: Size get() = renderBox.definiteSize
    val absoluteOffset: Offset = (parent?.absoluteOffset ?: Offset.ZERO) + offsetFromParent
    val rect: Rect get() = absoluteOffset combine size
}

// 当前类层次中,多子的 RenderBox 仅 RenderContainerBox、单子节点均经 RenderSingleChildBox;
// 故此 when 是一份封闭白名单。未来若引入其它自带子节点的 RenderBox,需在此同步扩展。
private fun childBoxes(box: RenderBox): List<RenderBox> = when (box) {
    is RenderContainerBox -> box.children
    is RenderSingleChildBox -> listOfNotNull(box.child)
    else -> emptyList()
}

/**
 * 将已 layout 的渲染树/子树构建为只读 [LayoutNode] 树。
 *
 * 前置条件:接收者应为**已 layout** 的渲染树/子树(通常取 `layoutWidget(...)` 的返回值或其根)。
 *
 * children 规则:RenderContainerBox → 逐个递归其 children;RenderSingleChildBox → child 非空时递归;
 * 其它 RenderBox → 无子节点。
 *
 * 几何语义:每个节点的偏移取自其 `parentData.offset`,与渲染路径同源;此自省**忽略**
 * transform/clip 等画布矩阵,只反映布局偏移。
 *
 * 坐标系约定:以"渲染根"为接收者时,根节点 `absoluteOffset == Offset.ZERO`;若对树外
 * 的子盒调用,原点会落在其缺席的父级处——通常不应这样用。
 */
fun RenderBox.toLayoutNode(): LayoutNode {
    fun build(box: RenderBox, parent: LayoutNode?): LayoutNode {
        val offset = box.parentData?.offset ?: Offset.ZERO
        val backing = mutableListOf<LayoutNode>()
        val node = LayoutNode(box, offset, parent, backing)
        childBoxes(box).forEach { backing += build(it, node) }
        return node
    }
    return build(this, null)
}

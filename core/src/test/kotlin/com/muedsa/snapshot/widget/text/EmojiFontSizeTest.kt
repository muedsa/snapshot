package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.testFontFamily
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals

class EmojiFontSizeTest {

    private val image: Image = Surface.makeRasterN32Premul(1, 1).makeImageSnapshot()

    // 省略 width/height 时 emoji 尺寸完全由「继承字号」决定:
    // sizeForConstraints 走 BoxConstraints.tightFor(fontSize, fontSize),
    // 1x1 图片在紧约束下做 preserveAspectRatio 仍是 (40, 40)。
    // 注入失效时 findFontSize() 返回 null、约束完全放开,尺寸退化为图片自身大小 1x1,
    // 与 40 的差异远大于容差 1f。
    @Test
    fun emoji_inherits_font_size_from_rich_text() {
        val root = rootLayout {
            RichText {
                TextSpan(style = TextStyle(fontSize = 40f, fontFamilies = listOf(testFontFamily))) {
                    ImageEmojiSpan(provider = { image })
                }
            }
        }

        val emoji = root.findType<RenderImageEmoji>()
        checkNotNull(emoji) { "找不到 RenderImageEmoji 节点" }
        emoji.assertSize(40f, 40f, tolerance = 1f)
    }

    // 没有 TextParentData(即不在 RichText 里)时 findFontSize() 返回 null,
    // 尺寸退化为按图片本身大小(1x1)决定,不得抛异常。
    // WidgetSpan 只有被 RichText 抽取才会进入 widget 树,所以这条路径只能在 render 层直接构造。
    @Test
    fun emoji_without_text_parent_data_does_not_crash() {
        val box = RenderImageEmoji(image = image)
        box.layout(BoxConstraints())

        assertEquals(1f, box.definiteSize.width, 0.01f)
        assertEquals(1f, box.definiteSize.height, 0.01f)
    }
}

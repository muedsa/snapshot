package com.muedsa.snapshot.parser.widget

class WidgetParserManager {

    val tags: MutableMap<String, WidgetParser> = mutableMapOf()

    fun register(tag: WidgetParser) {
        tags[tag.id] = tag
    }

    fun remove(tag: WidgetParser) {
        tags.remove(tag.id)
    }

    fun remove(tagId: String) {
        tags.remove(tagId)
    }

    operator fun get(tagId: String): WidgetParser? = tags[tagId]

    companion object {

        val DEFAULT_MANAGER: WidgetParserManager = WidgetParserManager().also {
            it.register(SnapshotParser)
            it.register(ContainerParser())
            it.register(BorderParser())
            it.register(ColoredBoxParser())
            it.register(DecoratedBoxParser())
            it.register(RowParser())
            it.register(ColumnParser())
            it.register(ExpandedParser())
            it.register(FlexibleParser())
            it.register(StackParser())
            it.register(PositionedParser())
            it.register(SizedBoxParser())
            it.register(ConstrainedBoxParser())
            it.register(LimitedBoxParser())
            it.register(OverflowBoxParser())
            it.register(SizedOverflowBoxParser())
            it.register(PaddingParser())
            it.register(AlignParser())
            it.register(CenterParser())
            it.register(OpacityParser())
            it.register(TransformParser())
            it.register(ClipRectParser())
            it.register(ClipOvalParser())
            it.register(ClipRRectParser())
            it.register(ColorFilteredParser())
            it.register(ImageFilteredParser())
            it.register(BackdropFilterParser())
            it.register(ImageParser())
            it.register(TextParser())
            it.register(RawTextParser())
            it.register(EmojiParser())
        }
    }
}

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

    /**
     * 创建当前注册表的独立副本。
     *
     * 复制后的注册表可以安全地增删或替换标签，不会影响原注册表。
     */
    fun copy(): WidgetParserManager = WidgetParserManager().also { copy ->
        tags.values.forEach(copy::register)
    }

    companion object {

        private val DEFAULT_PARSER_FACTORIES: List<() -> WidgetParser> = listOf(
            { SnapshotParser },
            { ContainerParser() },
            { BorderParser() },
            { ColoredBoxParser() },
            { DecoratedBoxParser() },
            { FlexParser() },
            { RowParser() },
            { ColumnParser() },
            { ExpandedParser() },
            { FlexibleParser() },
            { SpacerParser() },
            { StackParser() },
            { IndexedStackParser() },
            { PositionedParser() },
            { SizedBoxParser() },
            { AspectRatioParser() },
            { FractionallySizedBoxParser() },
            { UnconstrainedBoxParser() },
            { ConstrainedBoxParser() },
            { LimitedBoxParser() },
            { OverflowBoxParser() },
            { SizedOverflowBoxParser() },
            { PaddingParser() },
            { AlignParser() },
            { CenterParser() },
            { OpacityParser() },
            { TransformParser() },
            { ClipRectParser() },
            { ClipOvalParser() },
            { ClipRRectParser() },
            { ColorFilteredParser() },
            { ImageFilteredParser() },
            { BackdropFilterParser() },
            { ImageParser() },
            { TextParser() },
            { RawTextParser() },
            { EmojiParser() },
            { WidgetSpanParser() },
        )

        /**
         * 创建包含所有内置标签的新注册表。
         *
         * 每次调用都会创建独立的注册表和解析器实例，调用方的修改不会影响其他 Parser。
         */
        @JvmStatic
        fun withDefaults(): WidgetParserManager = WidgetParserManager().also { manager ->
            DEFAULT_PARSER_FACTORIES.forEach { factory -> manager.register(factory()) }
        }

        @Deprecated(
            message = "共享的默认注册表容易被意外修改，请改用 WidgetParserManager.withDefaults()",
            replaceWith = ReplaceWith("WidgetParserManager.withDefaults()"),
        )
        val DEFAULT_MANAGER: WidgetParserManager = withDefaults()
    }
}

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
            it.register(RowParser())
            it.register(ColumnParser())
            it.register(StackParser())
            it.register(PositionedParser())
            it.register(ImageParser())
            it.register(TextParser())
        }
    }
}
package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.Parser
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class WidgetParserManagerTest {

    @Test
    fun with_defaults_returns_independent_registries_and_parser_instances() {
        val first = WidgetParserManager.withDefaults()
        val second = WidgetParserManager.withDefaults()

        assertEquals(first.tags.keys, second.tags.keys)
        assertNotSame(first, second)
        assertNotSame(first[ContainerParser().id], second[ContainerParser().id])

        first.remove(ContainerParser().id)

        assertNull(first[ContainerParser().id])
        assertNotNull(second[ContainerParser().id])
    }

    @Test
    fun copy_can_be_modified_without_affecting_source_registry() {
        val source = WidgetParserManager.withDefaults()
        val copy = source.copy()

        copy.remove(TextParser().id)
        copy.register(CustomSizedBoxParser())

        assertNotNull(source[TextParser().id])
        assertNull(copy[TextParser().id])
        assertNull(source[CustomSizedBoxParser.ID])
        assertNotNull(copy[CustomSizedBoxParser.ID])
    }

    @Test
    fun default_parser_instances_do_not_share_registry_mutations() {
        val modifiedParser = ConfigurableParser()
        val independentParser = ConfigurableParser()
        modifiedParser.removeTag(SizedBoxParser().id)
        val document = "<Snapshot><SizedBox width=\"1\" height=\"1\"/></Snapshot>"

        assertFailsWith<ParseException> {
            modifiedParser.parse(StringReader(document))
        }
        independentParser.parse(StringReader(document))
    }

    private class ConfigurableParser : Parser() {
        fun removeTag(tagId: String) {
            widgetParserManager.remove(tagId)
        }
    }

    private class CustomSizedBoxParser : SizedBoxParser() {
        override val id: String = ID

        companion object {
            const val ID = "CustomSizedBox"
        }
    }
}

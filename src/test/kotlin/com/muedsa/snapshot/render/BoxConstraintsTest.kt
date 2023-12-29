package com.muedsa.snapshot.render

import com.muedsa.snapshot.rendering.BoxConstraints
import org.junit.jupiter.api.Test
import kotlin.test.expect

class BoxConstraintsTest {

    @Test
    fun copyWithTest() {
        val source = BoxConstraints(
            minWidth = 5f,
            maxWidth = 13f,
            minHeight = 7f,
            maxHeight = 19f
        )

        expect(source) {
            source.copyWith()
        }

        expect(
            BoxConstraints(
                minWidth = 3f,
                maxWidth = 13f,
                minHeight = 7f,
                maxHeight = 19f
            )
        ) {
            source.copyWith(minWidth = 3f)
        }

        expect(
            expected = BoxConstraints(
                minWidth = 5f,
                maxWidth = 11f,
                minHeight = 7f,
                maxHeight = 19f
            )
        ) {
            source.copyWith(maxWidth = 11f)
        }

        expect(
            expected = BoxConstraints(
                minWidth = 5f,
                maxWidth = 13f,
                minHeight = 3f,
                maxHeight = 19f
            )
        ) {
            source.copyWith(minHeight = 3f)
        }

        expect(
            expected = BoxConstraints(
                minWidth = 5f,
                maxWidth = 13f,
                minHeight = 7f,
                maxHeight = 20f
            )
        ) {
            source.copyWith(maxHeight = 20f)
        }
    }
}
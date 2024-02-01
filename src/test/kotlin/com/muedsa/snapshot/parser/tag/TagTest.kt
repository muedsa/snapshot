package com.muedsa.snapshot.parser.tag

import com.muedsa.snapshot.parser.Tag
import kotlin.test.Test

class TagTest {

    @Test
    fun tags_test() {
        Tag.entries.forEach {
            println(it)
        }
    }
}
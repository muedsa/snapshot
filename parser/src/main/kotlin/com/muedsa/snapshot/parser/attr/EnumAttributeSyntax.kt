package com.muedsa.snapshot.parser.attr

import java.util.Locale

/** 枚举的文本别名；不作用于正文、字体名、URL 或 OpenType 特性标签。 */
internal object EnumAttributeSyntax {
    fun normalize(value: String): String = value.trim().replace('-', '_').uppercase(Locale.ROOT)

    fun canonical(value: String, choices: List<String>): String {
        val normalized = normalize(value)
        return choices.firstOrNull { normalize(it) == normalized } ?: value
    }
}

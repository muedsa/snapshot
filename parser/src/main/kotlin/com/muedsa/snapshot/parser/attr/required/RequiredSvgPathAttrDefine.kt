package com.muedsa.snapshot.parser.attr.required

import org.jetbrains.skia.Path

/** SVG 路径数据（d 属性），不解析完整 SVG 文档。 */
class RequiredSvgPathAttrDefine(name: String) : AttrDefine<Path>(name) {

    override fun parseValue(valueStr: String?): Path {
        require(!valueStr.isNullOrBlank()) { "Attr [$name] must contain SVG path data" }
        require('\u0000' !in valueStr) { "Attr [$name] must not contain NUL" }
        return Path.makeFromSVGString(valueStr).also {
            require(it.isFinite) { "Attr [$name] path coordinates must be finite" }
        }
    }

    override fun copyWith(name: String): RequiredSvgPathAttrDefine = RequiredSvgPathAttrDefine(name)
}

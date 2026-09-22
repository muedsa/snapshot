package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.token.RawAttr

/**
 * 描述“使用任一依赖属性时必须同时提供主属性”的组合约束。
 *
 * 该约束只负责属性之间的依赖关系；具体属性值仍由各自的 [AttrDefine] 解析和校验。
 */
internal class RequiredAttributeContract(
    required: AttrDefine<*>,
    vararg dependents: AttrDefine<*>,
) {
    private val requiredName = required.name
    private val dependentNames = dependents.map(AttrDefine<*>::name)

    fun validate(
        requiredPresent: Boolean,
        attrs: Map<String, RawAttr>,
        transformName: (String) -> String = { it },
    ) {
        if (requiredPresent) return

        val unexpectedName = dependentNames
            .asSequence()
            .map(transformName)
            .firstOrNull(attrs::containsKey)
        require(unexpectedName == null) {
            "Attr [${transformName(requiredName)}] is required when [$unexpectedName] is specified"
        }
    }
}

package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.attr.nullable.NullableStringAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

class RequiredAttributeContractTest {

    @Test
    fun accepts_absent_dependents_or_present_required_attribute() {
        val contract = RequiredAttributeContract(REQUIRED, FIRST_DEPENDENT, SECOND_DEPENDENT)

        contract.validate(requiredPresent = false, attrs = emptyMap())
        contract.validate(
            requiredPresent = true,
            attrs = mapOf(FIRST_DEPENDENT.name to rawAttr(FIRST_DEPENDENT.name)),
        )
    }

    @Test
    fun reports_first_declared_dependent_instead_of_map_iteration_order() {
        val contract = RequiredAttributeContract(REQUIRED, FIRST_DEPENDENT, SECOND_DEPENDENT)
        val attrs = linkedMapOf(
            SECOND_DEPENDENT.name to rawAttr(SECOND_DEPENDENT.name),
            FIRST_DEPENDENT.name to rawAttr(FIRST_DEPENDENT.name),
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            contract.validate(requiredPresent = false, attrs = attrs)
        }

        assertContains(
            exception.message.orEmpty(),
            "Attr [required] is required when [firstDependent] is specified",
        )
    }

    @Test
    fun applies_name_transform_to_required_and_dependent_attributes() {
        val contract = RequiredAttributeContract(REQUIRED, FIRST_DEPENDENT)
        val attrs = mapOf("prefixFirstDependent" to rawAttr("prefixFirstDependent"))

        val exception = assertFailsWith<IllegalArgumentException> {
            contract.validate(requiredPresent = false, attrs = attrs) { name ->
                "prefix${name.replaceFirstChar(Char::uppercaseChar)}"
            }
        }

        assertContains(
            exception.message.orEmpty(),
            "Attr [prefixRequired] is required when [prefixFirstDependent] is specified",
        )
    }

    private fun rawAttr(name: String): RawAttr = RawAttr(name)

    companion object {
        private val REQUIRED = NullableStringAttrDefine("required")
        private val FIRST_DEPENDENT = NullableStringAttrDefine("firstDependent")
        private val SECOND_DEPENDENT = NullableStringAttrDefine("secondDependent")
    }
}

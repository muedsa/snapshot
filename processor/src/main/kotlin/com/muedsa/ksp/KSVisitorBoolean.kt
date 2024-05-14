package com.muedsa.ksp

import com.google.devtools.ksp.symbol.*

open class KSVisitorBoolean(
    private val default: Boolean = false
) : KSVisitor<Unit, Boolean> {
    override fun visitAnnotated(annotated: KSAnnotated, data: Unit): Boolean {
        return default
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit): Boolean {
        return default
    }

    override fun visitCallableReference(reference: KSCallableReference, data: Unit): Boolean {
        return default
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Boolean {
        return default
    }

    override fun visitClassifierReference(reference: KSClassifierReference, data: Unit): Boolean {
        return default
    }

    override fun visitDeclaration(declaration: KSDeclaration, data: Unit): Boolean {
        return default
    }

    override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Unit): Boolean {
        return default
    }

    override fun visitDefNonNullReference(reference: KSDefNonNullReference, data: Unit): Boolean {
        return default
    }

    override fun visitDynamicReference(reference: KSDynamicReference, data: Unit): Boolean {
        return default
    }

    override fun visitFile(file: KSFile, data: Unit): Boolean {
        return default
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit): Boolean {
        return default
    }

    override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: Unit): Boolean {
        return default
    }

    override fun visitNode(node: KSNode, data: Unit): Boolean {
        return default
    }

    override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: Unit): Boolean {
        return default
    }

    override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: Unit): Boolean {
        return default
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit): Boolean {
        return default
    }

    override fun visitPropertyGetter(getter: KSPropertyGetter, data: Unit): Boolean {
        return default
    }

    override fun visitPropertySetter(setter: KSPropertySetter, data: Unit): Boolean {
        return default
    }

    override fun visitReferenceElement(element: KSReferenceElement, data: Unit): Boolean {
        return default
    }

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit): Boolean {
        return default
    }

    override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit): Boolean {
        return default
    }

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit): Boolean {
        return default
    }

    override fun visitTypeReference(typeReference: KSTypeReference, data: Unit): Boolean {
        return default
    }

    override fun visitValueArgument(valueArgument: KSValueArgument, data: Unit): Boolean {
        return default
    }

    override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit): Boolean {
        return default
    }
}
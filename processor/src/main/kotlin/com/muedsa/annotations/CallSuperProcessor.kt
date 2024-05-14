package com.muedsa.annotations

import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.muedsa.ksp.KSVisitorBoolean

class CallSuperProcessor(
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functions = resolver.getSymbolsWithAnnotation("com.muedsa.annotations.CallSuper")
            .filter { it is KSFunctionDeclaration }
            .map { it as KSFunctionDeclaration }
            .filter { it.accept(CallSuperFunctionChecker(logger), Unit) }
        functions.forEach {
            logger.warn("final function @CallSuper", it)
        }
//        resolver.getAllFiles().map { file ->
//            file.declarations
//                .filter { it is KSClassDeclaration }
//                .map { it as KSClassDeclaration }
//        }.flatMap { it }
//            .map { it.getAllFunctions() }
//            .flatMap { it }
//            .filter { !it.isAbstract }
//            .filter { it.findOverridee() != null }
//            .map { it.findOverridee()!! }
//            .filter { functions.contains(it) }
//            .forEach {
//                logger.warn("Function should call super", it)
//            }
        return emptyList()
    }

    class CallSuperFunctionChecker(private val logger: KSPLogger) : KSVisitorBoolean() {
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit): Boolean {
            var flag = true
            if (!function.isOpen()) {
                logger.warn("@CallSuper fun is not open", function)
                flag = false
            }
// 存在这种情况吗？
//            else if(function.parentDeclaration !is KSClassDeclaration) {
//                logger.warn("@CallSuper fun not in Class", function)
//                flag = false
//            } else if(!(function.parentDeclaration as KSClassDeclaration).isOpen()) {
//                logger.warn("Class is not open, but member fun @CallSuper", function.parentDeclaration)
//                flag = false
//            }
            return flag
        }
    }
}

class CallSuperProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return CallSuperProcessor(environment.logger)
    }
}
package com.muedsa.annotations

class CallSuperTest {

    open class A {
        @CallSuper
        open fun fn() {
            println("A")
        }
    }

    open class B : A() {
        override fun fn() {
            super.fn()
            println("B")
        }
    }

    class C : B() {
        override fun fn() {
            super.fn()
            println("C")
        }
    }
}
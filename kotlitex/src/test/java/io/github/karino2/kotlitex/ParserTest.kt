package io.github.karino2.kotlitex

import org.junit.Test

import org.junit.Assert.*

class NodeMathOrdAsserter(val actual : PNodeMathOrd) {
    fun text(expect: String) = assertEquals(expect, actual.text)
    fun start(expect: Int) = assertEquals(expect, actual.loc?.start)
    fun end(expect: Int) = assertEquals(expect, actual.loc?.end)
    fun mode(expect: Mode) = assertEquals(expect, actual.mode)
}

fun assertMathOrd(node: ParseNode?, body: NodeMathOrdAsserter.() -> Unit) {
    assertTrue(node is PNodeMathOrd)
    // always succeed
    if(node is PNodeMathOrd) {
        NodeMathOrdAsserter(node).body()
    }
}
class NodeTextOrdAsserter(val actual : PNodeTextOrd) {
    fun text(expect: String) = assertEquals(expect, actual.text)
    fun start(expect: Int) = assertEquals(expect, actual.loc?.start)
    fun end(expect: Int) = assertEquals(expect, actual.loc?.end)
    fun mode(expect: Mode) = assertEquals(expect, actual.mode)
}
fun assertTextOrd(node: ParseNode?, body: NodeTextOrdAsserter.() -> Unit) {
    assertTrue(node is PNodeTextOrd)
    // always succeed
    if(node is PNodeTextOrd) {
        NodeTextOrdAsserter(node).body()
    }
}


class ParserTest {
    @Test
    fun parseX() {
        val parser = Parser("x")

        val actual = parser.parse()

        assertEquals(1, actual.size)
        assertMathOrd(actual[0]) {
            text("x")
            start(0)
            end(1)
            mode(Mode.MATH)
        }
    }

    @Test
    fun parseY() {
        val parser = Parser("y")

        val actual = parser.parse()
        assertMathOrd(actual[0]) {
            text("y")
        }
    }

    @Test
    fun parseSup() {
        val parser = Parser("x^2")

        val actual = parser.parse()
        assertEquals(1, actual.size)
        val supsub = actual[0]
        when (supsub) {
            is PNodeSupSub -> {
                assertMathOrd(supsub.base) {
                    text("x")
                    start(0)
                    end(1)
                }
                assertTextOrd(supsub.sup) {
                    text("2")
                    start(2)
                    end(3)
                }
                assertNull(supsub.sub)
            }
            else -> assertTrue(false)
        }
        @Test
        fun parsePlus() {
            val parser = Parser("x+y")

            val actual = parser.parse()
            assertTrue(actual.isNotEmpty())
        }
    }
}

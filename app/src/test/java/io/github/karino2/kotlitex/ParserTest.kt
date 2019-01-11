package io.github.karino2.kotlitex

import org.junit.Test

import org.junit.Assert.*

class NodeAsserter(val actual : NodeMathOrd) {
    fun text(expect: String) = assertEquals(expect, actual.text)
    fun start(expect: Int) = assertEquals(expect, actual.loc?.start)
    fun end(expect: Int) = assertEquals(expect, actual.loc?.end)
    fun mode(expect: Mode) = assertEquals(expect, actual.mode)
}

fun assertMathOrd(node: ParseNode, body: NodeAsserter.() -> Unit) {
    assertTrue(node is NodeMathOrd)
    // always succeed
    if(node is NodeMathOrd) {
        NodeAsserter(node).body()
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
}
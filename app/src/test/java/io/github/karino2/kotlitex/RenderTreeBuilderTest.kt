package io.github.karino2.kotlitex

import org.junit.Test

import org.junit.Assert.*


class RenderTreeBuilderTest {
    fun parse(input: String) : List<ParseNode> {
        val parser = Parser(input)
        return  parser.parse()
    }
    @Test
    fun basic() {
        val input = parse("x^2")
        val builder = RenderTreeBuilder()
        val actual = builder.buildGroup(input[0], Options(Style.TEXT))

        assertTrue(actual is SpanNode)
    }
}
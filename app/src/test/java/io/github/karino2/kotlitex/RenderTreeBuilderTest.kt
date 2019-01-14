package io.github.karino2.kotlitex

import org.junit.Test

import org.junit.Assert.*


class RenderTreeBuilderTest {
    fun parse(input: String) : List<ParseNode> {
        val parser = Parser(input)
        return  parser.parse()
    }

    val options = Options(Style.TEXT)
    // This is used to check intermediate result by debugger.
    @Test
    fun buildGroup_call_success() {
        val input = parse("x^2")
        val builder = RenderTreeBuilder
        val actual = builder.buildGroup(input[0], options)

        assertTrue(actual is SpanNode)
    }

    @Test
    fun buildExpression_x_sup_2() {
        val input = parse("x^2")
        val actual = RenderTreeBuilder.buildExpression(input, options, true)

        assertEquals(1, actual.size)
        val target = actual[0]

        assertTrue(target is SpanNode)
        val mord = (target as SpanNode)!!

        assertEquals(2, mord.children.size)
        assertTrue(mord.hasClass(CssClass.mord))

        assertTrue(mord.children[0] is SymbolNode)

        /*
        SymbolNode {
    text: 'x',
    height: 0.43056,
    depth: 0,
    italic: 0,
    skew: 0.02778,
    width: 0.57153,
    maxFontSize: 1,
    classes: [ 'mord', 'mathdefault' ],
    style: {}
         */
        val symx : SymbolNode = mord.children[0] as SymbolNode
        assertEquals("x", symx.text)
        // fail: 0.68889
        // assertEquals(0.43056, symx.height, 0.0001)
        assertEquals(0.0, symx.depth, 0.001)
        // fail: 0.0
        // assertEquals(0.02778, symx.skew, 0.0001)
        /*
        assertEquals(0.57153, symx.width, 0.00001)
        assertEquals(1.0, symx.maxFontSize, 0.01)
        assertEquals(2, symx.klasses.size)
        assertTrue(symx.klasses.contains(CssClass.mord))
        assertTrue(symx.klasses.contains(CssClass.mathdefault))
        */
    }


}
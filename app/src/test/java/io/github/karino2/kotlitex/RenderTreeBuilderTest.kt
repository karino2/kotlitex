package io.github.karino2.kotlitex

import org.junit.Test

import org.junit.Assert.*

class SymbolAsserter(val node: SymbolNode) {
    fun text(v: String) = assertEquals(v, node.text)
    fun h(v: Double) = assertEquals(v, node.height, 0.0001)
    fun depth(v: Double) = assertEquals(v, node.depth, 0.001)
    fun skew(v: Double) = assertEquals(v, node.skew, 0.0001)
    fun w(v: Double) = assertEquals(v, node.width, 0.00001)
    fun maxFont(v: Double) = assertEquals(v, node.maxFontSize, 0.01)
    fun knum(numOfClass: Int) = assertEquals(numOfClass, node.klasses.size)
    fun kl(klass: CssClass) = assertTrue(node.klasses.contains(klass))
}

fun assertSymbol(node: RenderNode?, body: SymbolAsserter.()->Unit) {
    assertTrue(node is SymbolNode)
    val sym: SymbolNode = node as SymbolNode
    SymbolAsserter(sym).body()
}

class SpanAsserter(val node: SpanNode) {
    fun cnum(numOfChildren: Int) = assertEquals(numOfChildren, node.children.size)
    fun h(v: Double) = assertEquals(v, node.height, 0.0001)
    fun depth(v: Double) = assertEquals(v, node.depth, 0.001)
    fun maxFont(v: Double) = assertEquals(v, node.maxFontSize, 0.01)
    fun knum(numOfClass: Int) = assertEquals(numOfClass, node.klasses.size)
    fun kl(klass: CssClass) = assertTrue(node.klasses.contains(klass))
    fun style(st: CssStyle) = assertEquals(st, node.style)
    fun child(idx: Int) = node.children[idx]
    /*
        Span {children: Array(2), attributes: Object, classes: ["mord"],
         depth: 0, height: 0.8141079999999999, maxFontSize: 1, style:{}}
     */
}

fun assertSpan(node: RenderNode?, body: SpanAsserter.()->Unit) {
    assertTrue(node is SpanNode)
    val sym: SpanNode = node as SpanNode
    SpanAsserter(sym).body()
}

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


    /*
        Run the same code in node katex, then check value by debugger and write this test.
     */
    @Test
    fun buildExpression_xSup2_sameAsNode() {
        val input = parse("x^2")
        val actual = RenderTreeBuilder.buildExpression(input, options, true)

        assertEquals(1, actual.size)
        val target = actual[0]

        /*
        Span {children: Array(2), attributes: Object, classes: ["mord"],
         depth: 0, height: 0.8141079999999999, maxFontSize: 1, style:{}}

         */
        assertSpan(target) {
            knum(1)
            kl(CssClass.mord)
            depth(0.0)
            h(0.8141079999999999)
            maxFont(1.0)
            cnum(2)

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
            assertSymbol(child(0)) {
                text("x")
                h(0.43056)
                depth(0.0)
                skew(0.02778)
                w(0.57153)
                maxFont(1.0)
                knum(2)
                kl(CssClass.mord)
                kl(CssClass.mathdefault)
            }
            assertSpan(child(1)) {
                kl(CssClass.msupsub)
                h(0.8141079999999999)
                maxFont(0.7)
                cnum(1)
                assertSpan(child(0)) {
                    knum(1)
                    kl(CssClass.vlist_t)
                    depth(-0.363)
                    h(0.8141079999999999)
                    maxFont(0.7)
                    cnum(1)
                    assertSpan(child(0)) {
                        knum(1)
                        kl(CssClass.vlist_r)
                        depth(0.0)
                        maxFont(0.7)
                        h(0.45110799999999995)
                        cnum(1)
                        assertSpan(child(0)) {
                            knum(1)
                            kl(CssClass.vlist)
                            depth(0.0)
                            maxFont(0.7)
                            h(0.45110799999999995)
                            cnum(1)
                            assertSpan(child(0)) {
                                /* I don't known which is collect.
                                knum(1)
                                kl(CssClass.EMPTY)
                                 */
                                knum(0)
                                depth(0.0)
                                maxFont(0.7)
                                h(0.45110799999999995)
                                cnum(2)
                                assertSpan(child(0)) {
                                    knum(1)
                                    kl(CssClass.pstruct)
                                    depth(0.0)
                                    maxFont(0.0)
                                    h(0.0)
                                    style(CssStyle(height="2.7em"))
                                    cnum(0)
                                }
                                assertSpan(child(1)) {
                                    knum(4)
                                    kl(CssClass.sizing)
                                    kl(CssClass.reset_size6)
                                    kl(CssClass.size3)
                                    kl(CssClass.mtight)
                                    depth(0.0)
                                    maxFont(0.7)
                                    h(0.45110799999999995)
                                    cnum(1)
                                    assertSymbol(child(0)) {
                                        knum(2)
                                        kl(CssClass.mord)
                                        kl(CssClass.mtight)
                                        depth(0.0)
                                        h(0.64444)
                                        maxFont(0.7)
                                        skew(0.0)
                                        text("2")
                                        w(0.5)
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

    }


}
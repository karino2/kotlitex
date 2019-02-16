package io.github.karino2.kotlitex

import org.junit.Assert.*

class SymbolAsserter(val node: RNodeSymbol) {
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
    assertTrue(node is RNodeSymbol)
    val sym: RNodeSymbol = node as RNodeSymbol
    SymbolAsserter(sym).body()
}

class SpanAsserter(val node: RNodeSpan) {
    fun cnum(numOfChildren: Int) = assertEquals(numOfChildren, node.children.size)
    fun h(v: Double) = assertEquals(v, node.height, 0.0001)
    fun depth(v: Double) = assertEquals(v, node.depth, 0.001)
    fun maxFont(v: Double) = assertEquals(v, node.maxFontSize, 0.01)
    fun knum(numOfClass: Int) = assertEquals(numOfClass, node.klasses.size)
    fun kl(klass: CssClass) = assertTrue(node.klasses.contains(klass))
    fun style(st: CssStyle) = assertEquals(st, node.style)
    fun styleTop(top: String) = assertEquals(top, node.style.top)
    fun child(idx: Int) = node.children[idx]
    /*
        Span {children: Array(2), attributes: Object, classes: ["mord"],
         depth: 0, height: 0.8141079999999999, maxFontSize: 1, style:{}}
     */

    // short cut of assertSpan(child(n)) {}
    fun ac(childIndex: Int, body: SpanAsserter.()->Unit) {
        val next = child(childIndex)
        assertTrue(next is RNodeSpan)
        val sym: RNodeSpan = next as RNodeSpan
        SpanAsserter(sym).body()
    }
}

fun assertSpan(node: RenderNode?, body: SpanAsserter.()->Unit) {
    assertTrue(node is RNodeSpan)
    val sym: RNodeSpan = node as RNodeSpan
    SpanAsserter(sym).body()
}


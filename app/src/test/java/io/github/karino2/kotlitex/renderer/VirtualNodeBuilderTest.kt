package io.github.karino2.kotlitex.renderer

import io.github.karino2.kotlitex.Options
import io.github.karino2.kotlitex.Parser
import io.github.karino2.kotlitex.RenderTreeBuilder
import io.github.karino2.kotlitex.Style
import org.junit.Test

import org.junit.Assert.*

class VirtualNodeBuilderTest {
    @Test
    fun build() {
        val parser = Parser("x^2")
        val tree = RenderTreeBuilder.buildExpression(parser.parse(), Options(Style.TEXT), true)
        val builder = VirtualNodeBuilder(tree, baseSize = 10.0, headless = true)
        println(builder.build())
    }
}
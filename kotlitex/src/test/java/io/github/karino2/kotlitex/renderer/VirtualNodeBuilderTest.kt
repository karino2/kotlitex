package io.github.karino2.kotlitex.renderer

import android.graphics.Typeface
import io.github.karino2.kotlitex.Options
import io.github.karino2.kotlitex.Parser
import io.github.karino2.kotlitex.RenderTreeBuilder
import io.github.karino2.kotlitex.Style
import io.github.karino2.kotlitex.renderer.node.Bounds
import io.github.karino2.kotlitex.renderer.node.CssFont
import org.junit.Test

class VirtualNodeBuilderTest {
    class TestFontLoader : FontLoader {
        override fun measureSize(font: CssFont, text: String): Bounds {
            return Bounds(0.0, 0.0)
        }

        override fun toTypeface(font: CssFont): Typeface {
            TODO("not implemented")
        }
    }

    @Test
    fun build() {
        val parser = Parser("x^2")
        val tree = RenderTreeBuilder.buildExpression(parser.parse(), Options(Style.TEXT), true)
        val builder = VirtualNodeBuilder(tree, baseSize = 10.0, fontLoader = TestFontLoader())
        println(builder.build())
    }
}
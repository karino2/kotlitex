package io.github.karino2.kotlitex.renderer

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.github.karino2.kotlitex.renderer.node.CssFont
import org.junit.Assert.assertEquals
import org.junit.Test

import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidFontLoaderTest {
    @Test
    fun measureTextWidth() {
        val ctx = InstrumentationRegistry.getTargetContext()
        val loader = AndroidFontLoader(ctx.assets)
        val font = CssFont.create("KaTeX_Math", "", 10.0)
        assertEquals(6.0, loader.measureTextWidth(font, "x"), 0.01)
        assertEquals(22.0, loader.measureTextWidth(font, "hello"), 0.01)
    }

    @Test
    fun toTypefaceIsFlyweight() {
        val ctx = InstrumentationRegistry.getTargetContext()
        val loader = AndroidFontLoader(ctx.assets)
        val font = CssFont.create("KaTeX_Math", "", 10.0)
        assertEquals(loader.toTypeface(font), loader.toTypeface(font))
    }
}
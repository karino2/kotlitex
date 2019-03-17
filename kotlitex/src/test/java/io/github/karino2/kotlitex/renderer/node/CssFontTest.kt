package io.github.karino2.kotlitex.renderer.node

import org.junit.Assert.*
import org.junit.Test

class CssFontTest {
    @Test
    fun createWithoutVariant() {
        val font = CssFont("serif", 12.0)
        assertEquals("", font.variant)
    }
}
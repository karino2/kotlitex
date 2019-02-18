package io.github.karino2.kotlitex.renderer.node

import android.graphics.Typeface

enum class CssFontFamily {
    SERIF
}

/**
 * This class is designed to 1) run unit tests without mocking Android classes such as Typesafe and
 * 2) make the structure similar to canvas-latex.
 */
data class CssFont(val family: CssFontFamily, val size: Double) {
    fun getTypeface(): Typeface {
        return Typeface.SERIF
    }
}
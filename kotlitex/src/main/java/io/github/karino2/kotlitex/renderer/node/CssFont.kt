package io.github.karino2.kotlitex.renderer.node

import android.graphics.Typeface

enum class CssFontFamily {
    SERIF,
    KaTeX_Size2,
    Math_Italic,
    Main_Regular
}

/**
 * This class is designed to 1) run unit tests without mocking Android classes such as Typesafe and
 * 2) make the structure similar to canvas-latex.
 */
data class CssFont(val family: CssFontFamily, val size: Double) {
    companion object {
        fun create(fami: String, variant: String, size: Double) : CssFont{
            val family  = when(fami) {
                "KaTeX_Size2" -> CssFontFamily.KaTeX_Size2
                // TODO: should care variant.
                "KaTeX_Math" -> CssFontFamily.Math_Italic
                else -> CssFontFamily.Main_Regular
            }
            return CssFont(family, size)
        }
    }

    fun getTypeface(): Typeface {
        return Typeface.SERIF
    }
}
package io.github.karino2.kotlitex.renderer

import android.content.res.AssetManager
import android.graphics.Typeface
import io.github.karino2.kotlitex.renderer.node.CssFont
import io.github.karino2.kotlitex.renderer.node.CssFontFamily

class FontLoader(val assetManager: AssetManager) {
    // mathdefault seems KaTeX_Math, italic in KaTeX css.
    val mathDefault by lazy {
        Typeface.createFromAsset(assetManager, "fonts/KaTeX_Math-Italic.ttf")
    }

    val katexSize2 by lazy {
        Typeface.createFromAsset(assetManager, "fonts/KaTeX_Size2-Regular.ttf")
    }

    val default by lazy {
        Typeface.createFromAsset(assetManager, "fonts/KaTeX_Main-Regular.ttf")
    }

    fun toTypeface(font: CssFont): Typeface {
        return when (font.family) {
            CssFontFamily.KaTeX_Size2 -> katexSize2
            CssFontFamily.Math_Italic -> mathDefault
            else -> default
        }
    }
}
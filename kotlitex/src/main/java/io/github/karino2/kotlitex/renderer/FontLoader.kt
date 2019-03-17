package io.github.karino2.kotlitex.renderer

import android.content.res.AssetManager
import android.graphics.Paint
import android.graphics.Typeface
import io.github.karino2.kotlitex.renderer.node.CssFont
import io.github.karino2.kotlitex.renderer.node.CssFontFamily

interface FontLoader {
    fun measureTextWidth(font: CssFont, text: String): Double
    fun toTypeface(font: CssFont): Typeface
}

class AndroidFontLoader(private val assetManager: AssetManager) :
    FontLoader {
    private val typefaceMap =
        listOf(
            "AMS-Regular",
            "Caligraphic-Bold",
            "Caligraphic-Regular",
            "Fraktur-Bold",
            "Fraktur-Regular",
            "Main-Bold",
            "Main-BoldItalic",
            "Main-Italic",
            "Main-Regular",
            "Math-BoldItalic",
            "Math-Italic",
            "Math-Regular",
            "SansSerif-Bold",
            "SansSerif-Italic",
            "SansSerif-Regular",
            "Script-Regular",
            "Size1-Regular",
            "Size2-Regular",
            "Size3-Regular",
            "Size4-Regular",
            "Typewriter-Regular"
            ).map {
            it to Typeface.createFromAsset(assetManager, "fonts/KaTeX_$it.ttf")
        }.toMap()

    override fun measureTextWidth(font: CssFont, text: String): Double {
        val paint = Paint()
        paint.typeface = toTypeface(font)
        paint.textSize = font.size.toFloat()
        return paint.measureText(text).toDouble()
    }

    override fun toTypeface(font: CssFont): Typeface {
        return when (font.family) {
            CssFontFamily.KaTeX_Size2 -> typefaceMap["Size2-Regular"]!!
            CssFontFamily.Math_Italic -> typefaceMap["Math-Italic"]!!
            else -> typefaceMap["Main-Regular"]!!
        }
    }
}
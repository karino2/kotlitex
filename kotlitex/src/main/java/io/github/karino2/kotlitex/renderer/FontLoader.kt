package io.github.karino2.kotlitex.renderer

import android.content.res.AssetManager
import android.graphics.Paint
import android.graphics.Typeface
import io.github.karino2.kotlitex.renderer.node.CssFont

interface FontLoader {
    fun measureTextWidth(font: CssFont, text: String): Double
    fun toTypeface(font: CssFont): Typeface
}

class AndroidFontLoader(private val assetManager: AssetManager) :
    FontLoader {
    private val typefaceMap =
        listOf(
            "KaTeX_AMS-Regular",
            "KaTeX_Caligraphic-Bold",
            "KaTeX_Caligraphic-Regular",
            "KaTeX_Fraktur-Bold",
            "KaTeX_Fraktur-Regular",
            "KaTeX_Main-Bold",
            "KaTeX_Main-BoldItalic",
            "KaTeX_Main-Italic",
            "KaTeX_Main-Regular",
            "KaTeX_Math-BoldItalic",
            "KaTeX_Math-Italic",
            "KaTeX_Math-Regular",
            "KaTeX_SansSerif-Bold",
            "KaTeX_SansSerif-Italic",
            "KaTeX_SansSerif-Regular",
            "KaTeX_Script-Regular",
            "KaTeX_Size1-Regular",
            "KaTeX_Size2-Regular",
            "KaTeX_Size3-Regular",
            "KaTeX_Size4-Regular",
            "KaTeX_Typewriter-Regular"
            ).map {
            it to Typeface.createFromAsset(assetManager, "fonts/$it.ttf")
        }.toMap()

    override fun measureTextWidth(font: CssFont, text: String): Double {
        val paint = Paint()
        paint.typeface = toTypeface(font)
        paint.textSize = font.size.toFloat()
        return paint.measureText(text).toDouble()
    }

    companion object {
        fun fontToTypefaceMapKey(font: CssFont): String {
            var variant = font.variant.capitalize()
            if (variant.isEmpty() || variant == "Normal") {
                variant = "Regular"
            }
            return "${font.family}-$variant"
        }
    }

    override fun toTypeface(font: CssFont): Typeface {
        return typefaceMap.getValue(fontToTypefaceMapKey(font))
    }
}
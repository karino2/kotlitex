package io.github.karino2.kotlitex.renderer

import android.content.res.AssetManager
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import io.github.karino2.kotlitex.renderer.node.Bounds
import io.github.karino2.kotlitex.renderer.node.CssFont

interface FontLoader {
    fun measureSize(font: CssFont, text: String): Bounds
    fun toTypeface(font: CssFont): Typeface
}

class AndroidFontLoader(private val assetManager: AssetManager) :
    FontLoader {
    private val paint = Paint()

    fun measureSizeWithoutSpace(font: CssFont, text: String): Bounds {
        val rect = Rect()

        paint.typeface = toTypeface(font)
        paint.textSize = font.size.toFloat()
        paint.getTextBounds(text, 0, text.length, rect)
        return Bounds(0.0, 0.0, rect.width().toDouble(), rect.height().toDouble())
    }

    override fun measureSize(font: CssFont, text: String): Bounds {
        // When measure bound of "\u00a0", android return 0 width.
        // But we sometime use "\u00a0" for spacing, so I use half of "a" instead for white space measure.
        if (text == "\u00a0") {
            val b = measureSizeWithoutSpace(font, "a")
            return Bounds(0.0, 0.0, b.width / 2, b.height)
        }
        return measureSizeWithoutSpace(font, text)
    }

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

    companion object {
        fun fontToTypefaceMapKey(font: CssFont): String {
            var variant = font.variant.capitalize()
            if (variant == "Normal") {
                variant = ""
            }
            var weight = font.weight.capitalize()
            if (weight == "Normal") {
                weight = ""
            }

            var wv = "$weight$variant"
            if (wv.isEmpty()) {
                wv = "Regular"
            }

            return "${font.family}-$wv"
        }
    }

    override fun toTypeface(font: CssFont): Typeface {
        return typefaceMap.getValue(fontToTypefaceMapKey(font))
    }
}
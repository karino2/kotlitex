package io.github.karino2.kotlitex.renderer.node

import android.graphics.Paint
import android.graphics.Typeface

class TextNode(val text: String,
               val typeface: Typeface,
               val textSize: Double,
               val color: String,
               klasses: Set<String>) : VirtualCanvasNode(klasses) {
    init {
        val paint = Paint()
        paint.typeface = typeface
        paint.textSize = textSize.toFloat()
        bounds.width = paint.measureText(text).toDouble()
    }
}
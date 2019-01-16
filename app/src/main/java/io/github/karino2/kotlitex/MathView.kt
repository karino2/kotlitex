package io.github.karino2.kotlitex

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class MathView(context: Context, attrSet: AttributeSet) : View(context, attrSet) {
    val nodes: List<RenderNode>
    init {
        val options = Options(Style.TEXT)
        val parser = Parser("x^2")
        val parsed =  parser.parse()
        nodes = RenderTreeBuilder.buildExpression(parsed, options, true)
    }
    var point: PointF = PointF(0f, 0f)

    val BaseTextSize: Double = 50.0;

    val textPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.create("serif", Typeface.NORMAL)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // The way it keeps the current cursor here doesn't look right. Maybe introducing Renderer later?
        point.x = 100f
        point.y = 100f

        drawRenderNodes(canvas, nodes)
    }

    private fun drawRenderNodes(canvas: Canvas, nodes: List<RenderNode>) {
        for (parent in nodes) {
            when (parent) {
                is SpanNode -> {
                    drawRenderNodes(canvas, parent.children)
                }
                is SymbolNode -> {
                    textPaint.textSize = (BaseTextSize * parent.maxFontSize).toFloat()
                    canvas.drawText(parent.text, point.x, point.y, textPaint)

                    // Assuming that the whole rendering process is Left-to-Right, which would not be true
                    point.x += (BaseTextSize * parent.width).toFloat()
                }
            }
        }
    }
}
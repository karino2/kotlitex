package io.github.karino2.kotlitex

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import io.github.karino2.kotlitex.renderer.*
import io.github.karino2.kotlitex.renderer.node.*

class MathView(context: Context, attrSet: AttributeSet) : View(context, attrSet) {
    private val BASE_X = 100.0f
    private val BASE_Y = 100.0f

    var rootNode: VerticalList
    init {
        val options = Options(Style.TEXT)
        val parser = Parser("\\frac{1}{2000}")
        val parsed =  parser.parse()
        val nodes = RenderTreeBuilder.buildExpression(parsed, options, true)
        val builder = VirtualNodeBuilder(nodes)
        rootNode = builder.build()
    }

    val paint = Paint()
    val textPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.SERIF
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRenderNodes(canvas, rootNode)
    }

    private fun translateX(x: Double): Float {
        return x.toFloat() + BASE_X
    }

    private fun translateY(y: Double): Float {
        return y.toFloat() + BASE_Y
    }

    private fun drawRenderNodes(canvas: Canvas, parent: VirtualCanvasNode) {
        when (parent) {
            is VirtualContainerNode<*> -> {
                parent.nodes.forEach {
                    drawRenderNodes(canvas, it)
                }
            }
            is TextNode -> {
                textPaint.typeface = parent.font.getTypeface()
                textPaint.textSize = parent.font.size.toFloat()
                val x = translateX(parent.bounds.x)
                val y = translateY(parent.bounds.y)
                canvas.drawText(parent.text, x, y, textPaint)
            }
            is HorizontalLineNode -> {
                paint.color = Color.BLACK
                paint.strokeWidth = 1.0f
                val x = translateX(parent.bounds.x)
                val y = translateY(parent.bounds.y)
                canvas.drawLine(x, y, x + parent.bounds.width.toFloat(), y, paint)
            }
        }
    }
}
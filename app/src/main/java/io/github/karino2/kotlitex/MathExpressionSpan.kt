package io.github.karino2.kotlitex

import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.DynamicDrawableSpan
import io.github.karino2.kotlitex.renderer.VirtualNodeBuilder
import io.github.karino2.kotlitex.renderer.node.*

private class MathExpressionDrawable(expr: String) : Drawable() {
    var rootNode: VerticalList
    init {
        val options = Options(Style.TEXT)
        val parser = Parser(expr)
        val parsed =  parser.parse()
        val nodes = RenderTreeBuilder.buildExpression(parsed, options, true)
        val builder = VirtualNodeBuilder(nodes)
        rootNode = builder.build()
    }

    val paint = Paint()
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.SERIF
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun draw(canvas: Canvas) {
        drawRenderNodes(canvas, rootNode)
    }

    // TODO
    override fun getIntrinsicWidth(): Int = 200
    override fun getIntrinsicHeight(): Int = 200
    private fun translateX(x: Double): Float {
        return x.toFloat() + 100
    }
    private fun translateY(y: Double): Float {
        return y.toFloat() + 100
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

class MathExpressionSpan(val expr: String) : DynamicDrawableSpan() {
    override fun getDrawable(): Drawable {
        val drawable = MathExpressionDrawable(expr)
        drawable.setBounds(drawable.bounds.left, drawable.bounds.top, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        return drawable
    }
}
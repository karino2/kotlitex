package io.github.karino2.kotlitex

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import io.github.karino2.kotlitex.renderer.*
import io.github.karino2.kotlitex.renderer.node.TextNode
import io.github.karino2.kotlitex.renderer.node.VerticalList
import io.github.karino2.kotlitex.renderer.node.VirtualCanvasNode
import io.github.karino2.kotlitex.renderer.node.VirtualContainerNode

class MathView(context: Context, attrSet: AttributeSet) : View(context, attrSet) {
    var rootNode: VerticalList
    init {
        val options = Options(Style.TEXT)
        val parser = Parser("x^2")
        val parsed =  parser.parse()
        val nodes = RenderTreeBuilder.buildExpression(parsed, options, true)
        val builder = VirtualNodeBuilder(nodes)
        rootNode = builder.build()
    }

    val textPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.SERIF
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRenderNodes(canvas, rootNode)
    }

    private fun drawRenderNodes(canvas: Canvas, parent: VirtualCanvasNode) {
        when (parent) {
            is VirtualContainerNode<*> -> {
                for (node in parent.nodes) {
                    drawRenderNodes(canvas, node)
                }
            }
            is TextNode -> {
                textPaint.typeface = parent.typeface
                textPaint.textSize = parent.textSize.toFloat()
                canvas.drawText(parent.text, 100 + parent.bounds.x.toFloat(), 100 + parent.bounds.y.toFloat(), textPaint)
            }
        }
    }
}
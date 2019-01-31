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
            is PathNode -> {
                val x = translateX(parent.bounds.x)
                val y = translateY(parent.bounds.y)


                // TODO: support other preserve aspect ratio.
                // "xMinYMin slice"
                val mat = Matrix()

                val (minx, miny, widthvb, heightvb) = parent.rnode.viewBox
                val viewBoxF = RectF(minx.toFloat(), miny.toFloat(), (minx+widthvb).toFloat(), (miny+heightvb).toFloat())
                val (_, _, wb, hb) = parent.bounds
                val dstBox = RectF(x, y, (x+wb).toFloat(), (y+hb).toFloat())

                mat.setRectToRect(viewBoxF, dstBox, Matrix.ScaleToFit.START)

                /* trial code. Please remove once we reach to correct rendering.

                val inPath = parent.rnode.children[0]!!.path

                // OK
                val inPath = Path()
                inPath.moveTo(95f, 702f)
                inPath.lineTo(400000f, 702f)


                // NG
                val inPath = Path()
                inPath.moveTo(95f, 702f)
                //             c(-2.7,0.0,-7.17,-2.7,-13.5,-8.0)
                inPath.rCubicTo(-2.7f, 0.0f,-7.17f,-2.7f,-13.5f,-8.0f)
                inPath.close()

                // draw something (streight line)
                // inPath.rCubicTo(0f, -0f, 200000f, -440.3f, 400000f, 0f)

                val tmppaint =Paint()
                tmppaint.setColor(Color.BLUE)
                tmppaint.setAntiAlias(true)
                tmppaint.setStrokeWidth(5f)
                tmppaint.setStyle(Paint.Style.STROKE)

                val path = Path()

                // OK.
                val rf = RectF(x, y, x+50f, y+50f)
                path.arcTo(rf, 0f, 180f)

                // OK
                path.moveTo(x, y)
                path.lineTo(x+100f, y)

                path.addPath(inPath, mat)

                paint.color = Color.BLACK
                paint.strokeWidth = 2.0f
                paint.style = Paint.Style.FILL_AND_STROKE


                canvas.drawPath(path, paint)
                */


                // TODO: more suitable handling.
                paint.color = Color.BLACK
                paint.strokeWidth = 2.0f
                paint.style = Paint.Style.FILL_AND_STROKE

                val path = Path()

                parent.rnode.children.forEach{
                    path.reset()
                    path.addPath(it.path, mat)
                    canvas.drawPath(path, paint)
                }
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
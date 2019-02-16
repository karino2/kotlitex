package io.github.karino2.kotlitex

import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.DynamicDrawableSpan
import io.github.karino2.kotlitex.renderer.VirtualNodeBuilder
import io.github.karino2.kotlitex.renderer.node.*

private class MathExpressionDrawable(expr: String, baseSize: Float, val drawBounds: Boolean = false) : Drawable() {
    var rootNode: VerticalList
    init {
        val options = Options(Style.DISPLAY)
        val parser = Parser(expr)
        val parsed =  parser.parse()
        val nodes = RenderTreeBuilder.buildExpression(parsed, options, true)
        val builder = VirtualNodeBuilder(nodes, baseSize.toDouble())
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
    override fun getIntrinsicWidth(): Int = 400
    override fun getIntrinsicHeight(): Int = 400
    private fun translateX(x: Double): Float {
        return x.toFloat() + 100
    }
    private fun translateY(y: Double): Float {
        return y.toFloat() + 200
    }

    private fun drawBounds(canvas: Canvas, bounds: Bounds) {
        if (! drawBounds) {
            return
        }

        val x = translateX(bounds.x)
        val y = translateY(bounds.y)

        paint.color = Color.RED
        paint.strokeWidth = 1.0f
        paint.style = Paint.Style.STROKE

        canvas.drawRect(RectF(x, y, x+ bounds.width.toFloat(), y + bounds.height.toFloat()), paint)
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
                drawBounds(canvas, parent.bounds)
            }
            is HorizontalLineNode -> {
                paint.color = Color.BLACK
                paint.strokeWidth = 1.0f
                val x = translateX(parent.bounds.x)
                val y = translateY(parent.bounds.y)
                canvas.drawLine(x, y, x + parent.bounds.width.toFloat(), y, paint)
                drawBounds(canvas, parent.bounds)
            }
            is PathNode -> {
                val x = translateX(parent.bounds.x)
                val y = (translateY(parent.bounds.y) - parent.bounds.height).toFloat()

                drawBounds(canvas, parent.bounds)


                // TODO: support other preserve aspect ratio.
                // "xMinYMin slice"
                val mat = Matrix()

                val heightvb = parent.rnode.viewBox.height
                val (_, _, wb, hb) = parent.bounds

                // this is preserveAspectRatio = meet
                /*
                val (minx, miny, widthvb, heightvb) = parent.rnode.viewBox
                    val viewBoxF = RectF(minx.toFloat(), miny.toFloat(), (minx+widthvb).toFloat(), (miny+heightvb).toFloat())
                    val dstBox = RectF(x, y, (x+wb).toFloat(), (y+hb).toFloat())
                    mat.setRectToRect(viewBoxF, dstBox, Matrix.ScaleToFit.START)
                 */

                /*
                Basically, width is far larger than height.
                So we scale to fit to height, then clip.
                 */
                val scale = (hb/heightvb).toFloat()
                mat.postScale(scale, scale)
                mat.postTranslate(x, y)

                // TODO: more suitable handling.
                paint.color = Color.BLACK
                paint.strokeWidth = 2.0f
                paint.style = Paint.Style.FILL_AND_STROKE

                val path = Path()

                canvas.save()
                canvas.clipRect(RectF(x, y, x+wb.toFloat(), y+hb.toFloat()))

                parent.rnode.children.forEach{
                    path.reset()
                    path.addPath(it.path, mat)
                    canvas.drawPath(path, paint)
                }

                canvas.restore()
                paint.style = Paint.Style.STROKE
            }
        }
    }

}

class MathExpressionSpan(val expr: String, val baseSize: Float = 44.0f) : DynamicDrawableSpan() {
    override fun getDrawable(): Drawable {
        // TODO: drawBounds should be always false. Unlike baseSize, we don't have to expose the flag to end-users.
        val drawable = MathExpressionDrawable(expr, baseSize, drawBounds = false)
        drawable.setBounds(drawable.bounds.left, drawable.bounds.top, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        return drawable
    }
}
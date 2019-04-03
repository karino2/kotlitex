package io.github.karino2.kotlitex

import android.content.res.AssetManager
import android.graphics.*
import android.text.TextPaint
import android.text.style.ReplacementSpan
import android.util.Log
import io.github.karino2.kotlitex.renderer.AndroidFontLoader
import io.github.karino2.kotlitex.renderer.FontLoader
import io.github.karino2.kotlitex.renderer.VirtualNodeBuilder
import io.github.karino2.kotlitex.renderer.node.*
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


private class MathExpressionDrawable(expr: String, baseSize: Float, val fontLoader: FontLoader, isMathMode: Boolean, val drawBounds: Boolean = false)  {
    var rootNode: VerticalList
    init {
        val options = if(isMathMode) Options(Style.DISPLAY) else Options(Style.TEXT)
        val parser = Parser(expr)
        val parsed =  parser.parse()
        val nodes = RenderTreeBuilder.buildExpression(parsed, options, true)
        val builder = VirtualNodeBuilder(nodes, baseSize.toDouble(), fontLoader)
        rootNode = builder.build()
    }

    val paint = Paint()
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.SERIF
    }

    private fun translateX(x: Double): Float {
        return x.toFloat()
    }
    private fun translateY(y: Double): Float {
        return y.toFloat()
    }

    private fun drawBounds(canvas: Canvas, bounds: Bounds, ratio: Float) {
        if (! drawBounds) {
            return
        }

        val x = translateX(bounds.x*ratio)
        val y = translateY(bounds.y*ratio)

        paint.color = Color.RED
        paint.strokeWidth = 1.0f
        paint.style = Paint.Style.STROKE

        // TODO: This doesn't look right though...
        // canvas.drawRect(RectF(x, y - bounds.height.toFloat()*ratio, x + bounds.width.toFloat()*ratio, y), paint)
        canvas.drawRect(RectF(x, y - bounds.height.toFloat()*ratio, x + bounds.width.toFloat()*ratio, y + bounds.height.toFloat()*ratio), paint)
    }

    private fun drawRenderNodes(canvas: Canvas, parent: VirtualCanvasNode, ratio: Float) {
        when (parent) {
            is VirtualContainerNode<*> -> {
                parent.nodes.forEach {
                    drawRenderNodes(canvas, it, ratio)
                }
            }
            is TextNode -> {
                textPaint.typeface = fontLoader.toTypeface(parent.font)
                textPaint.textSize = parent.font.size.toFloat()*ratio
                val x = translateX(parent.bounds.x*ratio)
                val y = translateY(parent.bounds.y*ratio)
                canvas.drawText(parent.text, x, y, textPaint)
                drawBounds(canvas, parent.bounds, ratio)
            }
            is HorizontalLineNode -> {
                paint.color = Color.BLACK
                paint.strokeWidth = parent.bounds.height.toFloat()*ratio
                val x = translateX(parent.bounds.x*ratio)
                val y = translateY(parent.bounds.y*ratio)
                canvas.drawLine(x, y, x + parent.bounds.width.toFloat()*ratio, y, paint)
                drawBounds(canvas, parent.bounds, ratio)
            }
            is PathNode -> {
                val x = translateX(parent.bounds.x*ratio)
                val y = (translateY(parent.bounds.y*ratio) - parent.bounds.height*ratio).toFloat()

                drawBounds(canvas, parent.bounds, ratio)


                // TODO: support other preserve aspect ratio.
                // "xMinYMin slice"
                val mat = Matrix()

                val heightvb = parent.rnode.viewBox.height
                val (_, _, wbVirtual, hbVirtual) = parent.bounds
                val wb = wbVirtual*ratio
                val hb = hbVirtual*ratio

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

    fun drawWithRatio(canvas: Canvas, ratio: Float) {
        if(drawBounds)
            drawBounds(canvas, calculateWholeBounds(), ratio)
        drawRenderNodes(canvas, rootNode, ratio)
    }

    fun calculateBounds(wholeBounds : Bounds, parent: VirtualCanvasNode) {
        if(parent.bounds.width != 0.0) {
            wholeBounds.extend(parent.bounds)
            return
        }
        when (parent) {
            is VirtualContainerNode<*> -> {
                parent.nodes.forEach {
                    calculateBounds(wholeBounds, it)
                }
            }
        }
    }

    val bounds by lazy {
        val b = calculateWholeBounds()

        with(b) {
            Rect(0, 0, (width).toInt(), (height).toInt())
        }
    }

    private fun calculateWholeBounds(): Bounds {
        val b = Bounds(rootNode.bounds.x, rootNode.bounds.y)
        calculateBounds(b, rootNode)

        // place rect to 0, 0
        b.x = 0.0
        b.y = 0.0

        return b
    }

}

// Similar to DynamicDrawableSpan, but getSize is a little different.
// I create super class of DynamicDrawableSpan because getCachedDrawable is private and we neet it.
class MathExpressionSpan(val expr: String, val baseHeight: Float, val assetManager: AssetManager, val isMathMode: Boolean) : ReplacementSpan() {
    enum class Align {
        Bottom, BaseLine
    }

    var verticalAlignment = Align.Bottom

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        if(isError)
            return (baseHeight*5).toInt()
        try {
            return getSizeWithException(fm)
        }catch(err: ParseError) {
            Log.d("kotlitex", err.msg)
            isError = true
            return (baseHeight*5).toInt()
        }
    }

    var isError = false

    private fun getSizeWithException(fm: Paint.FontMetricsInt?): Int {
        val d = getCachedDrawable()
        val rect = d.bounds

        val ratio = baseHeight / virtualBaseHeight
        if (fm == null) {
            return (rect.right * ratio).roundToInt()
        }

        val scaledBottom = (rect.bottom * ratio + 0.5).roundToInt()

        val ascent = scaledBottom / 2
        val descent = scaledBottom - ascent

        fm.ascent = -ascent
        fm.descent = descent

        fm.bottom = fm.descent
        fm.top = -scaledBottom

        // should we roundUp?
        return (rect.right * ratio).roundToInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        if(isError) {
            canvas.drawText("ERROR", x, y.toFloat(), paint)
            return
        }

        val b = getCachedDrawable()

        // We always use full height. So we do not need any alignment, right?
        // val ratio = (bottom-top).toDouble() / b.bounds.bottom.toDouble()
        val ratio = baseHeight / virtualBaseHeight

        // Log.d("kotlitex", "x=$x, y=$y, top=$top, ratio=$ratio, expr=$expr")



        canvas.save()
        canvas.translate(x, y.toFloat())
        b.drawWithRatio(canvas, ratio.toFloat())

        canvas.restore()
    }
    val virtualBaseHeight = 100.0f

    private fun getDrawable(): MathExpressionDrawable {
        // TODO: drawBounds should be always false. Unlike baseSize, we don't have to expose the flag to end-users.
        val drawable = MathExpressionDrawable(expr, virtualBaseHeight,
            AndroidFontLoader(assetManager), isMathMode, drawBounds = false)
        return drawable
    }

    private fun getCachedDrawable() : MathExpressionDrawable {
        val wr = drawableRef
        val d = wr?.get()
        if (d != null)
            return d
        val newDrawable = getDrawable()
        drawableRef = WeakReference(newDrawable)
        return newDrawable
    }

    private var drawableRef : WeakReference<MathExpressionDrawable>? = null
}
package io.github.karino2.kotlitex

import android.content.res.AssetManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import android.util.Log
import io.github.karino2.kotlitex.renderer.AndroidFontLoader
import io.github.karino2.kotlitex.renderer.FontLoader
import io.github.karino2.kotlitex.renderer.VirtualNodeBuilder
import io.github.karino2.kotlitex.renderer.node.*
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


private class MathExpressionDrawable(expr: String, baseSize: Float, val fontLoader: FontLoader, val drawBounds: Boolean = false) : Drawable() {
    var rootNode: VerticalList
    init {
        val options = Options(Style.DISPLAY)
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

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun draw(canvas: Canvas) {
        drawRenderNodes(canvas, rootNode, 1.0f)
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

    private fun drawBounds(canvas: Canvas, bounds: Bounds, ratio: Float) {
        if (! drawBounds) {
            return
        }

        val x = translateX(bounds.x)*ratio
        val y = translateY(bounds.y)*ratio

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
                // TODO: temp font test.
                textPaint.typeface = fontLoader.toTypeface(parent.font)
                textPaint.textSize = parent.font.size.toFloat()*ratio
                val x = translateX(parent.bounds.x)*ratio
                val y = translateY(parent.bounds.y)*ratio
                canvas.drawText(parent.text, x, y, textPaint)
                drawBounds(canvas, parent.bounds, ratio)
            }
            is HorizontalLineNode -> {
                paint.color = Color.BLACK
                paint.strokeWidth = parent.bounds.height.toFloat()*ratio
                val x = translateX(parent.bounds.x)*ratio
                val y = translateY(parent.bounds.y)*ratio
                canvas.drawLine(x, y, x + parent.bounds.width.toFloat()*ratio, y, paint)
                drawBounds(canvas, parent.bounds, ratio)
            }
            is PathNode -> {
                val x = translateX(parent.bounds.x)*ratio
                val y = (translateY(parent.bounds.y) - parent.bounds.height).toFloat()*ratio

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
        drawRenderNodes(canvas, rootNode, ratio)
    }

}

// Similar to DynamicDrawableSpan, but getSize is a little different.
// I create super class of DynamicDrawableSpan because getCachedDrawable is private and we neet it.
class MathExpressionSpan(val expr: String, val baseHeightSpecified: Float?, val assetManager: AssetManager) : ReplacementSpan() {
    enum class Align {
        Bottom, BaseLine
    }

    var verticalAlignment = Align.Bottom

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val d = getCachedDrawable()
        val rect = d.bounds

        baseHeightSpecified?.let { targetHeight ->
            val ratio = targetHeight/virtualBaseHeight
            if(fm == null) {
                return (rect.right*ratio).roundToInt()
            }


            val lineNum = rect.bottom/virtualBaseHeight
            // We follow DynamicDrawableSpan logic. But it's a little different from no-size specified case.
            fm.ascent = -(lineNum*targetHeight).roundToInt()
            fm.descent = 0
            fm.top = fm.ascent
            fm.bottom = 0
            // should we roundUp?
            return (rect.right*ratio).roundToInt()
        }
        // below here is baseHeightSpecified == null case.

        if(fm != null) {
            // TODO: consider handling of baseline.
            // I think we should align drawable's drawRenderNode baseline to fm.baseline.
            // But our font is different, so it's not obvious whether my assumption is correct.
            // Currently, I just use whole height as our box height.


            // fm.bottom and top seems zero. There seems no way to know current line height.
            /*
            val lineHeight = fm.bottom - fm.top
            // sometime, bottom and top is zero. in this case, we regard
            val ratio = if(lineHeight == 0) 1.0 else lineHeight/ rect.bottom.toDouble()

            // should we roundup?
            return (rect.right*ratio).roundToInt()
             */
            fm.ascent = -rect.bottom
            fm.descent = 0
            fm.top = fm.ascent
            fm.bottom
        }

        return rect.right
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
        val b = getCachedDrawable()

        // We always use full height. So we do not need any alignment, right?
        val ratio = (bottom-top).toDouble() / b.bounds.bottom.toDouble()
        Log.d("kotlitex", "ratio=$ratio")



        canvas.save()
        canvas.translate(x, top.toFloat())
        b.drawWithRatio(canvas, ratio.toFloat())

        canvas.restore()
    }
    val virtualBaseHeight = 100.0f

    private fun getDrawable(): MathExpressionDrawable {
        // TODO: drawBounds should be always false. Unlike baseSize, we don't have to expose the flag to end-users.
        val drawable = MathExpressionDrawable(expr, virtualBaseHeight,
            AndroidFontLoader(assetManager), drawBounds = true)
        drawable.setBounds(drawable.bounds.left, drawable.bounds.top, drawable.intrinsicWidth, drawable.intrinsicHeight);
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
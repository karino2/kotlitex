package io.github.karino2.kotlitex.view

import android.content.res.AssetManager
import android.graphics.* // ktlint-disable no-wildcard-imports
import android.text.TextPaint
import android.text.style.ReplacementSpan
import android.util.Log
import io.github.karino2.kotlitex.* // ktlint-disable no-wildcard-imports
import io.github.karino2.kotlitex.renderer.AndroidFontLoader
import io.github.karino2.kotlitex.renderer.FontLoader
import io.github.karino2.kotlitex.renderer.VirtualNodeBuilder
import io.github.karino2.kotlitex.renderer.node.* // ktlint-disable no-wildcard-imports
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.roundToInt

private class MathExpressionDrawable(expr: String, baseSize: Float, val fontLoader: FontLoader, val isMathMode: Boolean, val drawBounds: Boolean = false) {
    var rootNode: VerticalList

    val firstVListRowBound: Bounds?
    get() {
        if (rootNode.nodes.isEmpty())
            return null

        val b = Bounds(rootNode.bounds.x, rootNode.bounds.y)
        calculateBounds(b, rootNode.nodes[0])
        return b
    }

    // for debug use only
    val debugBaseSize = baseSize
    init {
        val options = if (isMathMode) Options(Style.DISPLAY) else Options(
            Style.TEXT
        )
        val parser = Parser(expr)
        val parsed = parser.parse()
        val nodes = RenderTreeBuilder.buildHTML(parsed, options)
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

    private fun drawBoundsRect(canvas: Canvas, rect: RectF, color: Int) {
        if (! drawBounds) {
            return
        }

        paint.color = color
        paint.strokeWidth = 2.0f
        paint.style = Paint.Style.STROKE

        canvas.drawRect(rect, paint)
    }

    private fun drawBounds(canvas: Canvas, bounds: Bounds) {
        if (! drawBounds) {
            return
        }

        val x = translateX(bounds.x)
        val y = translateY(bounds.y)

        drawBoundsRect(canvas, RectF(x, y - bounds.height.toFloat(), x + bounds.width.toFloat(), y), Color.RED)
    }

    private fun drawWholeBound(canvas: Canvas, bounds: Bounds) {
        if (! drawBounds) {
            return
        }

        val x = translateX(bounds.x)

        val firstBound = firstVListRowBound ?: return

        val ascent = (0.5 + firstBound.height * 4 / 5).toInt()

        /*
        work around for \sum^N_i case. (#161)
        In this case, firstBound.y becomes negative and normal acent calculation make ascent too upper.
        I don't know how to handle this, so extend descend to try to avoid overlap for this case.
         */
        val deltaDescent = -firstBound.y

        val y = -ascent
        // val padding = (ascent/9).toInt()
        val padding = 0
        drawBoundsRect(canvas, RectF(x, (y - padding).toFloat(), x + bounds.width.toFloat() + padding, y + padding*2 + (bounds.height + deltaDescent).toFloat()), Color.BLUE)
    }

    private fun drawRenderNodes(canvas: Canvas, parent: VirtualCanvasNode) {
        when (parent) {
            is VirtualContainerNode<*> -> {
                parent.nodes.forEach {
                    drawRenderNodes(canvas, it)
                }
            }
            is TextNode -> {
                textPaint.typeface = fontLoader.toTypeface(parent.font)
                textPaint.textSize = parent.font.size.toFloat()
                val x = translateX(parent.bounds.x)
                val y = translateY(parent.bounds.y)
                canvas.drawText(parent.text, x, y, textPaint)
                drawBounds(canvas, parent.bounds)
            }
            is HorizontalLineNode -> {
                paint.color = Color.BLACK
                paint.strokeWidth = max(1.0f, parent.bounds.height.toFloat())
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
                val (_, _, wbVirtual, hbVirtual) = parent.bounds
                val wb = wbVirtual
                val hb = hbVirtual

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
                val scale = (hb / heightvb).toFloat()
                mat.postScale(scale, scale)
                mat.postTranslate(x, y)

                // TODO: more suitable handling.
                paint.color = Color.BLACK
                paint.strokeWidth = 2.0f
                paint.style = Paint.Style.FILL_AND_STROKE

                val path = Path()

                canvas.save()
                canvas.clipRect(RectF(x, y, x + wb.toFloat(), y + hb.toFloat()))

                parent.rnode.children.forEach {
                    path.reset()
                    path.addPath(it.path, mat)
                    canvas.drawPath(path, paint)
                }

                canvas.restore()
                paint.style = Paint.Style.STROKE
            }
        }
    }

    fun drawAllRenderNodes(canvas: Canvas) {
        if (drawBounds)
            drawWholeBound(canvas, calculateWholeBounds())
        drawRenderNodes(canvas, rootNode)
    }

    fun calculateBounds(wholeBounds: Bounds, parent: VirtualCanvasNode) {
        if (parent.bounds.width != 0.0) {
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
        ensureDrawable()
        if (isError)
            return (baseHeight * 5).toInt()
        return getSizeInternal(fm)
    }

    var isError = false

    private fun getSizeInternal(fm: Paint.FontMetricsInt?): Int {
        val d = getCachedDrawable()
        val rect = d.bounds

        val firstBound = d.firstVListRowBound
        if (fm == null || firstBound == null) {
            return rect.right
        }

        val bottom = (rect.bottom + 0.5).roundToInt()

        val ascent = (0.5 + firstBound.height * 4 / 5).toInt()

        /*
        work around for \sum^N_i case. (#161)
        In this case, firstBound.y becomes negative and normal acent calculation make ascent too upper.
        I don't know how to handle this, so extend descend to try to avoid overlap for this case.
         */
        val deltaDescent = (0.5 - firstBound.y).roundToInt()

        val padding = ascent / 9
        val descent = bottom - ascent + deltaDescent

        fm.ascent = -ascent - padding
        fm.descent = descent + padding

        fm.bottom = fm.descent
        fm.top = -ascent

        return (rect.right + 0.5 + padding).roundToInt()
    }

    fun ensureDrawable() {
        try {
            getCachedDrawable()
        } catch (err: ParseError) {
            Log.d("kotlitex", err.msg)
            isError = true
        } catch (err: NotImplementedError) {
            Log.d("kotlitex", err.message)
            isError = true
        }
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
        if (isError) {
            canvas.drawText("ERROR", x, y.toFloat(), paint)
            return
        }

        val b = getCachedDrawable()

        // Log.d("kotlitex", "x=$x, y=$y, top=$top, ratio=$ratio, expr=$expr")

        canvas.save()
        canvas.translate(x, y.toFloat())
        b.drawAllRenderNodes(canvas)

        canvas.restore()
    }

    private fun getDrawable(): MathExpressionDrawable {
        // TODO: drawBounds should be always false. Unlike baseSize, we don't have to expose the flag to end-users.
        val drawable = MathExpressionDrawable(
            expr, baseHeight,
            AndroidFontLoader(assetManager), isMathMode, drawBounds = false
        )
        return drawable
    }

    private fun getCachedDrawable(): MathExpressionDrawable {
        val wr = drawableRef
        val d = wr?.get()
        if (d != null)
            return d
        val newDrawable = getDrawable()
        drawableRef = WeakReference(newDrawable)
        return newDrawable
    }

    private var drawableRef: WeakReference<MathExpressionDrawable>? = null
}
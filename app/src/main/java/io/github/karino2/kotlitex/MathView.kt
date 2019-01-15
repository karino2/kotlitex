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
    /*
    val nodes = list {
        span("mord", children=list {
            sym("x", "mord mathdefault", height=0.43056, skew=0.02778, width=0.57153, maxFontSize=1.0)
            span("msupsub", height=0.8141079999999999, maxFontSize = 0.7,children=list {
                span("vlist-t", height=0.8141079999999999, depth=-0.367, maxFontSize = 0.7, children=list {
                    span("vlist-r", height=0.45110799999999995, maxFontSize = 0.7,  children=list {
                        span(
                            "vlist",
                            height = 0.45110799999999995,
                            sheight = "0.8141079999999999em",
                            maxFontSize = 0.7,
                            children = list {
                                span(
                                    "",
                                    top = "-3.063em",
                                    height = 0.45110799999999995,
                                    italic = 0.05,
                                    maxFontSize = 0.7,
                                    children = list {
                                        span("pstruct", sheight = "2.7em")
                                        span(
                                            "sizing reset-size6 size3 mtight",
                                            height = 0.45110799999999995,
                                            maxFontSize = 0.7,
                                            children = list {
                                                sym(
                                                    "2",
                                                    "mord mtight",
                                                    height = 0.64444,
                                                    width = 0.5,
                                                    maxFontSize = 0.7,
                                                    skew = 0.0
                                                )
                                            })
                                    })
                            })
                    })
                })

            })
        })
    }
    */

    val BaseTextSize: Double = 50.0;

    val textPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.create("serif", Typeface.NORMAL)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (renderNode in nodes) {
            drawNode(canvas, renderNode)
        }
    }

    private fun drawNode(canvas: Canvas, parent: RenderNode) {
        when (parent) {
            is SpanNode -> {
                for (node in parent.children) {
                    drawNode(canvas, node)
                }
            }
            is SymbolNode -> {
                textPaint.textSize = (BaseTextSize * parent.maxFontSize).toFloat()
                canvas.drawText(parent.text, 100.0f, 100.0f, textPaint)
            }
        }
    }
}
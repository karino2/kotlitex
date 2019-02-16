package io.github.karino2.kotlitex

import org.junit.Test

import org.junit.Assert.*

fun toEnum(name: String) = CssClass.valueOf(name.replace("-", "_"))
fun classesToEnums(klassnames: String) = klassnames.split(" ").filter { !it.isEmpty() }.map { toEnum(it) }.toMutableSet()

class SpanBuilder {
    val spans = mutableListOf<RenderNode>()
    fun span(klassnames: String,
             children: MutableList<RenderNode> = mutableListOf(),
             top : String? = null,
             height: Double = 0.0,
             depth: Double = 0.0,
             italic: Double = 0.0,
               maxFontSize: Double = 0.0,
             sheight: String? = null
             ) : RNodeSpan {

        val klasses = classesToEnums(klassnames)
        val style = CssStyle(sheight, top)

        val node = RNodeSpan(children, null,  klasses, height, depth, maxFontSize, style)
        spans.add(node)
        return node
    }

    fun sym(text: String, klassnames: String, height: Double, skew: Double, width: Double, maxFontSize: Double) : RNodeSymbol{
        val klasses = classesToEnums(klassnames)
        val node = RNodeSymbol(text, height=height, skew=skew, width=width, klasses=klasses )
        node.maxFontSize = maxFontSize
        spans.add(node)
        return node
    }
}
fun list(body: SpanBuilder.() -> Unit) : MutableList<RenderNode>{
    val builder = SpanBuilder()
    builder.body()
    return builder.spans
}


class RenderNodeTest {
    @Test
    fun firstTarget() {
        val spans = list {
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

        assertEquals(1, spans.size)
        val target = spans[0]

        assertTrue(target is RNodeSpan)
        val mord = (target as RNodeSpan)

        assertEquals(2, mord.children.size)
        assertTrue(mord.hasClass(CssClass.mord))

        assertTrue(mord.children[0] is RNodeSymbol)

        val symx : RNodeSymbol = mord.children[0] as RNodeSymbol
        assertEquals("x", symx.text)

        /*
        '<span class="mord">
           <span class="mord mathdefault">x</span>
           <span class="msupsub"><span class="vlist-t"><span class="vlist-r"><span class="vlist" style="height:0.8141079999999999em;"><span style="top:-3.063em;margin-right:0.05em;"><span class="pstrut" style="height:2.7em;"></span><span class="sizing reset-size6 size3 mtight"><span class="mord mtight">2</span></span></span></span></span></span></span></span>'
         */
    }
}
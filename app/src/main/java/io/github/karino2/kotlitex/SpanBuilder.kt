package io.github.karino2.kotlitex

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

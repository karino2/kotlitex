package io.github.karino2.kotlitex

enum class CssClass {
    base, mathdefault, mord, mtight, msupsub, vlist, vlist_r, vlist_t, pstruct, reset_size6, sizing,  size3, struct
}

data class CssStyle(
    // It seems always "double + em". may be it better be double
    var height: String? = null,
    var top: String? = null
/*
    backgroundColor: string,
    borderBottomWidth: string,
    borderColor: string,
    borderRightWidth: string,
    borderTopWidth: string,
    bottom: string,
    color: string,
    left: string,
    marginLeft: string,
    marginRight: string,
    marginTop: string,
    minWidth: string,
    paddingLeft: string,
    position: string,
    width: string,
    verticalAlign: string,

 */
)

// Almost the same as HtmlDomNode
sealed class RenderNode (val klasses : MutableSet<CssClass> = mutableSetOf(), var height: Double = 0.0,
                       var depth: Double = 0.0, var maxFontSize: Double = 0.0, var style: CssStyle = CssStyle()) {
    fun hasClass(klass : CssClass) = klasses.contains(klass)
}

class SpanNode(var children: MutableList<RenderNode> = mutableListOf(), var width: Double? = null,
                    klasses : MutableSet<CssClass> = mutableSetOf(), height: Double = 0.0,
                    depth: Double = 0.0, maxFontSize: Double = 0.0, style: CssStyle = CssStyle()) : RenderNode(klasses, height, depth, maxFontSize, style)

class SymbolNode(val text: String,
    val italic: Double = 0.0,
    val skew: Double = 0.0,
    val width: Double = 0.0,
    klasses : MutableSet<CssClass> = mutableSetOf(), height: Double = 0.0,
                 depth: Double = 0.0, style: CssStyle = CssStyle()) : RenderNode(klasses, height, depth, 0.0, style)

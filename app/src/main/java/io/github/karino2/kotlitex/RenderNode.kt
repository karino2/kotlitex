package io.github.karino2.kotlitex

enum class CssClass {
    amsrm, base, delimcenter, delimsizing, delimsizinginner, delim_size1, delim_size4,
    frac_line, mathdefault, mbin, mclose, mfrac, minner, mop, mopen, mord, mpunct, mrel, mtight,
    msupsub, mspace, mult, nulldelimiter,
    vlist, vlist_r, vlist_s, vlist_t, vlist_t2, pstruct, reset_size6, sizing,  size3, struct,
    textbf, textit, textrm,
    EMPTY;

    companion object {
        fun resetClass(size: Int) : CssClass{
            return when(size) {
                6 -> CssClass.reset_size6
                else -> throw Exception("Unknown reset class size: $size")
            }
        }

        fun sizeClass(size: Int) : CssClass {
            return when(size) {
                3 -> CssClass.size3
                else -> throw Exception("Unknown size class size: $size")
            }
        }
    }
}

fun Set<CssClass>.concat(target: Set<CssClass>) : MutableSet<CssClass>{
    val newone = target.toList().toMutableSet() // is this really cloned?
    newone.addAll(this)
    return newone.filter { it != CssClass.EMPTY }.toMutableSet()
}



data class CssStyle(
    // It seems always "double + em". may be it better be double
    var height: String? = null,
    var top: String? = null,

    var color: String? = null,
    var marginLeft: String? = null,
    var marginRight: String? = null,
    var borderBottomWidth: String? = null
/*
    backgroundColor: string,
    borderColor: string,
    borderRightWidth: string,
    borderTopWidth: string,
    bottom: string,
    left: string,
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

class RNodeSpan(var children: MutableList<RenderNode> = mutableListOf(), var width: Double? = null,
                klasses : MutableSet<CssClass> = mutableSetOf(), height: Double = 0.0,
                depth: Double = 0.0, maxFontSize: Double = 0.0, style: CssStyle = CssStyle())
    : RenderNode(klasses, height, depth, maxFontSize, style) {

    constructor(klasses : MutableSet<CssClass> = mutableSetOf(), children: MutableList<RenderNode> = mutableListOf(), options: Options?, style: CssStyle = CssStyle() )
            : this(children, null, klasses, style=style) {
        if(options?.style?.isTight == true) {
            klasses.add(CssClass.mtight)
        }
        if(options?.color != null) {
            this.style.color = options.color
        }

    }
}

class RNodeSymbol(val text: String,
                  val italic: Double = 0.0,
                  val skew: Double = 0.0,
                  val width: Double = 0.0,
                  klasses : MutableSet<CssClass> = mutableSetOf(), height: Double = 0.0,
                  depth: Double = 0.0, style: CssStyle = CssStyle())
    : RenderNode(klasses, height, depth, 0.0, style)

package io.github.karino2.kotlitex

import android.graphics.Path
import io.github.karino2.kotlitex.functions.SvgGeometry

enum class CssClass {
    amsrm, base, delimcenter, delimsizing, delimsizinginner, delim_size1, delim_size4,
    enclosing, frac_line, hide_tail, large_op,
    mathbf, mathdefault, mbin, mclose, mfrac, minner, mop, mopen, mord, mpunct, mrel, mtight,
    msupsub, mspace, mult, nulldelimiter,
    vlist, vlist_r, vlist_s, vlist_t, vlist_t2, pstruct, op_symbol,op_limits,
    reset_size1, reset_size2, reset_size3, reset_size4, reset_size5, reset_size6,
    reset_size7, reset_size8, reset_size9, reset_size10, reset_size11, root,
    sizing,  size1, size2, size3, size4, size5, size6, size7, size8, size9, size10, size11,
    small_op, sqrt, struct, svg_align,
    textbf, textit, textrm,
    EMPTY;

    companion object {
        fun resetClass(size: Int) : CssClass{
            return when(size) {
                1 -> CssClass.reset_size1
                2 -> CssClass.reset_size2
                3 -> CssClass.reset_size3
                4 -> CssClass.reset_size4
                5 -> CssClass.reset_size5
                6 -> CssClass.reset_size6
                7 -> CssClass.reset_size7
                8 -> CssClass.reset_size8
                9 -> CssClass.reset_size9
                10 -> CssClass.reset_size10
                11 -> CssClass.reset_size11
                else -> throw Exception("Unknown reset class size: $size")
            }
        }

        fun sizeClass(size: Int) : CssClass {
            return when(size) {
                1 -> CssClass.size1
                2 -> CssClass.size2
                3 -> CssClass.size3
                4 -> CssClass.size4
                5 -> CssClass.size5
                6 -> CssClass.size6
                7 -> CssClass.size7
                8 -> CssClass.size8
                9 -> CssClass.size9
                10 -> CssClass.size10
                11 -> CssClass.size11
                else -> throw Exception("Unknown size class size: $size")
            }
        }

        fun mFamily(family: Atoms): CssClass {
            return when(family) {
                Atoms.punct -> CssClass.mpunct
                Atoms.bin -> CssClass.mbin
                Atoms.close -> CssClass.mclose
                Atoms.inner -> CssClass.minner
                Atoms.open -> CssClass.mopen
                Atoms.rel -> CssClass.mrel
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
    var borderBottomWidth: String? = null,
    var minWidth: String? = null,
    var paddingLeft: String? = null,
    var position: String? = null
/*
    backgroundColor: string,
    borderColor: string,
    borderRightWidth: string,
    borderTopWidth: string,
    bottom: string,
    left: string,
    marginTop: string,

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

    override fun toString(): String {
        val children = children.map { it.toString() }.joinToString(", ", "[", "]")
        return this.javaClass.simpleName + " { klasses = " + klasses + ", children = " + children + " }"
    }

    // Basically RNodeSpan does not have italic, but in op.js, they put italic secretly
    // I put this as field so that all other doesn't need to care there existence.
    var italic = 0.0
}

// PathNode in js.
class RNodePath(val path: Path) : RenderNode() {
    // TODO: support other pathName
    constructor(pathName: String) : this(SvgGeometry.sqrtMain) {
        if(pathName != "sqrtMain")
            throw NotImplementedError("TODO: RNodePath other than sqrtMain is NYI")
    }
}

data class ViewBox(val minX: Double, val minY: Double, val width: Double, val height: Double)

// SvgNode in js.
class RNodePathHolder(val children: MutableList<RNodePath>, val widthStr: String, val heightStr: String,
                      val viewBox: ViewBox, val preserveAspectRatio: String,
                      val styleStr: String?=null) : RenderNode()

// SvgSpan in js.
// Use RNodeSpan as SvgSpan until we need to separate them.
typealias RNodePathSpan = RNodeSpan


class RNodeSymbol(var text: String,
                  var italic: Double = 0.0,
                  val skew: Double = 0.0,
                  val width: Double = 0.0,
                  klasses : MutableSet<CssClass> = mutableSetOf(), height: Double = 0.0,
                  depth: Double = 0.0, style: CssStyle = CssStyle())
    : RenderNode(klasses, height, depth, 0.0, style) {
    override fun toString(): String {
        val b = StringBuilder()
        b.append("${this.javaClass.simpleName} { text='$text', style=$style")
        if (klasses.isNotEmpty()) {
            b.append(", klasses=$klasses")
        }
        b.append(" }")
        return b.toString()
    }
}

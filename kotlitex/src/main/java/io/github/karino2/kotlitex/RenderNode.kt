package io.github.karino2.kotlitex

import android.graphics.Path
import io.github.karino2.kotlitex.functions.SvgGeometry

// Almost the same as HtmlDomNode
sealed class RenderNode (val klasses : MutableSet<CssClass> = mutableSetOf(), var height: Double = 0.0,
                       var depth: Double = 0.0, var maxFontSize: Double = 0.0, var style: CssStyle = CssStyle()) {
    fun hasClass(klass : CssClass) = klasses.contains(klass)
}

class RNodeSpan(var children: MutableList<RenderNode> = mutableListOf(), var width: Double? = null,
                klasses: MutableSet<CssClass> = mutableSetOf(), height: Double = 0.0,
                depth: Double = 0.0, maxFontSize: Double = 0.0, style: CssStyle = CssStyle())
    : RenderNode(klasses, height, depth, maxFontSize, style) {

    constructor(klasses: MutableSet<CssClass> = mutableSetOf(), children: MutableList<RenderNode> = mutableListOf(), options: Options?, style: CssStyle = CssStyle())
            : this(children, null, klasses, style = style) {
        if (options?.style?.isTight == true) {
            klasses.add(CssClass.mtight)
        }
        if (options?.color != null) {
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
        if (pathName != "sqrtMain")
            throw NotImplementedError("TODO: RNodePath other than sqrtMain is NYI")
    }
}

data class ViewBox(val minX: Double, val minY: Double, val width: Double, val height: Double)

// SvgNode in js.
class RNodePathHolder(val children: MutableList<RNodePath>, val widthStr: String, val heightStr: String,
                      val viewBox: ViewBox, val preserveAspectRatio: String,
                      val styleStr: String? = null) : RenderNode()

// SvgSpan in js.
// Use RNodeSpan as SvgSpan until we need to separate them.
typealias RNodePathSpan = RNodeSpan

class RNodeSymbol(var text: String,
                  var italic: Double = 0.0,
                  val skew: Double = 0.0,
                  val width: Double = 0.0,
                  klasses: MutableSet<CssClass> = mutableSetOf(), height: Double = 0.0,
                  depth: Double = 0.0,
                  style: CssStyle = CssStyle()) : RenderNode(klasses, height, depth, 0.0, style) {
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

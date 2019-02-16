package io.github.karino2.kotlitex.renderer.node

import android.graphics.Paint
import io.github.karino2.kotlitex.RNodePathHolder

enum class Alignment {
    NONE /* same as null on canvas-latex */, LEFT, RIGHT, CENTER
}

/**
 * A Virtual Node represents the interface for a all sub Nodes.
 * They are simple models, which allow the data to be transplated to most
 * rendering platforms.
 */
sealed class VirtualCanvasNode(var klasses: Set<String>) {
    var margin = Margin(0.0, 0.0)
    open val bounds = Bounds(0.0, 0.0)

    open fun setPosition(x: Double, y: Double) {
        bounds.x = x
        bounds.y = y
    }

    open fun updateSize() {
    }

    override fun toString(): String {
        val b = StringBuilder()
        b.append("${this.javaClass.simpleName} { bounds=$bounds, margin=$margin")
        if (klasses.isNotEmpty()) {
            b.append(", klasses=$klasses")
        }
        b.append(" }")
        return b.toString()
    }
}

/**
 * A ContainerNode represents the container of child nodes
 * When this position is updated, so are the children's position.
 */
abstract class VirtualContainerNode<T : VirtualCanvasNode>(klasses: Set<String>) : VirtualCanvasNode(klasses) {
    val nodes = mutableListOf<T>()

    fun addNode(node: T) {
        nodes.add(node)
    }

    fun last(): T? {
        if (nodes.size == 0)
            return null
        else
            return nodes.last()
    }

    override fun setPosition(x: Double, y: Double) {
        super.setPosition(x, y)
        val delta = x - bounds.x
        for (child in nodes) {
            val newX = child.bounds.x + delta
            child.setPosition(newX, child.bounds.y)
        }
    }

    override fun toString(): String {
        val nodes = this.nodes.map { it.toString() }.joinToString(", ", "[", "]")
        return this.javaClass.simpleName + " { nodes = " + nodes + ", klasses = " + this.klasses + " }"
    }

    override val bounds: Bounds
        get() {
            var bounds = super.bounds
            nodes.withIndex().forEach {
                if (it.index == 0) {
                    bounds = it.value.bounds.copy()
                } else {
                    bounds.extend(it.value.bounds)
                }
            }
            return bounds
        }
}

class VerticalListRow(klasses: Set<String>) : VirtualContainerNode<VirtualCanvasNode>(klasses) {
    var strutBounds: Bounds? = null

    fun addBaseStrut(padNode: VirtualCanvasNode) {
        if (strutBounds == null) {
            strutBounds = padNode.bounds.copy()
        } else {
            strutBounds?.extend(padNode.bounds)
        }
    }

    fun setPositionX(x: Double) = setPosition(x, bounds.y)

    fun leftAlign(tableLeft: Double) = setPositionX(tableLeft)

    fun centerAlign(tableCenter: Double) {
        val width = bounds.width
        val center = tableCenter - width / 2
        setPositionX(center)
    }
    fun rightAlign(tableRight: Double) {
        val width = bounds.width
        val right = tableRight - width
        setPositionX(right)
    }
}

abstract class StretchyNode(val minWidth: Double, klasses: Set<String>) : VirtualCanvasNode(klasses) {
    fun setListWidth(width: Double) {
        bounds.width = width + this.minWidth
    }
}

/**
 * The VerticalList class represents a 1D array of VerticalListRow's
 * which can be horizontally aligned left, right, or center.
 */
class VerticalList(var alignment: Alignment, var rowStart: Double, klasses: Set<String>) : VirtualContainerNode<VerticalListRow>(klasses) {
    /**
     * Return the x coordinate of the next node to be placed into the List
     */
    fun getNextNodePlacement(): Double {
        val lastRow = this.last() ?: return this.rowStart + this.margin.left
        val lastNode = lastRow.last() ?: return this.rowStart + lastRow.margin.left

        val bounds = lastNode.bounds
        return bounds.x + bounds.width + lastNode.margin.right
    }

    /**
     * Sets the width of the stretchy nodes contained within.
     */
    fun setStretchWidths() {
        val width = bounds.width
        nodes.forEach { rowNode ->
            rowNode.nodes.forEach { node ->
                if (node is StretchyNode) {
                    node.setListWidth(width)
                }
            }
        }
    }

    /**
     * Aligns the List based on the specified alignment
     */
    fun align() {
        when (this.alignment) {
            Alignment.LEFT -> leftAlign()
            Alignment.CENTER -> centerAlign()
            Alignment.RIGHT -> rightAlign()
        }
    }

    /**
     * Adds a row to the List.
     */
    fun addRow(row: VerticalListRow) {
        addNode(row)
    }

    /**
     * Adds a VirtualCanvasNode to current row
     */
    fun addCell(node: VirtualCanvasNode) {
        this.last()!!.addNode(node)
    }

    val strutBounds: Bounds
        get() {
            val result = this.bounds
            nodes.forEach { row ->
                val b = row.strutBounds
                if (b != null) {
                    result.extend(b)
                }
            }
            return result
        }

    fun centerAlign() {
        val center = bounds.x + bounds.width / 2
        nodes.forEach { it.centerAlign(center) }
    }

    fun rightAlign() {
        val right = bounds.x + bounds.width
        nodes.forEach { it.rightAlign(right) }
    }

    fun leftAlign() {
        val left = bounds.x
        nodes.forEach { it.leftAlign(left) }
    }
}

class TextNode(
    val text: String,
    val font: CssFont,
    val color: String,
    klasses: Set<String>
) : VirtualCanvasNode(klasses) {
    override fun updateSize() {
        val paint = Paint()
        paint.typeface = font.getTypeface()
        paint.textSize = font.size.toFloat()
        bounds.width = paint.measureText(text).toDouble()
    }

    override fun toString(): String {
        val b = StringBuilder()
        b.append("${this.javaClass.simpleName} { text=$text")
        b.append(", bounds=$bounds")
        b.append(" }")
        return b.toString()
    }
}

/**
 * An HPaddingNode represents an invisible node (not drawn) with a specific width/x.
 */
class HPaddingNode(klasses: Set<String>) : VirtualCanvasNode(klasses)

class HorizontalLineNode(val color: String, minWidth: Double, klasses: Set<String>) : StretchyNode(minWidth, klasses)

// SvgNode in canvas-latex. Note that there is the same class-name in katex (but this class is counter-part of canvas-latex, not katex).
class PathNode(val rnode: RNodePathHolder, minWidth: Double, klasses: Set<String>) : StretchyNode(minWidth, klasses)
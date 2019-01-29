package io.github.karino2.kotlitex.renderer.node

import android.graphics.Paint

enum class Alignment {
    LEFT, RIGHT, CENTER
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

class VerticalListRow(klasses: Set<String>) : VirtualContainerNode<VirtualCanvasNode>(klasses)

/**
 * The VerticalList class represents a 1D array of VerticalListRow's
 * which can be horizontally aliged left, right, or center.
 */
class VerticalList(var alignment: Alignment, var rowStart: Double, klasses: Set<String>) : VirtualContainerNode<VerticalListRow>(klasses) {
    val structBounds = this.bounds.copy()

    /**
     * Return the x coordinate of the next node to be placed into the List
     */
    fun getNextNodePlacement(): Double {
        val lastRow = this.last() ?: return this.rowStart + this.margin.left
        val lastNode = lastRow.last() ?: return this.rowStart + lastRow.margin.left

        val bounds = lastNode.bounds
        return bounds.x + bounds.width + lastNode.margin.right
    }

    fun setStretchWidths() {
    }

    fun align() {
        when (this.alignment) {
            Alignment.LEFT -> leftAlign()
            Alignment.CENTER -> centerAlign()
            Alignment.RIGHT -> rightAlign()
        }
    }

    fun addRow(row: VerticalListRow) {
        addNode(row)
    }

    fun addCell(node: VirtualCanvasNode) {
        this.last()?.addNode(node)
    }

    fun leftAlign() {
    }

    fun centerAlign() {
    }

    fun rightAlign() {
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
        return this.javaClass.simpleName + " { text = " + text + ", klasses = " + this.klasses + " }"
    }
}
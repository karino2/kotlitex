package io.github.karino2.kotlitex.renderer.node

import android.graphics.Paint
import android.graphics.Typeface

/**
 * A Virtual Node represents the interface for a all sub Nodes.
 * They are simple models, which allow the data to be transplated to most
 * rendering platforms.
 */
sealed class VirtualCanvasNode(var klasses: Set<String>) {
    var margin = Margin(0.0, 0.0);
    open val bounds = Bounds(0.0, 0.0)

    open fun setPosition(x: Double, y: Double) {
        bounds.x = x
        bounds.y = y
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

    override val bounds: Bounds
        get() {
            var first = true
            var b: Bounds = super.bounds

            for (child in nodes) {
                if (first) {
                    b = child.bounds.copy()
                    first = false
                } else {
                    b.extend(child.bounds)
                }
            }
            return b
        }
}

class VerticalListRow(klasses: Set<String>) : VirtualContainerNode<VirtualCanvasNode>(klasses) {
}


/**
 * The VerticalList class represents a 1D array of VerticalListRow's
 * which can be horizontally aliged left, right, or center.
 */
class VerticalList(var alignment: String, var rowStart: Double, klasses: Set<String>) : VirtualContainerNode<VerticalListRow>(klasses) {
    val structBounds = this.bounds.copy()

    /**
     * Return the x coordinate of the next node to be placed into the List
     */
    fun getNextNodePlacement(): Double {
        val lastRow = this.last()
        if (lastRow != null) {
            val lastNode = lastRow.last()
            if (lastNode != null) {
                val b = lastNode.bounds
                return b.x + b.width + lastNode.margin.right
            } else {
                return this.rowStart + lastRow.margin.left
            }
        } else {
            return this.rowStart + this.margin.left
        }
    }

    fun setStretchWidths() {
    }

    fun align() {
        when (this.alignment) {
            "left" -> leftAlign()
            "center" -> centerAlign()
            "right" -> rightAlign()
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

class TextNode(val text: String,
               val typeface: Typeface,
               val textSize: Double,
               val color: String,
               klasses: Set<String>) : VirtualCanvasNode(klasses) {
    init {
        val paint = Paint()
        paint.typeface = typeface
        paint.textSize = textSize.toFloat()
        bounds.width = paint.measureText(text).toDouble()
    }
}
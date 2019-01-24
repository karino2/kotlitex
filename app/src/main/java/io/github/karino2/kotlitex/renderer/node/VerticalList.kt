package io.github.karino2.kotlitex.renderer.node

/**
 * The VerticalList class represents a 1D array of VerticalListRow's
 * which can be horizontally aliged left, right, or center.
 */
public class VerticalList(var alignment: String, var rowStart: Double, klasses: Set<String>) : VirtualContainerNode<VerticalListRow>(klasses) {
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
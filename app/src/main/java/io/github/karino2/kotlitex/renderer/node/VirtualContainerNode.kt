package io.github.karino2.kotlitex.renderer.node

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
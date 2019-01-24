package io.github.karino2.kotlitex.renderer.node

import io.github.karino2.kotlitex.renderer.node.Bounds
import io.github.karino2.kotlitex.renderer.node.Margin

/**
 * A Virtual Node represents the interface for a all sub Nodes.
 * They are simple models, which allow the data to be transplated to most
 * rendering platforms.
 */
open class VirtualCanvasNode(var klasses: Set<String>) {
    var margin = Margin(0.0, 0.0);
    open val bounds = Bounds(0.0, 0.0)

    open fun setPosition(x: Double, y: Double) {
        bounds.x = x
        bounds.y = y
    }
}
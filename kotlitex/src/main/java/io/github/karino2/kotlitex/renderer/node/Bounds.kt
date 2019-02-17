package io.github.karino2.kotlitex.renderer.node

data class Bounds(var x: Double, var y: Double, var width: Double = 0.0, var height: Double = 0.0) {
    fun extend(bounds: Bounds) {
        if (bounds.x < this.x) {
            this.width += this.x - bounds.x
            this.x = bounds.x
        }
        if (bounds.y < this.y) {
            this.height += this.y - bounds.y
            this.y = bounds.y
        }
        if (bounds.x + bounds.width > this.x + this.width) {
            this.width = (bounds.x + bounds.width) - this.x
        }
        if (bounds.y + bounds.height > this.y + this.height) {
            this.height = (bounds.y + bounds.height) - this.y
        }
    }
}
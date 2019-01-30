package io.github.karino2.kotlitex.renderer.node

import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.RenderingState

object StyleStateMapping {
    fun createState(state: RenderingState, node: RenderNode): RenderingState {
        val top = node.style.top?.replace("em", "")?.toDouble() ?: return state
        println(state.fontSize())
        println(state.size)
        println(top)
        return state.copy(y = state.y + (top * state.fontSize()))
    }
}
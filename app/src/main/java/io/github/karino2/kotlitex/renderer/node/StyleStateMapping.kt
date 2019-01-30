package io.github.karino2.kotlitex.renderer.node

import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.RenderingState

object StyleStateMapping {
    fun createState(original: RenderingState, node: RenderNode): RenderingState {
        var state = original.copy()

        val color = node.style.color
        if (color != null) {
            state = state.copy(color = color)
        }

        val top = node.style.top
        if (top != null) {
            state = state.copy(y = state.y + parseEm(top, state.fontSize()))
        }

        val paddingLeft = node.style.paddingLeft
        if (paddingLeft != null) {
            val spacingLeft = parseEm(paddingLeft, state.fontSize())
            val padLeftNode = HPaddingNode(state.klasses)
            padLeftNode.setPosition(state.nextX(), state.y)
            padLeftNode.bounds.width = spacingLeft
            state.vlist.addCell(padLeftNode)
            state = state.withResetMargin()
        }

        val marginLeft = node.style.marginLeft
        if (marginLeft != null) {
            state = state.copy(marginLeft = parseEm(marginLeft, state.fontSize()))
        }

        val marginRight = node.style.marginRight
        if (marginRight != null) {
            state = state.copy(marginRight = parseEm(marginRight, state.fontSize()))
        }

        val minWidth = node.style.minWidth
        if (minWidth != null) {
            state = state.copy(minWidth = parseEm(minWidth, state.fontSize()))
        }

        return state
    }

    private fun parseEm(str: String, fontSize: Double): Double {
        return str.replace("em", "").toDouble() * fontSize
    }
}
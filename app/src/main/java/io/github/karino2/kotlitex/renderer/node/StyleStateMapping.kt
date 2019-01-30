package io.github.karino2.kotlitex.renderer.node

import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.RenderingState

object StyleStateMapping {
    fun createState(original: RenderingState, node: RenderNode): RenderingState {
        var state = original.copy()

        node.style.color?.let {
            state = state.copy(color = it)
        }

        node.style.top?.let {
            state = state.copy(y = state.y + state.parseEm(it))
        }

        node.style.paddingLeft?.let {
            val spacingLeft = state.parseEm(it)
            val padLeftNode = HPaddingNode(state.klasses)
            padLeftNode.setPosition(state.nextX(), state.y)
            padLeftNode.bounds.width = spacingLeft
            state.vlist.addCell(padLeftNode)
            state = state.withResetMargin()
        }

        node.style.marginLeft?.let {
            state = state.copy(marginLeft = state.parseEm(it))
        }

        node.style.marginRight?.let {
            state = state.copy(marginRight = state.parseEm(it))
        }

        node.style.minWidth?.let {
            state = state.copy(minWidth = state.parseEm(it))
        }

        return state
    }
}
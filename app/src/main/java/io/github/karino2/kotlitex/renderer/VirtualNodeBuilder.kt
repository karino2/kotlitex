package io.github.karino2.kotlitex.renderer

import android.graphics.Typeface
import io.github.karino2.kotlitex.RNodeSpan
import io.github.karino2.kotlitex.RNodeSymbol
import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.node.TextNode
import io.github.karino2.kotlitex.renderer.node.VerticalList
import io.github.karino2.kotlitex.renderer.node.VerticalListRow

class VirtualNodeBuilder(val children: List<RenderNode>) {
    var state: RenderingState = RenderingState()

    fun build(): VerticalList {
        val row = VerticalListRow(emptySet())
        state.vlist.addRow(row)
        createRenderingState(children)
        val rootNode = state.vlist
        rootNode.align()
        return rootNode
    }

    private fun getGlyphDataFromNode(node: RenderNode) {
        createTextNode(node)
    }

    private fun createTextNode(node: RenderNode) {
        if (node is RNodeSymbol) {
            if (node.text.length > 0) {
                val s = this.state
                val textNode = TextNode(node.text, Typeface.SERIF, state.fontSize(), state.color, state.klasses)
                textNode.setPosition(s.nextX(), s.y)
                textNode.margin.left = s.marginLeft
                textNode.margin.right = s.marginRight
                state.vlist.addCell(textNode)
                state = state.withResetMargin()
            }
        }
    }

    /**
     * Keep its state while executing `body`. It replaces canvas-latex's resetState()
     */
    private fun withSaveState(body: () -> Unit) {
        val original = state
        body()
        state = original
    }

    private fun createRenderingState(children: List<RenderNode>) {
        withSaveState {
            children.forEach { createRenderingState(it) }
        }
    }

    private fun createRenderingState(node: RenderNode) {
        withSaveState {
            getGlyphDataFromNode(node)
            when (node) {
                is RNodeSpan -> {
                    node.children.forEach { createRenderingState(it)}
                }
            }
        }
    }
}
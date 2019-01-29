package io.github.karino2.kotlitex.renderer

import io.github.karino2.kotlitex.CssClass
import io.github.karino2.kotlitex.RNodeSpan
import io.github.karino2.kotlitex.RNodeSymbol
import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.node.Alignment
import io.github.karino2.kotlitex.renderer.node.CssFont
import io.github.karino2.kotlitex.renderer.node.CssFontFamily
import io.github.karino2.kotlitex.renderer.node.TextNode
import io.github.karino2.kotlitex.renderer.node.VerticalList
import io.github.karino2.kotlitex.renderer.node.VerticalListRow

class VirtualNodeBuilder(val children: List<RenderNode>, val headless: Boolean = false) {
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
        extractClassDataFromNode(node)
        extractStyleDataFromNode(node)
        createMSpace()
        createSvgNode(node)
        createTextNode(node)
        createItalicNode(node)
    }

    private fun extractClassDataFromNode(node: RenderNode) {
        node.klasses.forEach {
            when (it) {
                CssClass.size3 -> state = state.copy(size = 3)
                CssClass.vlist -> {
                    val vlist = VerticalList(Alignment.CENTER, state.nextX(), state.klasses)
                    state = state.copy(vlist = vlist)
                }
                CssClass.pstruct -> {
                    val height = 0.0 // ((+node.style.height.replace('em', '')) * state.em)
                    val tableRow = VerticalListRow(state.klasses)
                    state.vlist.addRow(tableRow)
                    tableRow.setPosition(state.nextX(), state.y + height)
                    tableRow.bounds.height = height
                    tableRow.margin.left = state.marginLeft
                    tableRow.margin.right = state.marginRight
                    // return state.withPstrut(height)
                }
                else -> {}
            }
        }
    }

    private fun extractStyleDataFromNode(node: RenderNode) {}

    private fun createMSpace() {}

    private fun createSvgNode(node: RenderNode) {}

    private fun createTextNode(node: RenderNode) {
        if (node is RNodeSymbol) {
            if (node.text.length > 0) {
                val s = this.state
                val textNode = TextNode(node.text, CssFont(CssFontFamily.SERIF, state.fontSize()), state.color, state.klasses)

                // Maybe subclass or type-parameter?
                if (! headless) {
                    textNode.updateSize()
                }

                textNode.setPosition(s.nextX(), s.y)
                textNode.margin.left = s.marginLeft
                textNode.margin.right = s.marginRight
                state.vlist.addCell(textNode)
                state = state.withResetMargin()
            }
        }
    }

    private fun createItalicNode(node: RenderNode) {}

    /**
     * Keep its state while executing `body`. It replaces canvas-latex's resetState()
     */
    private fun withSaveState(body: () -> Unit) {
        val original = state
        body()
        resetState(original)
    }

    private fun resetState(parentState: RenderingState) {
        val vlist = state.vlist
        val parentVlist = parentState.vlist
        if (vlist != parentVlist) {
            vlist.setStretchWidths()
            vlist.align()
            parentVlist.addCell(vlist)
        }
        if (state.klasses != parentState.klasses) {
            // TODO
        }
        if (state.pstruct.isNotBlank()) {
            // TODO
        }
        state = parentState
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
                    node.children.forEach { createRenderingState(it) }
                }
            }
        }
    }
}
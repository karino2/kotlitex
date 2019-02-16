package io.github.karino2.kotlitex.renderer

import io.github.karino2.kotlitex.CssClass
import io.github.karino2.kotlitex.RNodePathHolder
import io.github.karino2.kotlitex.RNodeSpan
import io.github.karino2.kotlitex.RNodeSymbol
import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.node.ClassStateMapping
import io.github.karino2.kotlitex.renderer.node.CssFont
import io.github.karino2.kotlitex.renderer.node.CssFontFamily
import io.github.karino2.kotlitex.renderer.node.HPaddingNode
import io.github.karino2.kotlitex.renderer.node.PathNode
import io.github.karino2.kotlitex.renderer.node.StyleStateMapping
import io.github.karino2.kotlitex.renderer.node.TextNode
import io.github.karino2.kotlitex.renderer.node.VerticalList
import io.github.karino2.kotlitex.renderer.node.VerticalListRow

class VirtualNodeBuilder(val children: List<RenderNode>, baseSize: Double, val headless: Boolean = false) {
    var state: RenderingState = RenderingState().copy(baseSize = baseSize)

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
        var nextClassIsLatexClass = false
        node.klasses.forEach {
            if (it == CssClass.enclosing) {
                nextClassIsLatexClass = true
            } else if (nextClassIsLatexClass) {
                nextClassIsLatexClass = false
                // TODO
            } else {
                state = ClassStateMapping.createState(it, state, node)
            }
        }
    }

    private fun extractStyleDataFromNode(node: RenderNode) {
        state = StyleStateMapping.createState(state, node)
    }

    private fun createMSpace() {
        if (state.mspace > 0.0) {
            val mspace = HPaddingNode(state.klasses)
            mspace.setPosition(state.nextX(), state.y)
            mspace.bounds.width = state.mspace * state.fontSize()
            state.vlist.addCell(mspace)
            state = state.withResetMargin()
        }
    }

    private fun createSvgNode(node: RenderNode) {
        if (node is RNodePathHolder) {
            val height = state.parseEm(node.heightStr)
            node.height = height
            // TODO: support fill.
            // node.attributes.fill = this._state.color
            val pathNode = PathNode(node, state.minWidth, state.klasses)
            pathNode.setPosition(state.nextX(), state.y)
            pathNode.bounds.height = height
            pathNode.margin.left = state.marginLeft
            pathNode.margin.right = state.marginRight
            state.vlist.addCell(pathNode)
            state = state.withResetMargin()
        }
    }

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
        if (state.pstrut != 0.0) {
            state = parentState.copy(y = parentState.y + state.pstrut)
        } else {
            state = parentState
        }
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
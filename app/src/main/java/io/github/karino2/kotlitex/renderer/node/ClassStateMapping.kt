package io.github.karino2.kotlitex.renderer.node

import io.github.karino2.kotlitex.CssClass
import io.github.karino2.kotlitex.RNodeSpan
import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.RenderingState

object ClassStateMapping {
    fun createState(klass: CssClass, state: RenderingState, node: RenderNode): RenderingState {
        return when (klass) {
            CssClass.size3 -> state.copy(size = 3)
            CssClass.vlist -> {
                if (! isTrueVlist(node)) {
                    return state
                }
                val vlist = VerticalList(state.textAlign, state.nextX(), state.klasses)
                vlist.setPosition(state.nextX(), state.y)
                vlist.margin.left = state.marginLeft
                vlist.margin.right = state.marginRight
                state.copy(vlist = vlist).withResetMargin()
            }
            CssClass.pstruct -> {
                val height = node.style.height!!.replace("em", "").toDouble() * state.em
                val tableRow = VerticalListRow(state.klasses)
                state.vlist.addRow(tableRow)
                tableRow.setPosition(state.nextX(), state.y + height)
                tableRow.bounds.height = height
                tableRow.margin.left = state.marginLeft
                tableRow.margin.right = state.marginRight
                state.copy(pstrut = height)
            }
            CssClass.frac_line -> {
                val lineHeight = state.parseEm(node.style.borderBottomWidth!!)
                val lineNode = HorizontalLineNode(state.color, state.minWidth, state.klasses)
                lineNode.setPosition(state.nextX(), state.y)
                lineNode.bounds.height = lineHeight
                lineNode.margin.left = state.marginLeft
                lineNode.margin.right = state.marginRight
                state.vlist.addCell(lineNode)
                return state.withResetMargin()
            }
            else -> state
        }
    }

    private fun isTrueVlist(node: RenderNode): Boolean {
        if (node is RNodeSpan) {
            val firstChild = node.children[0]
            if (firstChild is RNodeSpan) {
                return firstChild.children.size > 0
            }
        }
        return false
    }
}
package io.github.karino2.kotlitex.renderer.node

import io.github.karino2.kotlitex.CssClass
import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.RenderingState

object ClassStateMapping {
    fun createState(klass: CssClass, state: RenderingState, node: RenderNode): RenderingState {
        return when (klass) {
            CssClass.size3 -> state.copy(size = 3)
            CssClass.vlist -> {
                val vlist = VerticalList(Alignment.CENTER, state.nextX(), state.klasses)
                state.copy(vlist = vlist)
            }
            CssClass.pstruct -> {
                val height = node.style.height!!.replace("em", "").toDouble() * state.em
                val tableRow = VerticalListRow(state.klasses)
                state.vlist.addRow(tableRow)
                tableRow.setPosition(state.nextX(), state.y + height)
                tableRow.bounds.height = height
                tableRow.margin.left = state.marginLeft
                tableRow.margin.right = state.marginRight
                state.copy(pstruct = height)
            }
            else -> state
        }
    }
}
package io.github.karino2.kotlitex.renderer.node

import io.github.karino2.kotlitex.CssClass
import io.github.karino2.kotlitex.RNodeSpan
import io.github.karino2.kotlitex.RenderNode
import io.github.karino2.kotlitex.renderer.RenderingState
import kotlin.math.max

object ClassStateMapping {
    fun createState(klass: CssClass, state: RenderingState, node: RenderNode): RenderingState {
        return when (klass) {
            CssClass.size1 -> state.copy(size = 1)
            CssClass.size2 -> state.copy(size = 2)
            CssClass.size3 -> state.copy(size = 3)
            CssClass.size4 -> state.copy(size = 4)
            CssClass.size5 -> state.copy(size = 5)
            CssClass.size6 -> state.copy(size = 6)
            CssClass.size7 -> state.copy(size = 7)
            CssClass.size8 -> state.copy(size = 8)
            CssClass.size9 -> state.copy(size = 9)
            CssClass.size10 -> state.copy(size = 10)
            CssClass.size11 -> state.copy(size = 11)

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
            CssClass.pstrut -> {
                val height = state.parseEm(node.style.height!!)
                val tableRow = VerticalListRow(state.klasses)
                state.vlist.addRow(tableRow)
                tableRow.setPosition(state.nextX(), state.y + height)
                tableRow.bounds.height = height
                tableRow.margin.left = state.marginLeft
                tableRow.margin.right = state.marginRight
                state.copy(pstrut = height)
            }
            CssClass.base -> {
                val height = node.height * state.fontSize()
                val strut = HPaddingNode(state.klasses)
                val depth = node.depth * state.fontSize()
                strut.setPosition(state.nextX(), state.y - height)
                strut.bounds.height = height+depth
                val lastRow = state.vlist.last()!!
                // What's depth?
                // lastRow.depth = depth
                lastRow.addBaseStrut(strut)
                state
            }
            CssClass.newline -> {
                val tableRow= VerticalListRow(state.klasses)
                val strutBounds = state.vlist.last()!!.strutBounds!!
                val marginTop = node.style.marginTop
                val topPadding = marginTop?.let { state.parseEm(marginTop) }  ?: 0.0
                state.vlist.addRow(tableRow)
                tableRow.setPosition(state.nextX(), state.y)
                val lineHeight = state.fontSize() * 1.2
                val strutHeight = strutBounds.height
                val yOffset = max(lineHeight, strutHeight)
                state.copy(pstrut = yOffset + topPadding)
            }
            CssClass.underline_line -> {
                withHorizLine(state, node)
            }
            CssClass.frac_line -> {
                withHorizLine(state, node)
            }
            CssClass.accent -> {
                state.copy(textAlign = Alignment.CENTER)
            }
            CssClass.op_limits -> {
                state.copy(textAlign = Alignment.CENTER)
            }
            CssClass.mfrac -> {
                state.copy(textAlign = Alignment.CENTER)
            }
            CssClass.mspace -> {
                node.style.marginRight?.let {
                    val mspace = state.parseEm(it)
                    return state.copy(mspace = mspace)
                }
                return state
            }
            CssClass.mathbb -> {
                state.copy(weight = "normal", variant = "normal", family = "KaTeX_AMS")
            }
            CssClass.mathcal -> {
                state.copy(weight = "normal", variant = "normal", family = "KaTeX_Caligraphic")
            }
            CssClass.mathdefault -> {
                // TODO: temp implementation.
                state.copy(family = "KaTeX_Math", variant = "italic")
            }
            CssClass.mathscr -> {
                state.copy(weight = "normal", variant = "normal", family = "KaTeX_Script")
            }
            CssClass.boldsymbol -> {
                state.copy(weight = "bold", variant = "italic", family = "KaTeX_Math")
            }
            CssClass.large_op -> {
                state.copy(weight = "normal", variant = "normal", family = "KaTeX_Size2")
            }
            else -> state
        }
    }

    private fun withHorizLine(
        state: RenderingState,
        node: RenderNode
    ): RenderingState {
        val lineHeight = state.parseEm(node.style.borderBottomWidth!!)
        val lineNode = HorizontalLineNode(state.color, state.minWidth, state.klasses)
        lineNode.setPosition(state.nextX(), state.y)
        lineNode.bounds.height = lineHeight
        lineNode.margin.left = state.marginLeft
        lineNode.margin.right = state.marginRight
        state.vlist.addCell(lineNode)
        return state.withResetMargin()
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
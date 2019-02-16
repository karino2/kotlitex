package io.github.karino2.kotlitex.renderer

import io.github.karino2.kotlitex.renderer.node.Alignment
import io.github.karino2.kotlitex.renderer.node.VerticalList

/**
 * This class keeps intermediate state while constructing VirtualNode instances.
 * Unlike canvas-latex, this class doesn't have a lot of withXxx methods,
 * since Kotlin has handy copy() with named parameters.
 */
data class RenderingState(
    val y: Double,
    val baseSize: Double,
    val size: Int,

    val family: String,
    val variant: String,
    val weight: String,

    val vlist: VerticalList,
    val textAlign: Alignment,
    val minWidth: Double,
    val marginRight: Double,
    val marginLeft: Double,
    val delimSizing: Boolean,
    val klasses: Set<String>,
    val mspace: Double,

    /**
     * pstrut is used as a padding, apparently
     */
    val pstrut: Double,

    val color: String
) {
    private val SIZES = listOf(0.0, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.2, 1.44, 1.728, 2.074, 2.488)

    constructor() : this(
        y = 0.0,
        baseSize = 44.0,
        size = 6,
        family = "KaTeX_Main",
        variant = "normal",
        weight = "normal",
        vlist = VerticalList(Alignment.CENTER, 0.0, emptySet()),
        textAlign = Alignment.NONE,
        minWidth = 0.0,
        marginRight = 0.0,
        marginLeft = 0.0,
        delimSizing = false,
        klasses = emptySet(),
        mspace = 0.0,
        pstrut = 0.0,
        color = "black"
    )

    fun withResetMargin(): RenderingState {
        return this.copy(marginLeft = 0.0, marginRight = 0.0)
    }

    fun nextX(): Double {
        return this.vlist.getNextNodePlacement() + this.marginLeft
    }

    fun fontSize(): Double {
        return baseSize * SIZES[this.size]
    }

    fun parseEm(str: String): Double {
        return str.replace("em", "").toDouble() * fontSize()
    }
}
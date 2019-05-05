package io.github.karino2.kotlitex.functions

import io.github.karino2.kotlitex.*
import java.lang.IllegalArgumentException

object FunctionSymbolsSpacing {

    // A map of CSS-based spacing functions to their CSS class.
    val cssSpace = mapOf(
        "\\nobreak" to CssClass.nobreak,
        "\\allowbreak" to CssClass.allowbreak
    )

    // A lookup table to determine whether a spacing function/symbol should be
    // treated like a regular space character.  If a symbol or command is a key
    // in this table, then it should be a regular space character.  Furthermore,
    // the associated value may have a `className` specifying an extra CSS class
    // to add to the created `span`.
    val regularSpace = mapOf(
        " " to  CssClass.EMPTY,
        "\\ " to CssClass.EMPTY,
        "~" to CssClass.nobreak,
        "\\space" to CssClass.EMPTY,
        "\\nobreakspace" to CssClass.nobreak
    )

    // (ParseNode, Options)->RenderNode
    fun renderNodeHandler(group: ParseNode, options: Options) : RenderNode {
        if(group !is PNodeSpacingOrd)
            throw IllegalArgumentException("unexpected type in spacing RNodeBuilder.")

        regularSpace[group.text]?.let { klass->
            if(group.mode == Mode.TEXT) {
                val ord = RenderTreeBuilder.makeOrd(group, options, "textord")
                if(CssClass.EMPTY != klass)
                    ord.klasses.add(klass)
                return ord
            } else {
                val klasses = if(CssClass.EMPTY != klass) mutableSetOf(CssClass.mspace, klass) else mutableSetOf(CssClass.mspace)
                return RenderTreeBuilder.makeSpan(klasses,
                    mutableListOf(RenderTreeBuilder.mathsym(group.text, group.mode, options, mutableSetOf())),
                    options)
            }
        }
        val cssSpaceClass = cssSpace[group.text] ?: throw ParseError("Unknown type of space ${group.text}", null)
        // Spaces based on just a CSS class.
        return RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.mspace, cssSpaceClass), mutableListOf(), options)
    }

    fun defineAll() {
        LatexFunctions.defineFunctionBuilder(
            "spacing",
            FunctionSymbolsSpacing::renderNodeHandler
        )
    }
}
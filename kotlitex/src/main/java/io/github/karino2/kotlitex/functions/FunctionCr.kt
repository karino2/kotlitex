package io.github.karino2.kotlitex.functions

import io.github.karino2.kotlitex.*
import java.lang.IllegalArgumentException

object FunctionCr {
    fun defineAll() {
        LatexFunctions.defineFunction(
            FunctionSpec(
                "cr",
                0,
                numOptionalArgs = 1,
                argTypes = listOf(ArgSize),
                allowedInText = true
            ),
            listOf("\\cr", "\\newline"),
            { context: FunctionContext, _ /* args */: List<ParseNode>, optArgs: List<ParseNode?> ->
                val parser = context.parser
                val funcName = context.funcName


                val size = optArgs[0]
                val newRow = (funcName == "\\cr")
                val newLine =
                    if (newRow)
                        false
                    else {
                        !(parser.settings.displayMode &&
                                parser.settings.useStrictBehavior(
                                    "newLineInDisplayMode", "In LaTeX, \\\\ or \\newline " +
                                            "does nothing in display mode", null
                                ))
                    }

                //  size = size && assertNodeType(size, "size").value,
                val sizeArg = size?.let {
                    if (it is PNodeSize) {
                        it.value
                    } else
                        null
                }

                PNodeCr(
                    parser.mode,
                    null,
                    newRow,
                    newLine,
                    sizeArg
                )

            },
            // The following builders are called only at the top level,
            // not within tabular/array environments.
            { group: ParseNode, options: Options ->
                if (group !is PNodeCr) {
                    throw IllegalArgumentException("unexpected type in cr RNodeBuilder.")
                }
                if (group.newRow) {
                    throw ParseError(
                        "\\cr valid only within a tabular/array environment", null
                    )
                }
                val span =
                    RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.mspace), options = options)
                if (group.newLine) {
                    span.klasses.add(CssClass.newline)
                    group.size?.let {
                        val top = RenderTreeBuilder.calculateSize(group.size, options)
                        span.style.marginTop = "${top}em";
                    }
                }
                span
            }
        )
    }
}
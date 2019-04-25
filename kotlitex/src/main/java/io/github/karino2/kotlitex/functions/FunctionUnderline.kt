package io.github.karino2.kotlitex.functions

import io.github.karino2.kotlitex.*
import java.lang.IllegalArgumentException

object FunctionUnderline {
    fun defineAll() {
        LatexFunctions.defineFunction(
            FunctionSpec(
                "underline",
                1,
                allowedInText=true
            ),
            listOf("\\underline"),
            { context: FunctionContext, args: List<ParseNode>, _ /* optArgs */: List<ParseNode?> ->
                PNodeUnderline(
                    context.parser.mode,
                    null,
                    args[0]
                )
            },
            {group: ParseNode, options: Options ->
                if(group !is PNodeUnderline)
                    throw IllegalArgumentException("unexpected type in underline's RNodeBuilder.")

                // Underlines are handled in the TeXbook pg 443, Rule 10.
                // Build the inner group.
                val innerGroup = RenderTreeBuilder.buildGroup(group.body, options);

                // Create the line to go below the body
                val line = RenderTreeBuilder.makeLineSpan(CssClass.underline_line, options);

                // Generate the vlist, with the appropriate kerns
                val vlist = RenderBuilderVList.makeVList(
                    VListParamPositioned(
                        PositionType.Top,
                        innerGroup.height,
                     listOf(
                         VListKern(line.height),
                         VListElem(line),
                         VListKern(3*line.height),
                         VListElem(innerGroup)
                     )
                    ), options)

                RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.mord, CssClass.underline), mutableListOf(vlist), options)
            })
    }
}
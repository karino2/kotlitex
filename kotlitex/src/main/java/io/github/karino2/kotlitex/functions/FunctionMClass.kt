package io.github.karino2.kotlitex.functions

import io.github.karino2.kotlitex.*
import java.lang.IllegalArgumentException

object FunctionMClass {
    fun renderNodeBuilder(group: ParseNode, options: Options): RenderNode {
        if (group !is PNodeMClass) {
            throw IllegalArgumentException("unexpected type in mclass RNodeBuilder.")
        }
        val elements = RenderTreeBuilder.buildExpression(group.body, options, true)
        return RenderTreeBuilder.makeSpan(mutableSetOf(group.mclass), elements.toMutableList(), options)
    }

    fun defineAll() {
        // Math class commands except \mathop
        LatexFunctions.defineFunction(
            FunctionSpec("mclass", 1),
            listOf(
                "\\mathord", "\\mathbin", "\\mathrel", "\\mathopen",
                "\\mathclose", "\\mathpunct", "\\mathinner"
            ),
            { context: FunctionContext, args: List<ParseNode>, _ /* optArgs */ : List<ParseNode?> ->
                val parser = context.parser
                val funcName = context.funcName
                val body = args[0]



                PNodeMClass(
                    parser.mode,
                    null,
                    CssClass.mclass("m" + funcName.substring(5)),
                    LatexFunctions.ordargument(body)
                )
            },
            FunctionMClass::renderNodeBuilder
        )

    }
}
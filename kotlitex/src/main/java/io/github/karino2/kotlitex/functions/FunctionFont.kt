package io.github.karino2.kotlitex.functions

import io.github.karino2.kotlitex.*
import java.lang.IllegalArgumentException

object FunctionFont {
    fun renderNodeBuilder(group: ParseNode, options: Options) : RenderNode {
        if (group !is PNodeFont) {
            throw IllegalArgumentException("unexpected type in font RNodeBuilder.")
        }
        val newOptions = options.copy(font = group.font)
        return RenderTreeBuilder.buildGroup(group.body, newOptions)
    }

    val fontAliases = mapOf(
        "\\Bbb" to "\\mathbb",
        "\\bold" to "\\mathbf",
        "\\frak" to "\\mathfrak",
        "\\bm" to "\\boldsymbol"
    )

    fun defineAll() {
        LatexFunctions.defineFunction(
            FunctionSpec("font", 1, greediness = 2),
            listOf(
                // styles, except \boldsymbol defined below
                "\\mathrm", "\\mathit", "\\mathbf", "\\mathnormal",

                // families
                "\\mathbb", "\\mathcal", "\\mathfrak", "\\mathscr", "\\mathsf",
                "\\mathtt",

                // aliases, except \bm defined below
                "\\Bbb", "\\bold", "\\frak"
            ),
            { context: FunctionContext, args: List<ParseNode>, _ /* optArgs */ : List<ParseNode?> ->
                val parser = context.parser
                val fName = context.funcName

                val func = if(fontAliases.containsKey(fName)) {
                    fontAliases.getValue(fName)
                } else fName

                PNodeFont(parser.mode,
                    null, func.substring(1), args[0])
            },
            FunctionFont::renderNodeBuilder
        )

        /* TODO: need binrelClass in mclass. implement after mclass
        LatexFunctions.defineFunction(
            FunctionSpec("mclass", 1, greediness = 2),
            listOf("\\boldsymbol", "\\bm"),
            { context: FunctionContext, args: List<ParseNode>, _ /* optArgs */ : List<ParseNode?> ->
                val parser = context.parser
                val fName = context.funcName

                val func = if(fontAliases.containsKey(fName)) {
                    fontAliases.getValue(fName)
                } else fName

                PNodeFont(parser.mode,
                    null, func.substring(1), args[0])
            },
            FunctionFont::renderNodeBuilder
        )
        */

        // Old font changing functions
        LatexFunctions.defineFunction(
            FunctionSpec("font", 0, allowedInText = true),
            listOf("\\rm", "\\sf", "\\tt", "\\bf", "\\it"),
            { context: FunctionContext, _/* args */: List<ParseNode>, _ /* optArgs */ : List<ParseNode?> ->
                val parser = context.parser
                val fName = context.funcName
                val breakOnTokenText = context.breakOnTokenText

                parser.consumeSpaces()
                val body = parser.parseExpression(true, breakOnTokenText)
                val style = "math${fName.substring(1)}"

                PNodeFont(parser.mode, null, style,
                    PNodeOrdGroup(parser.mode, null, body))
            },
            FunctionFont::renderNodeBuilder
        )
    }
}
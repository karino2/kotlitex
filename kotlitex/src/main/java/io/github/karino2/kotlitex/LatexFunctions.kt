package io.github.karino2.kotlitex

// LaTeX argument type.
//   - "size": A size-like thing, such as "1em" or "5ex"
//   - "color": An html color, like "#abc" or "blue"
//   - "url": An url string, in which "\" will be ignored
//   -        if it precedes [#$%&~_^\{}]
//   - "raw": A string, allowing single character, percent sign,
//            and nested braces
//   - "original": The same type as the environment that the
//                 function being parsed is in (e.g. used for the
//                 bodies of functions like \textcolor where the
//                 first argument is special and the second
//                 argument is parsed normally)
//   - Mode: Node group parsed in given mode.
sealed class LatexArgType

object ArgColor : LatexArgType()
object ArgSize : LatexArgType()
object ArgUrl : LatexArgType()
object ArgRaw : LatexArgType()
object ArgOriginal : LatexArgType()
data class ArgMode(val mode: Mode) : LatexArgType()



data class FunctionContext(val funcName: String, val parser: Parser, val token: Token?, val breakOnTokenText: String? /* BreakToken, one of "]" | "}" | "$" | "\\)" | "\\cr"*/ )


// from FunctionPropSpec
data class FunctionSpec(val type: String,
                        // The number of arguments the function takes.
                        val numArgs: Int,
                        // An array corresponding to each argument of the function, giving the
                        // type of argument that should be parsed. Its length should be equal
                        // to `numOptionalArgs + numArgs`, and types for optional arguments
                        // should appear before types for mandatory arguments.
                        val argTypes: List<LatexArgType>? = null,
                        // The greediness of the function to use ungrouped arguments.
                        //
                        // E.g. if you have an expression
                        //   \sqrt \frac 1 2
                        // since \frac has greediness=2 vs \sqrt's greediness=1, \frac
                        // will use the two arguments '1' and '2' as its two arguments,
                        // then that whole function will be used as the argument to
                        // \sqrt. On the other hand, the expressions
                        //   \frac \frac 1 2 3
                        // and
                        //   \frac \sqrt 1 2
                        // will fail because \frac and \frac have equal greediness
                        // and \sqrt has a lower greediness than \frac respectively. To
                        // make these parse, we would have to change them to:
                        //   \frac {\frac 1 2} 3
                        // and
                        //   \frac {\sqrt 1} 2
                        //
                        // The default value is `1`
                        val greediness: Int = 1,

                        // Whether or not the function is allowed inside text mode
                        // (default false)
                        val allowedInText: Boolean = false,

                        // Whether or not the function is allowed inside text mode
                        // (default true)
                        val allowedInMath: Boolean = true,

                        // (optional) The number of optional arguments the function
                        // should parse. If the optional arguments aren't found,
                        // `null` will be passed to the handler in their place.
                        // (default 0)
                        val numOptionalArgs: Int = 0,

                        // Must be true if the function is an infix operator.
                        val infix: Boolean = false,

                        // Switch to the specified mode while consuming the command token.
                        // This is useful for commands that switch between math and text mode,
                        // for making sure that a switch happens early enough.  Note that the
                        // mode is switched immediately back to its original value after consuming
                        // the command token, so that the argument parsing and/or function handler
                        // can easily access the old mode while doing their own mode switching.
                        val consumeMode: Mode? = null)

typealias HandlerType = (FunctionContext, List<ParseNode>, List<ParseNode?>)->ParseNode
typealias RenderNodeHandlerType = (ParseNode, Options)->RenderNode

data class FunctionDef(val spec: FunctionSpec,
                       val handler: HandlerType)

/*

    fun registerBuilder(nodeType: String, builder: (ParseNode, Options)->RenderNode) {
        groupBuilders[nodeType] = builder
    }

 */

object LatexFunctions {
    val functions = mutableMapOf<String, FunctionDef>()
    val renderGroupBuilders = mutableMapOf<String, RenderNodeHandlerType>()

    fun defineFunction(spec: FunctionSpec, names: List<String>, handler: HandlerType, groupHandler: RenderNodeHandlerType) {
        val fundef = FunctionDef(spec, handler)
        for(name in names) {
            functions[name] = fundef
        }
        renderGroupBuilders[spec.type] = groupHandler
    }

    fun defineFunctionBuilder(type: String, groupHandler: RenderNodeHandlerType) {
        defineFunction(FunctionSpec(type, numArgs=0), listOf(), {_,_,_->throw Error("Should never be called.")}, groupHandler)
    }
}

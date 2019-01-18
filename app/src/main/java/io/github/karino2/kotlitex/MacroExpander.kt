package io.github.karino2.kotlitex

class MacroExpander(val input: String, var mode : Mode = Mode.MATH) {
    val lexer = Lexer(input)

    val stack = ArrayList<Token>()

    fun beginGroup() = {}
    fun endGroup() = {}

    companion object {
        val implicitCommands = setOf(
            "\\relax",     // MacroExpander.js
        "^",           // Parser.js
        "_",           // Parser.js
        "\\limits",    // Parser.js
        "\\nolimits"  // Parser.js
        )
    }

    /**
     * Returns the topmost token on the stack, without expanding it.
     * Similar in behavior to TeX's `\futurelet`.
     */
    fun future() : Token {
        if(stack.size == 0) {
            pushToken(lexer.lex())
        }
        return stack.last()
    }

    fun pushToken(token: Token) = stack.add(token)

    fun popToken(): Token {
        future()
        return stack.removeAt(stack.size-1)
    }



    /*
       In original katex, return type is Token|Token[] and semantics is a little different.(Fully expanded or not).
       I add boolean flag to specify whether this is token[] semantics or token semantics (True means token) for kotlin.
     */
    fun expandOnce(): Pair<Boolean, List<Token>> {
        val topToken = popToken()
        // TODO: implement expand.

        pushToken(topToken)
        return Pair(true, listOf(topToken))
    }

    /**
     * Recursively expand first token, then return first non-expandable token.
     */
    fun expandNextToken() : Token {
        while(true) {
            val (expanded, tokens) = expandOnce()

            if(expanded) {
                val token = tokens.last()

                // \relax stops the expansion, but shouldn't get returned (a
                // null return value couldn't get implemented as a function).
                if (token.text === "\\relax") {
                    stack.removeAt(stack.size-1)
                } else {
                    return stack.removeAt(stack.size -1)  // === expanded
                }

            }
        }
    }

    fun switchMode(newMode: Mode) {
        mode = newMode
    }

}
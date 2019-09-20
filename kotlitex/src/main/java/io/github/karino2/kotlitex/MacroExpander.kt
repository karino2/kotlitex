package io.github.karino2.kotlitex

class MacroExpander(val input: String, val settings: Settings, var mode: Mode = Mode.MATH) {
    var lexer = Lexer(input)

    val stack = ArrayList<Token>()

    var expansionCount = 0
    val macros = Namespace(Macros.builtinMacros, settings.macros)

    fun ArrayList<Token>.pop() = this.removeAt(this.lastIndex)


    /**
     * Feed a new input string to the same MacroExpander
     * (with existing macros etc.).
     */
    fun feed(input: String) {
        lexer = Lexer(input);
    }



    /**
     * Start a new group nesting within all namespaces.
     */
    fun beginGroup() { macros.beginGroup() }


    /**
     * End current group nesting within all namespaces.
     */
    fun endGroup() { macros.endGroup() }

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
    fun future(): Token {
        if (stack.size == 0) {
            pushToken(lexer.lex())
        }
        return stack.last()
    }

    fun pushToken(token: Token) = stack.add(token)

    fun pushTokens(tokens: List<Token>) = stack.addAll(tokens)


    fun popToken(): Token {
        future()
        return stack.pop()
    }

    /**
     * Consume all following space tokens, without expansion.
     */
    fun consumeSpaces() {
        while(true) {
            val token = this.future();
            if (token.text == " ") {
                this.stack.pop();
            } else {
                return
            }
        }
    }

    /**
     * Consume the specified number of arguments from the token stream,
     * and return the resulting array of arguments.
     */
    fun consumeArgs(numArgs: Int): List<List<Token>> {
        val args = ArrayList<ArrayList<Token>>()
        // obtain arguments, either single token or balanced {…} group
        repeat(numArgs) {i->
            this.consumeSpaces()  // ignore spaces before each argument
            val startOfArg = this.popToken()
            when {
                startOfArg.text == "{" -> {
                    val arg = ArrayList<Token>()
                    var depth = 1
                    while (depth != 0) {
                        val tok = this.popToken()
                        arg.add(tok)
                        when {
                            tok.text == "{" -> ++depth
                            tok.text == "}" -> --depth
                            tok.text == "EOF" -> throw ParseError(
                                "End of input in macro argument",
                                startOfArg)
                        }
                    }
                    arg.pop(); // remove last }
                    arg.reverse(); // like above, to fit in with stack order
                    args[i] = arg;
                }
                startOfArg.text == "EOF" -> throw ParseError(
                    "End of input expecting macro argument", null)
                else -> args[i] = arrayListOf(startOfArg)
            }
        }
        return args
    }

    /*
     * Expand the next token only once if possible.
     *
     * If the token is expanded, the resulting tokens will be pushed onto
     * the stack in reverse order and will be returned as an array,
     * also in reverse order.
     *
     * If not, the next token will be returned without removing it
     * from the stack.  This case can be detected by a `Token` return value
     * instead of an `Array` return value.
     *
     * In either case, the next token will be on the top of the stack,
     * or the stack will be empty.
     *
     * Used to implement `expandAfterFuture` and `expandNextToken`.
     *
     * At the moment, macro expansion doesn't handle delimited macros,
     * i.e. things like those defined by \def\foo#1\end{…}.
     * See the TeX book page 202ff. for details on how those should behave.


    Added comment for kotlitex:
       In original katex, return type is Token|Token[] and semantics is a little different.(Fully expanded or not).
       I add boolean flag to specify whether this is token[] semantics or token semantics (True means token) for kotlin.
     */
    fun expandOnce(): Pair<Boolean, List<Token>> {
        val topToken = popToken()
        val name = topToken.text
        val expansion = this._getExpansion(name);
        if (expansion == null) { // mainly checking for undefined here
            // Fully expanded
            this.pushToken(topToken);
            return Pair(true, listOf(topToken))
        }
        expansionCount++;
        if (this.expansionCount > this.settings.maxExpand) {
            throw ParseError("Too many expansions: infinite loop or " +
                    "need to increase maxExpand setting", null);
        }
        var tokens = expansion.tokens
        if (expansion.numArgs > 0) {
            val args = this.consumeArgs(expansion.numArgs);
            // paste arguments in place of the placeholders
            tokens = mutableListOf<Token>().apply { addAll(tokens) } // make a shallow copy

            // index manipulation inside reverse for loop...
            var i = tokens.size -1
            while(i >= 0) {
                var tok = tokens[i]
                if (tok.text == "#") {
                    if (i == 0) {
                        throw ParseError(
                                "Incomplete placeholder at end of macro body",
                        tok)
                    }
                    tok = tokens[--i]; // next token on stack
                    if (tok.text == "#") { // ## → #
                        tokens.removeAt(i+1) // drop first #
                    } else if ("^[1-9]$".toRegex().matches(tok.text)) {
                        // replace the placeholder with the indicated argument
                        val toknum = tok.text.toInt()

                        //  Original JS code
                        //  tokens.splice(i, 2, ...args[tok.text - 1]);
                        tokens.removeAt(i)
                        tokens.removeAt(i)
                        tokens.addAll(i, args[toknum -1])
                    } else {
                        throw ParseError(
                                "Not a valid argument number",
                        tok)
                    }
                }
                i--
            }
        }
        // Concatenate expansion onto top of stack.
        this.pushTokens(tokens);
        return Pair(false, tokens)
    }

    /**
     * Recursively expand first token, then return first non-expandable token.
     */
    fun expandNextToken(): Token {
        while (true) {
            val (expanded, tokens) = expandOnce()

            if (expanded) {
                val token = tokens.last()

                // \relax stops the expansion, but shouldn't get returned (a
                // null return value couldn't get implemented as a function).
                if (token.text === "\\relax") {
                    stack.removeAt(stack.size - 1)
                } else {
                    return stack.removeAt(stack.size - 1)  // === expanded
                }
            }
        }
    }

    /**
     * Returns the expanded macro as a reversed array of tokens and a macro
     * argument count.  Or returns `null` if no such macro.
     */
    fun _getExpansion(name: String): MacroExpansion? {
        // mainly checking for undefined here
        val definition = this.macros.get(name) ?: return null
        val expansionDef = when(definition) {
            is MacroFunction -> definition.func(this)
            else->definition
        }
        if (expansionDef is MacroString) {
            var numArgs = 0
            val expansion = expansionDef.value

            if (expansion.indexOf("#") != -1) {

                val stripped = expansion.replace("##".toRegex(), "")
                while (stripped.indexOf("#" + (numArgs + 1)) != -1) {
                    ++numArgs;
                }
            }
            val bodyLexer = Lexer(expansion)
            val tokens = ArrayList<Token>()
            var tok = bodyLexer.lex();
            while (tok.text != "EOF") {
                tokens.add(tok);
                tok = bodyLexer.lex();
            }
            tokens.reverse() // to fit in with stack using push and pop
            return MacroExpansion(tokens, numArgs)
        }

        //function return only MacroString or MacroExpansion.
        return expansionDef as MacroExpansion
    }


    /**
     * Switches between "text" and "math" modes.
     */
    fun switchMode(newMode: Mode) {
        mode = newMode
    }
}
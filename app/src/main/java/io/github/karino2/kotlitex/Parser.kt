package io.github.karino2.kotlitex

import io.github.karino2.kotlitex.Lexer.Companion.combiningDiacriticalMarksEndRegex
import io.github.karino2.kotlitex.functions.FunctionFrac
import io.github.karino2.kotlitex.functions.FunctionOp
import io.github.karino2.kotlitex.functions.FunctionSqrt

enum class Mode { MATH, TEXT }
data class SourceLocation(val lexer: Lexer?, val start:Int, val end: Int) {
    companion object {

        /**
         * Merges two `SourceLocation`s from location providers, given they are
         * provided in order of appearance.
         * - Returns the first one's location if only the first is provided.
         * - Returns a merged range of the first and the last if both are provided
         *   and their lexers match.
         * - Otherwise, returns null.
         */
        fun range(first: Token?, second: Token? = null) : SourceLocation? {
            return if (second == null) {
                first?.loc
            } else if (first?.loc == null || second.loc == null
                || first.loc.lexer != second.loc.lexer) {
                null
            } else {
                SourceLocation(
                    first.loc.lexer, first.loc.start, second.loc.end);
            }

        }
    }

    fun getSource(): String {
        return this.lexer!!.input.slice(start until end);
    }

}


typealias Font = String

// enum class Font { MAIN, AMS }

interface Group

enum class Atoms : Group {
    bin, close, inner, open, punct, rel
}

enum class NonAtoms: Group {
    accent_token, mathord, op_token, spacing, textord
}

data class CharInfo(val font: Font, val group : Group, val replace : String?)

data class Settings(val displayMode :Boolean = false, val throwOnError: Boolean = true,
                    val errorColor: String = "#cc0000",
                    val macros: Map<String, Any?> /*MacroMap*/ =  mapOf(),
                    val colorIsTextColor: Boolean = false,
                    val strict : Any? /* strict: boolean | "ignore" | "warn" | "error" | StrictFunction */ = "warn",
                    val maxSize: Int = Int.MAX_VALUE,
                    val maxExpand: Int = 1000,
                    val allowedProtocols: List<String> = emptyList()) {
    /**
     * Report nonstrict (non-LaTeX-compatible) input.
     * Can safely not be called if `this.strict` is false in JavaScript.
     */
    fun reportNonstrict(errorCode: String, errorMsg: String,
    token: Any? /* Token | AnyParseNode*/) {
        /* TODO:
        val strict = this.strict;
        if (typeof strict === "function") {
            // Allow return value of strict function to be boolean or string
            // (or null/undefined, meaning no further processing).
            strict = strict(errorCode, errorMsg, token);
        }
        */
        if (strict == null || strict == "ignore") {
            return;
        } else if (strict == true || strict == "error") {
            throw ParseError(
                    "LaTeX-incompatible input and strict mode is set to 'error': " +
                            "${errorMsg} [${errorCode}]", token as Token);
        } else if (strict == "warn") {
            /* TODO:
            typeof console !== "undefined" && console.warn(
                "LaTeX-incompatible input and strict mode is set to 'warn': " +
                        `${errorMsg} [${errorCode}]`);
                        */
        } else {  // won't happen in type-safe code
            /* TODO:
            typeof console !== "undefined" && console.warn(
                "LaTeX-incompatible input and strict mode is set to " +
                        `unrecognized '${strict}': ${errorMsg} [${errorCode}]`);
                        */
        }
    }

}

data class AccentRelation(val text: String, val math: String) {
    fun get(mode: Mode) =if(mode==Mode.MATH) math else text
}

class Parser(val input: String, val settings:Settings = Settings()) {
    companion object {
        val endOfExpression = listOf("}", "\\end", "\\right", "&");
        val SUPSUB_GREEDINESS = 1

        init {
            // Is here right place?
            FunctionFrac.defineAll()
            FunctionSqrt.defineAll()
            FunctionOp.defineAll()
        }
    }





    var mode = Mode.MATH



    var _nextToken : Token? = null

    val gullet = MacroExpander(input, mode)

    fun consume() {
        _nextToken = gullet.expandNextToken()
    }

    val nextToken: Token
        get() = _nextToken!!

    fun consumeSpaces() {
        while (nextToken.text == " ") {
            this.consume();
        }
    }

    fun consumeComment() {
        // the newline character is normalized in Lexer, check original source
        while (nextToken.text != "EOF" && nextToken.loc != null &&
            nextToken.loc?.getSource()?.indexOf("\n") == -1) {
            this.consume();
        }

        if (nextToken.text == "EOF") {
            this.settings.reportNonstrict("commentAtEnd",
                "% comment has no terminating newline; LaTeX would " +
                        "fail because of commenting the end of math mode (e.g. $)", null);
        }
        if (mode == Mode.MATH) {
            this.consumeSpaces(); // ignore spaces in math mode
        } else {// text mode
            nextToken.loc?.let {
                val source = it.getSource();
                if (source.indexOf("\n") == source.length - 1) {
                    this.consumeSpaces(); // if no space after the first newline
                }
            }
        }
    }



    fun switchMode(newMode: Mode) {
        mode = newMode
        gullet.switchMode(newMode)
    }

    /**
     * Checks a result to make sure it has the right type, and throws an
     * appropriate error otherwise.
     */
    fun expect(text: String, consume: Boolean = true) {
        if(nextToken.text != text) {
            throw ParseError(
                "Expected '${text}', got '${nextToken.text}'",
                nextToken
            )
        }
        if(consume) {
            consume()
        }
    }

    /**
     * Parses a group when the mode is changing.
     */
    fun parseGroupOfType(
            name: String,
            type: LatexArgType?,
            optional: Boolean,
            greediness: Int?
            ): ParseNode? {
        return when (type) {
            is ArgColor -> this.parseColorGroup(optional);
            is ArgSize -> this.parseSizeGroup(optional);
            is ArgUrl -> this.parseUrlGroup(optional);
            is ArgMode -> this.parseGroup(name, optional, greediness, null, type.mode);
            is ArgOriginal -> this.parseGroup(name, optional, greediness)
            null -> this.parseGroup(name, optional, greediness)
            else ->
                throw ParseError(
                    "Unknown group type as $name", this.nextToken);
        }
    }

    /**
     * Call a function handler with a suitable context and arguments.
     */
    fun callFunction(
        name: String,
        args: List<ParseNode>,
        optArgs: List<ParseNode?>,
        token: Token?,
        breakOnTokenText: String? /* BreakToken?, */
        ): ParseNode {
        val context = FunctionContext(name, this, token, breakOnTokenText)
        val func = LatexFunctions.functions[name]
        if (func?.handler != null) {
            return func.handler(context, args, optArgs);
        } else {
            throw ParseError("No function handler for ${name}", null)
        }
    }


    /**
     * Parses the arguments of a function or environment
     */
    fun parseArguments(
            func: String,   // Should look like "\name" or "\begin{name}".
            funcData: FunctionSpec /* FunctionSpec<*> | EnvSpec<*>,*/
        ): Pair<List<ParseNode>, List<ParseNode?>> {
        val totalArgs = funcData.numArgs + funcData.numOptionalArgs;

        if (totalArgs == 0) {
            return Pair(listOf(), listOf())
        }

        val baseGreediness = funcData.greediness;
        val args = mutableListOf<ParseNode>()
        val optArgs = mutableListOf<ParseNode?>()

        repeat(totalArgs) { i->
            val argType = funcData.argTypes?.get(i);
            val isOptional = i < funcData.numOptionalArgs
            // Ignore spaces between arguments.  As the TeXbook says:
            // "After you have said ‘\def\row#1#2{...}’, you are allowed to
            //  put spaces between the arguments (e.g., ‘\row x n’), because
            //  TeX doesn’t use single spaces as undelimited arguments."
            if (i > 0 && !isOptional) {
                this.consumeSpaces();
            }
            // Also consume leading spaces in math mode, as parseSymbol
            // won't know what to do with them.  This can only happen with
            // macros, e.g. \frac\foo\foo where \foo expands to a space symbol.
            // In LaTeX, the \foo's get treated as (blank) arguments).
            // In KaTeX, for now, both spaces will get consumed.
            // TODO(edemaine)
            if (i == 0 && !isOptional && this.mode == Mode.MATH) {
                this.consumeSpaces();
            }
            val nextToken = this.nextToken;
            val arg = this.parseGroupOfType(
                "argument to '$func'",
            argType, isOptional, baseGreediness);
            if (arg == null) {
                if (isOptional) {
                    optArgs.add(null)
                    return@repeat // continue
                }
                throw ParseError(
                    "Expected group after '$func'", nextToken);
            }
            if(isOptional) {
                optArgs.add(arg)
            } else {
                args.add(arg)
            }
        }

        return Pair(args, optArgs)
    }



    /**
     * Parses an entire function, including its base and all of its arguments.
     */
    fun parseFunction(
        breakOnTokenText: String? /* BreakToken?, "]" | "}" | "$" | "\\)" | "\\cr" */,
        name: String?, // For error reporting.
        greediness: Int?
       ): ParseNode? {
        val token = this.nextToken;
        val func = token.text
        val funcData = LatexFunctions.functions[func] ?: return null

        if (greediness != null && funcData.spec.greediness <= greediness) {
            throw ParseError(
                    "Got function '" + func + "' with no arguments" +
                            if(name != null) " as $name" else "", token)
        } else if (this.mode == Mode.TEXT && !funcData.spec.allowedInText) {
            throw ParseError(
                "Can't use function '$func' in text mode", token);
        } else if (this.mode == Mode.MATH && !funcData.spec.allowedInMath) throw ParseError(
            "Can't use function '$func' in math mode", token)

        // Consume the command token after possibly switching to the
        // mode specified by the function (for instant mode switching),
        // and then immediately switch back.
        if (funcData.spec.consumeMode != null) {
            val oldMode = this.mode;
            this.switchMode(funcData.spec.consumeMode);
            this.consume();
            this.switchMode(oldMode);
        } else {
            this.consume();
        }
        val (args, optArgs) = this.parseArguments(func, funcData.spec);
        return this.callFunction(func, args, optArgs, token, breakOnTokenText);
    }


    /**
     * Parses a group, essentially returning the string formed by the
     * brace-enclosed tokens plus some position information.
     */
    fun parseStringGroup(
        modeName: LatexArgType,  // Used to describe the mode in error messages.
        optional: Boolean,
        raw: Boolean? = null
    ): Token? {
        val groupBegin = if(optional)  "[" else "{"
        val groupEnd = if(optional) "]" else "}"
        if (nextToken.text != groupBegin) {
            if (optional) {
                return null;
            } else if (raw == true && nextToken.text != "EOF" &&
                "[^{}\\[\\]]".toRegex().matches(nextToken.text)){ //  /[^{}[\]]/.test(nextToken.text)) {
                // allow a single character in raw string group
                this.consume();
                return nextToken
            }
        }
        val outerMode = this.mode
        this.mode = Mode.TEXT
        this.expect(groupBegin)
        var str = ""
        val firstToken = this.nextToken
        var nested = 0 // allow nested braces in raw string group
        var lastToken = firstToken
        loop@ while ((raw == true && nested > 0) || this.nextToken.text != groupEnd) {
            when (this.nextToken.text) {
                "EOF" ->
                throw ParseError(
                        "Unexpected end of input in " + modeName,
                firstToken.range(lastToken, str));
                "%" ->
                if (raw != true) { // allow % in raw string group
                    this.consumeComment();
                    continue@loop
                }
                groupBegin -> nested++
                groupEnd -> nested--
            }
            lastToken = this.nextToken
            str += lastToken.text;
            this.consume();
        }
        this.mode = outerMode;
        this.expect(groupEnd);
        return firstToken.range(lastToken, str);
    }

    /**
     * Parses a color description.
     */
    fun parseColorGroup(optional: Boolean): PNodeColorToken? {
        val res = this.parseStringGroup(ArgColor, optional) ?: return null

        val match = "^(#[a-f0-9]{3}|#?[a-f0-9]{6}|[a-z]+)\$".toRegex(RegexOption.IGNORE_CASE).matchEntire(res.text) ?:
            throw ParseError("Invalid color: '" + res.text + "'", res);


        var color = match.groupValues[0];
        if ("^[0-9a-f]{6}$".toRegex(RegexOption.IGNORE_CASE).matches(color)) {
            // We allow a 6-digit HTML color spec without a leading "#".
            // This follows the xcolor package's HTML color model.
            // Predefined color names are all missed by this RegEx pattern.
            color = "#$color";
        }
        return PNodeColorToken(mode, null, color)
    }

    /**
     * Parses a regex-delimited group: the largest sequence of tokens
     * whose concatenated strings match `regex`. Returns the string
     * formed by the tokens plus some position information.
     */
    fun parseRegexGroup(
        regex: Regex,
        modeName: String   // Used to describe the mode in error messages.
    ): Token {
        val outerMode = this.mode
        this.mode = Mode.TEXT
        val firstToken = this.nextToken
        var lastToken = firstToken
        var str = ""
        while (this.nextToken.text != "EOF"
            && (regex.matches(str + this.nextToken.text)
                    || this.nextToken.text == "%")) {
            if (this.nextToken.text == "%") {
                this.consumeComment();
                continue;
            }
            lastToken = this.nextToken
            str += lastToken.text
            this.consume()
        }
        if (str == "") {
            throw ParseError(
                    "Invalid " + modeName + ": '" + firstToken.text + "'",
            firstToken)
        }
        this.mode = outerMode
        return firstToken.range(lastToken, str)
    }


    /**
     * Parses a size specification, consisting of magnitude and unit.
     */
    fun parseSizeGroup(optional: Boolean): PNodeSize? {
        var isBlank = false;
        var res = (if (!optional && this.nextToken.text != "{") {
            this.parseRegexGroup(
                "^[-+]? *(?:$|\\d+|\\d+\\.\\d*|\\.\\d*) *[a-z]{0,2} *$".toRegex(), "size");
        } else {
            this.parseStringGroup(ArgSize, optional);
        }) ?: return null

        if (!optional && res.text.isEmpty()) {
            // Because we've tested for what is !optional, this block won't
            // affect \kern, \hspace, etc. It will capture the mandatory arguments
            // to \genfrac and \above.
            res = res.copy(text = "0pt") // Enable \above{}
            isBlank = true;      // This is here specifically for \genfrac
        }
        val match = ("([-+]?) *(\\d+(?:\\.\\d*)?|\\.\\d+) *([a-z]{2})".toRegex()).matchEntire(res.text) ?:
            throw ParseError("Invalid size: '" + res.text + "'", res);

        val data = Measurement(
            (match.groupValues[1] + match.groupValues[2]).toInt(), // sign + magnitude, cast to number
            match.groupValues[3]
        )

        if (!data.isValidUnit) {
            throw ParseError("Invalid unit: '" + data.unit + "'", res);
        }
        return PNodeSize(mode, null, data, isBlank)
    }

    /**
     * Parses an URL, checking escaped letters and allowed protocols.
     */
    fun parseUrlGroup(optional: Boolean): PNodeUrl? {
        // get raw string
        val res = this.parseStringGroup(ArgUrl, optional, true) ?: return null

        // hyperref package allows backslashes alone in href, but doesn't
        // generate valid links in such cases; we interpret this as
        // "undefined" behaviour, and keep them as-is. Some browser will
        // replace backslashes with forward slashes.
        val url = res.text.replace("\\([#$%&~_^{}]".toRegex(), "$1")
        val protocolMatch = """^\s*([^\\/#]*?)(?::|&#0*58|&#x0*3a)""".toRegex(RegexOption.IGNORE_CASE).matchEntire(url)
        val protocol = if(protocolMatch != null)  protocolMatch.groupValues[1] else "_relative"

        val allowed = this.settings.allowedProtocols
        if ("*" !in allowed && protocol !in allowed)
            throw ParseError("Forbidden protocol '${protocol}'", res)

        return PNodeUrl(mode, null, url)
    }

    /**
     * Converts the textual input of an unsupported command into a text node
     * contained within a color node whose color is determined by errorColor
     */
    fun handleUnsupportedCmd(): ParseNode {
        val text = this.nextToken.text
        val textordArray = mutableListOf<PNodeTextOrd>()

        for (ch in text) {
            textordArray.add(PNodeTextOrd(Mode.TEXT, null, ch.toString()))
        }

        val textNode = PNodeText(mode, null, textordArray)


        val colorNode = PNodeColor(
            mode, null, this.settings.errorColor, listOf(textNode)
        )

        this.consume()
        return colorNode
    }


    /**
     * If `optional` is false or absent, this parses an ordinary group,
     * which is either a single nucleus (like "x") or an expression
     * in braces (like "{x+y}") or an implicit group, a group that starts
     * at the current position, and ends right before a higher explicit
     * group ends, or at EOF.
     * If `optional` is true, it parses either a bracket-delimited expression
     * (like "[x+y]") or returns null to indicate the absence of a
     * bracket-enclosed group.
     * If `mode` is present, switches to that mode while parsing the group,
     * and switches back after.
     */
    fun parseGroup(
        name: String, // For error reporting.
        optional: Boolean,
        greediness: Int?,
        breakOnTokenText: String? = null,
        in_mode: Mode? = null
        ): ParseNode?
    {
        val outerMode = mode
        val firstToken = nextToken
        val text = firstToken.text
        if (in_mode != null) {
            switchMode(in_mode)
        }

        var result: ParseNode?

        // Try to parse an open brace
        val openBrace = if (optional) "[" else "{"
        if (text == openBrace) {
            gullet.beginGroup()
            consume()
            val closeBrace = if (optional) "]" else "}"

            val expression = parseExpression(false, closeBrace)
            val lastToken = nextToken
            // Switch mode back before consuming symbol after close brace
            if (in_mode != null) {
                switchMode(outerMode)
            }
            // End group namespace before consuming symbol after close brace
            this.gullet.endGroup();
            expect(closeBrace)
            return PNodeOrdGroup(mode, SourceLocation.range(firstToken, lastToken), expression)
        }else if(optional) {
            // Return nothing for an optional group
            result = null;
        } else {
            // If there exists a function with this name, parse the function.
            // Otherwise, just return a nucleus

            // TODO:
            result = this.parseFunction(breakOnTokenText, name, greediness) ?:
                    this.parseSymbol();
            if (result == null && text[0] == '\\' &&
                text !in MacroExpander.implicitCommands) {
                if (this.settings.throwOnError) {
                    throw ParseError(
                        "Undefined control sequence: $text", firstToken);
                }
                result = this.handleUnsupportedCmd();
            }
        }
        // Switch mode back
        if (in_mode != null) {
            switchMode(outerMode);
        }
        return result;
    }

    fun supportedCodepoint(ch : Int) = Symbols.supportedCodepoint(ch)


    /**
     * Parse a single symbol out of the string. Here, we handle single character
     * symbols and special functions like verbatim
     */
    private fun parseSymbol(): ParseNode? {
        val nucleus = nextToken
        var text = nucleus.text

        if ("^\\\\verb[^a-zA-Z]".toRegex().find(text) != null) {
            this.consume();

            var arg = text.substring(5)
            val star = (arg[0] == '*')
            if (star) {
                arg = arg.substring(1)
            }
            // Lexer's tokenRegex is constructed to always have matching
            // first/last characters.
            if (arg.length < 2 || arg[0] != arg.last()) {
                throw ParseError("\\verb assertion failed -- \nplease report what input caused this bug", null)
            }
            arg = arg.slice(1 until (arg.length-1)) // remove first and last char
            return PNodeVerb(Mode.TEXT, null, arg, star)
        } else if (text === "%") {
            this.consumeComment();
            return this.parseSymbol();
        }
        // At this point, we should have a symbol, possibly with accents.
        // First expand any accented base symbol according to unicodeSymbols.
        if (Symbols.unicodeSymbols.containsKey(text[0])
            && !Symbols.get(mode).containsKey(text.substring(0 until 1))) {
            // This behavior is not strict (XeTeX-compatible) in math mode.
            if (this.settings.strict != null && mode == Mode.MATH) {
                this.settings.reportNonstrict("unicodeTextInMathMode",
                    "Accented Unicode text character \"${text[0]}\" used in " +
                            "math mode", nucleus);
            }
            text = Symbols.unicodeSymbols[text[0]] + text.substring(1);
        }
        // Strip off any combining characters
        val match = combiningDiacriticalMarksEndRegex.find(text);
        if (match != null) {

            text = text.substring(0, match.range.start);
            if (text == "i") {
                text = "\u0131"  // dotless i, in math and text mode
            } else if (text == "j") {
                text = "\u0237"  // dotless j, in math and text mode
            }
        }
        // Recognize base symbol
        var symbol: ParseNode
        if (Symbols.get(mode).containsKey(text)) {
            if (settings.strict != null && mode == Mode.MATH &&
                Symbols.extraLatin.contains(text)) {
                this.settings.reportNonstrict("unicodeTextInMathMode",
                    "Latin-1/Unicode text character \"${text[0]}\" used in " +
                            "math mode", nucleus)
            }
            val group: Group = Symbols.get(mode).get(text)?.group ?: throw Exception("Never happens")
            val loc = SourceLocation.range(nucleus)
            var s /* TODO: SymbolParseNode */ = if (group is Atoms) {
                PNodeAtom(group, mode, loc, text)
            } else {
                // TODO: handle all Non Atom
                when(group) {
                    NonAtoms.textord -> PNodeTextOrd(mode, loc, text)
                    NonAtoms.mathord -> PNodeMathOrd(mode, loc, text)
                    else -> throw NotImplementedError("NYI for non ATOM symbols.")

                }
                /*
                        // $FlowFixMe
                        s = {
                            type: group,
                            mode: this.mode,
                            loc,
                            text,
                        };
                        */
            }
            symbol = s;
        } else if (text[0].toInt() >= 0x80) { // no symbol for e.g. ^
            if (settings.strict != null) {
                if (!supportedCodepoint(text[0].toInt())) {
                    this.settings.reportNonstrict("unknownSymbol",
                        "Unrecognized Unicode character \"${text[0]}\"" +
                                " (${text[0]})", nucleus)
                } else if (this.mode == Mode.MATH) {
                    this.settings.reportNonstrict("unicodeTextInMathMode",
                        "Unicode text character \"${text[0]}\" used in math mode",
                        nucleus)
                }
            }
            symbol = PNodeTextOrd(mode, SourceLocation.range(nucleus), text)
        } else {
            return null;  // EOF, ^, _, {, }, etc.
        }
        this.consume();
        // Transform combining characters into accents
        if (match != null) {
            for (accent in match.value){
                if (!Symbols.unicodeAccents.containsKey(accent)) {
                    throw ParseError("Unknown accent ' ${accent}'", nucleus)
                }


                val command = Symbols.unicodeAccents[accent]?.get(mode) ?: throw ParseError(
                            "Accent ${accent} unsupported in ${this.mode} mode", nucleus)

                symbol = PNodeAccent(mode, SourceLocation.range(nucleus), command, false, true, symbol)
            }
        }
        return symbol
    }


    /**
     * Parses a group with optional super/subscripts.
     */
    fun parseAtom(breakOnTokenText: String?): ParseNode? {
        // The body of an atom is an implicit group, so that things like
        // \left(x\right)^2 work correctly.
        val base = this.parseGroup("atom", false, null, breakOnTokenText)

        // In text mode, we don't have superscripts or subscripts
        if (mode === Mode.TEXT) {
            return base;
        }

        // Note that base may be empty (i.e. null) at this point.

        var superscript : ParseNode? = null
        var subscript : ParseNode? = null
        while(true) {
            // Guaranteed in math mode, so eat any spaces first.
            consumeSpaces()

            // Lex the first token
            val lex = this.nextToken

            if (lex.text === "\\limits" || lex.text === "\\nolimits") {
                // We got a limit control
                val opNode = base as? PNodeOp ?:
                               throw ParseError("Limit controls must follow a math operator", lex)

                val limits = lex.text == "\\limits";
                opNode.limits = limits;
                opNode.alwaysHandleSupSub = true;
                this.consume();
            } else if (lex.text == "^") {
                // We got a superscript start
                if (superscript != null) {
                    throw ParseError("Double superscript", lex);
                }
                superscript = handleSupSubscript("superscript");
            } else if (lex.text == "_") {
                // We got a subscript start
                if (subscript != null) {
                    throw ParseError("Double subscript", lex);
                }
                subscript = this.handleSupSubscript("subscript");
            } else if (lex.text === "'") {
                // We got a prime
                if (superscript != null) {
                    throw ParseError("Double superscript", lex);
                }
                val prime = PNodeTextOrd(mode, null, "\\prime")

                // Many primes can be grouped together, so we handle this here
                val primes : MutableList<ParseNode> = mutableListOf(prime);
                this.consume();
                // Keep lexing tokens until we get something that's not a prime
                while (nextToken.text == "'") {
                    // For each one, add another prime to the list
                    primes.add(prime);
                    this.consume();
                }
                // If there's a superscript following the primes, combine that
                // superscript in with the primes.
                if (nextToken.text == "^") {
                    primes.add(handleSupSubscript("superscript"));
                }
                // Put everything into an ordgroup as the superscript
                superscript = PNodeOrdGroup(mode, null, primes)
            } else if (lex.text === "%") {
                this.consumeComment();
            } else {
                // If it wasn't ^, _, or ', stop parsing super/subscripts
                break;
            }
        }

        // Base must be set if superscript or subscript are set per logic above,
        // but need to check here for type check to pass.
        if (superscript !=null || subscript != null) {
            // If we got either a superscript or subscript, create a supsub

            return PNodeSupSub(mode, null, base, superscript, subscript)
        } else {
            // Otherwise return the original body
            return base
        }
    }

    /**
     * Handle a subscript or superscript with nice errors.
     */
    private fun handleSupSubscript(name: String): ParseNode {
        val symbolToken = nextToken
        val symbol = symbolToken.text
        consume()
        consumeSpaces() // ignore spaces before sup/subscript argument

        return parseGroup(name, false, SUPSUB_GREEDINESS) ?:
                        throw ParseError("Expected group after '${symbol}'", symbolToken)
    }


    /**
     * Parses an "expression", which is a list of atoms.
     *
     * `breakOnInfix`: Should the parsing stop when we hit infix nodes? This
     *                 happens when functions have higher precendence han infix
     *                 nodes in implicit parses.
     *
     * `breakOnTokenText`: The text of the token that the expression should end
     *                     with, or `null` if something else should end the
     *                     expression.
     */
    fun parseExpression(breakOnInfix: Boolean, breakOnTokenText: String? = null) : List<ParseNode> {
        val body = ArrayList<ParseNode>()
        // Keep adding atoms to the body until we can't parse any more atoms (either
        // we reached the end, a }, or a \right)
        while(true) {
            // Ignore spaces in math mode
            if (this.mode == Mode.MATH) {
                this.consumeSpaces();
            }
            val lex = nextToken
            if(endOfExpression.contains(lex.text)) {
                break
            }
            if(breakOnTokenText != null && breakOnTokenText == lex.text) {
                break
            }

            if (breakOnInfix && LatexFunctions.functions[lex.text]?.spec?.infix == true) {
                break;
            }
            val atom = parseAtom(breakOnTokenText) ?: break
            body.add(atom)
        }

        if(mode == Mode.TEXT){
            // TODO:
            // this.formLigatures(body);
        }
        return handleInfixNodes(body)
    }


    /**
     * Rewrites infix operators such as \over with corresponding commands such
     * as \frac.
     *
     * There can only be one infix operator per group.  If there's more than one
     * then the expression is ambiguous.  This can be resolved by adding {}.
     */
    private fun handleInfixNodes(body: List<ParseNode>): List<ParseNode> {
        return body
        // TODO: implement here.
        /*
        let overIndex = -1;
        let funcName;

        for (let i = 0; i < body.length; i++) {
            const node = checkNodeType(body[i], "infix");
            if (node) {
                if (overIndex !== -1) {
                    throw new ParseError(
                        "only one infix operator per group",
                        node.token);
                }
                overIndex = i;
                funcName = node.replaceWith;
            }
        }

        if (overIndex !== -1 && funcName) {
            let numerNode;
            let denomNode;

            const numerBody = body.slice(0, overIndex);
            const denomBody = body.slice(overIndex + 1);

            if (numerBody.length === 1 && numerBody[0].type === "ordgroup") {
                numerNode = numerBody[0];
            } else {
                numerNode = {type: "ordgroup", mode: this.mode, body: numerBody};
            }

            if (denomBody.length === 1 && denomBody[0].type === "ordgroup") {
                denomNode = denomBody[0];
            } else {
                denomNode = {type: "ordgroup", mode: this.mode, body: denomBody};
            }

            let node;
            if (funcName === "\\\\abovefrac") {
                node = this.callFunction(funcName,
                    [numerNode, body[overIndex], denomNode], []);
            } else {
                node = this.callFunction(funcName, [numerNode, denomNode], []);
            }
            return [node];
        } else {
            return body;
        }

         */
    }


    /**
     * Main parsing function, which parses an entire input.
     */
    fun parse() : List<ParseNode> {
        // Create a group namespace for the math expression.
        // (LaTeX creates a new group for every $...$, $$...$$, \[...\].)
        gullet.beginGroup()

        // Use old \color behavior (same as LaTeX's \textcolor) if requested.
        // We do this within the group for the math expression, so it doesn't
        // pollute settings.macros.
        // TODO:
        /*
        if (this.settings.colorIsTextColor) {
            this.gullet.macros.set("\\color", "\\textcolor");
        }
        */

        // Try to parse the input
        consume()

        val parse = parseExpression(false)

        // If we succeeded, make sure there's an EOF at the end
        this.expect("EOF", false);

        // End the group namespace for the expression
        gullet.endGroup()
        return parse;
    }

}


package io.github.karino2.kotlitex

import java.util.regex.Pattern

data class Token(val text: String, val loc: SourceLocation?) {
    /**
     * Given a pair of tokens (this and endToken), compute a `Token` encompassing
     * the whole input range enclosed by these two.
     */
    fun range(
        endToken: Token,  // last token of the range, inclusive
        text: String     // the text of the newly constructed token
        ) = Token(text, SourceLocation.range(this, endToken))

}

class ParseError(val msg: String, val token: Token?): Exception(msg)


class Lexer(val input: String) {
    companion object {
        val spaceRegexString = "[ \r\n\t]"

        val controlWordRegexString = "\\\\[a-zA-Z@]+"
        val controlSymbolRegexString = "\\\\[^\uD800-\uDFFF]"
        val controlWordWhitespaceRegexString = "${controlWordRegexString}${spaceRegexString}*"
        val controlWordWhitespaceRegex = Regex("^(${controlWordRegexString})${spaceRegexString}*$")
        val combiningDiacriticalMarkString = "[\u0300-\u036f]"


        val combiningDiacriticalMarksEndRegex = Regex("${combiningDiacriticalMarkString}+$")
        /*
        val tokenRegexString = "(${spaceRegexString}+)|" +  // whitespace
                "([!-\\[\\]-\u2027\u202A-\uD7FF\uF900-\uFFFF]" +  // single codepoint
                "${combiningDiacriticalMarkString}*" +            // ...plus accents
                "|[\uD800-\uDBFF][\uDC00-\uDFFF]" +               // surrogate pair
                "${combiningDiacriticalMarkString}*" +            // ...plus accents
                "|\\\\verb\\*([^]).*?\\3" +                       // \verb*
                "|\\\\verb([^*a-zA-Z]).*?\\4" +                   // \verb unstarred
                "|${controlWordWhitespaceRegexString}" +          // \macroName + spaces
                "|${controlSymbolRegexString})"                  // \\, \', etc.
                */
        val tokenRegexString = "(${spaceRegexString}+)|" +  // whitespace
                "([!-\\[\\]-\u2027\u202A-\uD7FF\uF900-\uFFFF]" +  // single codepoint
                "${combiningDiacriticalMarkString}*" +            // ...plus accents
                "|[\uD800-\uDBFF][\uDC00-\uDFFF]" +               // surrogate pair
                "${combiningDiacriticalMarkString}*" +            // ...plus accents
                // Original code was below. but I can't understand the meanings. So I add some character group.
                // "|\\\\verb\\*([^]).*?\\3" +                       // \verb*
                "|\\\\verb\\*([^a]|a).*?\\3" +                       // \verb*
                "|\\\\verb([^*a-zA-Z]).*?\\4" +                   // \verb unstarred
                "|${controlWordWhitespaceRegexString}" +          // \macroName + spaces
                "|${controlSymbolRegexString})"                  // \\, \', etc.

    }

    val tokenRegex = Pattern.compile(tokenRegexString)

    val matcher by lazy {
        tokenRegex.matcher(input)
    }

    var lastIndex = 0

    fun lex() : Token {
        val pos = lastIndex
        if(pos == input.length) {
            return Token("EOF", SourceLocation(this, input.length, input.length))
        }

        if(!matcher.find()) {
            throw ParseError("Unexpected char ${input[pos]}", Token(input[pos].toString(), SourceLocation(this, pos, pos+1)))
        }
        val text = matcher.group(2) ?: " "
        val controlMatch = controlWordWhitespaceRegex.find(text)
        val targetText = controlMatch?.groups?.get(1)?.value ?: text

        lastIndex = matcher.end()

        return Token(targetText, SourceLocation(this, pos, lastIndex))
    }

}
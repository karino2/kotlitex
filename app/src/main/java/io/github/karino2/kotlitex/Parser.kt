package io.github.karino2.kotlitex

enum class Mode { MATH, TEXT }
data class SourceLocation(val lexer: Any?, val start:Int, val end: Int)

sealed class ParseNode;


data class NodeMathOrd(val mode: Mode, val loc: SourceLocation?, val text: String) : ParseNode() {
    val type = "mathord"
}


class Parser(val input: String) {
    var mode = Mode.MATH

    fun parse() : List<ParseNode> {
        return listOf(NodeMathOrd(mode, SourceLocation(null, 0, 1), input))
    }

}
package io.github.karino2.kotlitex

sealed class ParseNode
data class NodeMathOrd(val mode: Mode, val loc: SourceLocation?, val text: String) : ParseNode() {
    val type = "mathord"
}

data class NodeOrdGroup(val mode: Mode, val loc: SourceLocation?, val body: List<ParseNode>) : ParseNode() {
    val type = "ordgroup"
}

// To avoid requiring run-time type assertions, this more carefully captures
// the requirements on the fields per the op.js htmlBuilder logic:
// - `body` and `value` are NEVER set simultanouesly.
// - When `symbol` is true, `body` is set.
data class NodeOp(val mode: Mode, val loc: SourceLocation?,
                  var limits: Boolean,
                  var alwaysHandleSupSub: Boolean?,
                  val suppressBaseShift: Boolean?,
                  val symbol: Boolean,
                  val name: String,
                  val body: Any?
                  ) : ParseNode() {
    val type = "op"
}

data class NodeTextOrd(val mode: Mode, val loc: SourceLocation?, val text: String) : ParseNode() {
    val type = "textord"
}

data class NodeSupSub(val mode: Mode, val loc: SourceLocation?, val base: ParseNode?, val sup: ParseNode?, val sub: ParseNode?) : ParseNode() {
    val type = "supsub"
}

data class NodeVerb(val mode: Mode, val loc: SourceLocation?, val body: String, val start: Boolean) : ParseNode() {
    val type = "verb"
}

data class NodeAtom(val family: Atoms, val mode: Mode, val loc: SourceLocation?, val text: String) : ParseNode() {
    val type = "atom"
}

/*
    "accent": {|
        type: "accent",
        mode: Mode,
        loc?: ?SourceLocation,
        label: string,
        isStretchy?: boolean,
        isShifty?: boolean,
        base: AnyParseNode,
    |},

 */

data class NodeAccent(val mode: Mode, val loc: SourceLocation?, val label: String, val isStretchy: Boolean, val isShifty: Boolean, val base: ParseNode) : ParseNode() {
    val type = "accent"
}

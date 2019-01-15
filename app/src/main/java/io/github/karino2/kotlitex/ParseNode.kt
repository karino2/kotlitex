package io.github.karino2.kotlitex


sealed class ParseNode {
    abstract val mode: Mode
    abstract val loc: SourceLocation?
    abstract val type: String
}

data class NodeOrdGroup(override val mode: Mode, override val loc: SourceLocation?, val body: List<ParseNode>) : ParseNode() {
    override val type = "ordgroup"
}

// To avoid requiring run-time type assertions, this more carefully captures
// the requirements on the fields per the op.js htmlBuilder logic:
// - `body` and `value` are NEVER set simultanouesly.
// - When `symbol` is true, `body` is set.
data class NodeOp(override val mode: Mode, override val loc: SourceLocation?,
                  var limits: Boolean,
                  var alwaysHandleSupSub: Boolean?,
                  val suppressBaseShift: Boolean?,
                  val symbol: Boolean,
                  val name: String,
                  val body: Any?
                  ) : ParseNode() {
    override val type = "op"
}

abstract class NodeOrd(override val mode: Mode, override val loc: SourceLocation?, val text: String) : ParseNode()



class NodeMathOrd(mode: Mode, loc: SourceLocation?, text: String) : NodeOrd(mode, loc, text) {
    override val type = "mathord"
}

class NodeTextOrd(mode: Mode, loc: SourceLocation?, text: String) : NodeOrd(mode, loc, text){
    override val type = "textord"
}

data class NodeSupSub(override val mode: Mode, override val loc: SourceLocation?, val base: ParseNode?, val sup: ParseNode?, val sub: ParseNode?) : ParseNode() {
    override val type = "supsub"
}

data class NodeVerb(override val mode: Mode, override val loc: SourceLocation?, val body: String, val start: Boolean) : ParseNode() {
    override val type = "verb"
}

data class NodeAtom(val family: Atoms, override val mode: Mode, override val loc: SourceLocation?, val text: String) : ParseNode() {
    override val type = "atom"
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

data class NodeAccent(override val mode: Mode, override val loc: SourceLocation?, val label: String, val isStretchy: Boolean, val isShifty: Boolean, val base: ParseNode) : ParseNode() {
    override val type = "accent"
}

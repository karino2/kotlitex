package io.github.karino2.kotlitex


sealed class ParseNode {
    abstract val mode: Mode
    abstract val loc: SourceLocation?
    abstract val type: String
}

data class PNodeOrdGroup(override val mode: Mode, override val loc: SourceLocation?, val body: List<ParseNode>) : ParseNode() {
    override val type = "ordgroup"
}

// To avoid requiring run-time type assertions, this more carefully captures
// the requirements on the fields per the op.js htmlBuilder logic:
// - `body` and `value` are NEVER set simultanouesly.
// - When `symbol` is true, `body` is set.
data class PNodeOp(override val mode: Mode, override val loc: SourceLocation?,
                   var limits: Boolean,
                   var alwaysHandleSupSub: Boolean?,
                   val suppressBaseShift: Boolean?,
                   val symbol: Boolean,
                   var name: String,
                   val body: Any?
                  ) : ParseNode() {
    override val type = "op"
}

abstract class PNodeOrd(override val mode: Mode, override val loc: SourceLocation?, val text: String) : ParseNode()



class PNodeMathOrd(mode: Mode, loc: SourceLocation?, text: String) : PNodeOrd(mode, loc, text) {
    override val type = "mathord"
}

class PNodeTextOrd(mode: Mode, loc: SourceLocation?, text: String) : PNodeOrd(mode, loc, text){
    override val type = "textord"
}

data class PNodeSupSub(override val mode: Mode, override val loc: SourceLocation?, val base: ParseNode?, val sup: ParseNode?, val sub: ParseNode?) : ParseNode() {
    override val type = "supsub"
}

data class PNodeVerb(override val mode: Mode, override val loc: SourceLocation?, val body: String, val start: Boolean) : ParseNode() {
    override val type = "verb"
}

data class PNodeAtom(val family: Atoms, override val mode: Mode, override val loc: SourceLocation?, val text: String) : ParseNode() {
    override val type = "atom"
}

data class PNodeAccent(override val mode: Mode, override val loc: SourceLocation?, val label: String, val isStretchy: Boolean, val isShifty: Boolean, val base: ParseNode) : ParseNode() {
    override val type = "accent"
}

// LaTeX display style.
enum class SizeStyle {
    TEXT,
    DISPLAY,
    SCRIPT,
    SCRIPTSCRIPT,
    AUTO // should we include here?
}


data class PNodeGenFrac(
    override val mode: Mode,
    override val loc: SourceLocation?,
    val continued: Boolean,
    val numer: ParseNode,
    val denom: ParseNode,
    val hasBarLine: Boolean,
    val leftDelim: String?,
    val rightDelim: String?,
    val size: SizeStyle,
    val barSize: Measurement?) : ParseNode() {
    override val type = "genfrac"
}

data class PNodeColorToken(
    override val mode: Mode,
    override val loc: SourceLocation?,
    val color: String) : ParseNode() {
    override val type = "color-token"
}


data class PNodeSize(
    override val mode: Mode,
    override val loc: SourceLocation?,
    val value: Measurement,
    val isBlank : Boolean) : ParseNode() {
    override val type = "size"
}

data class PNodeUrl(
    override val mode: Mode,
    override val loc: SourceLocation?,
    val url : String) : ParseNode() {
    override val type = "url"
}

data class PNodeText(
    override val mode: Mode,
    override val loc: SourceLocation?,
    val body: List<ParseNode>,
    val font: String? = null): ParseNode() {
    override val type = "text"
}

data class PNodeColor(
    override val mode: Mode,
    override val loc: SourceLocation?,
    val color: String,
    val body: List<ParseNode>): ParseNode() {
    override val type = "color"
}

data class PNodeSqrt (
    override val mode: Mode,
    override val loc: SourceLocation?,
    val body: ParseNode,
    val index: ParseNode?
): ParseNode() {
    override val type = "sqrt"
}

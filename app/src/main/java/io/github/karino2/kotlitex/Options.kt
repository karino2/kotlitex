package io.github.karino2.kotlitex

const val BASESIZE = 6
val sizeMultipliers = listOf(
// fontMetrics.js:getGlobalMetrics also uses size indexes, so if
// you change size indexes, change that function.
0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.2, 1.44, 1.728, 2.074, 2.488
)

// A font family applies to a group of fonts (i.e. SansSerif), while a font
// represents a specific font (i.e. SansSerif Bold).
// See: https://tex.stackexchange.com/questions/22350/difference-between-textrm-and-mathrm
data class Options(var style: Style, val _color: String?="", var size: Int = BASESIZE, val font: String="",
                   val fontFamily: String = "", val fontWeight : CssClass = CssClass.EMPTY, val fontShape: CssClass = CssClass.EMPTY,
                   val sizeMultiplier: Double = sizeMultipliers[size], val maxSize: Double = Double.POSITIVE_INFINITY) {

    val textSize = size

    val color: String?
    get() {
        // TODO:
        // return "#6495ed"
        return _color
    }

    /*
getGlobalMetrics: metrics { cssEmPerMu: 0.05555555555555555,
  slant: 0.25,
  space: 0,
  stretch: 0,
  shrink: 0,
  xHeight: 0.431,
  quad: 1,
  extraSpace: 0,
  num1: 0.677,
  num2: 0.394,
  num3: 0.444,
  denom1: 0.686,
  denom2: 0.345,
  sup1: 0.413,
  sup2: 0.363,
  sup3: 0.289,
  sub1: 0.15,
  sub2: 0.247,
  supDrop: 0.386,
  subDrop: 0.05,
  delim1: 2.39,
  delim2: 1.01,
  axisHeight: 0.25,
  defaultRuleThickness: 0.04,
  bigOpSpacing1: 0.111,
  bigOpSpacing2: 0.166,
  bigOpSpacing3: 0.2,
  bigOpSpacing4: 0.6,
  bigOpSpacing5: 0.1,
  sqrtRuleThickness: 0.04,
  ptPerEm: 10,
  doubleRuleSep: 0.2 }
 */
    // TODO:
    val fontMetrics: FontMetrics
    get() = FontMetrics(
        0.05555555555555555,
        0.25,
        0.0,
        0.0,
        0.0,
        0.431,
        1,
        0.0,
        0.677,
        0.394,
        0.444,
        0.686,
        0.345,
        0.413,
        0.363,
        0.289,
        0.15,
        0.247,
        0.386,
        0.05,
        2.39,
        1.01,
        0.25,
        0.04,
        0.111,
        0.166,
        0.2,
        0.6,
        0.1,
        0.04,
        10,
        0.2
    )

    /*
    sizingClasses
     */
    /**
     * Return the CSS sizing classes required to switch from enclosing options
     * `oldOptions` to `this`. Returns an array of classes.
     */
    fun sizingClasses(oldOptions: Options): MutableSet<CssClass> {
        if (oldOptions.size != this.size) {
            return mutableSetOf(
                CssClass.sizing,
                CssClass.resetClass(oldOptions.size),
                CssClass.sizeClass(this.size)
            )
        } else {
            return mutableSetOf()
        }
    }


    val sizeStyleMap = listOf(
    // Each element contains [textsize, scriptsize, scriptscriptsize].
    // The size mappings are taken from TeX with \normalsize=10pt.
    listOf(1, 1, 1),    // size1: [5, 5, 5]              \tiny
        listOf(2, 1, 1),    // size2: [6, 5, 5]
    listOf(3, 1, 1),    // size3: [7, 5, 5]              \scriptsize
    listOf(4, 2, 1),    // size4: [8, 6, 5]              \footnotesize
    listOf(5, 2, 1),    // size5: [9, 6, 5]              \small
    listOf(6, 3, 1),    // size6: [10, 7, 5]             \normalsize
    listOf(7, 4, 2),    // size7: [12, 8, 6]             \large
    listOf(8, 6, 3),    // size8: [14.4, 10, 7]          \Large
    listOf(9, 7, 6),    // size9: [17.28, 12, 10]        \LARGE
    listOf(10, 8, 7),   // size10: [20.74, 14.4, 12]     \huge
    listOf(11, 10, 9)  // size11: [24.88, 20.74, 17.28] \HUGE
    )

    fun sizeAtStyle(size: Int, style: Style): Int {
        return if(style.size < 2)  size else sizeStyleMap[size - 1][style.size - 1];
    };


    /**
     * Return an options object with the given style. If `this.style === style`,
     * returns `this`.
     */
    fun havingStyle(style: Style): Options {
        if (this.style == style) {
            return this
        } else {
            return this.copy(style = style, size = sizeAtStyle(this.textSize, style))
        }
    }


}
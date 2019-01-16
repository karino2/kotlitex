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
                   val sizeMultiplier: Double = sizeMultipliers[size-1], val maxSize: Double = Double.POSITIVE_INFINITY) {

    val textSize = size

    val color: String?
    get() {
        return _color
    }



    val fontMetrics: FontMetrics
    by lazy {
            FontMetrics.getGlobalMetrics(size)
        }

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
            return Options(style=style, size=sizeAtStyle(this.textSize, style))
        }
    }


}
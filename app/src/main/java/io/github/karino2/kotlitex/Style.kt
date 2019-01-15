package io.github.karino2.kotlitex

data class Style(val id: Int, val size: Int, val cramped : Boolean) {
    val isTight : Boolean
    get() = size >= 2

    companion object {
        // IDs of the different styles
        val D = 0
        val Dc = 1
        val T = 2
        val Tc = 3
        val S = 4
        val Sc = 5
        val SS = 6
        val SSc = 7

        // Lookup tables for switching from one style to another
        val sup = listOf(S, Sc, S, Sc, SS, SSc, SS, SSc)
        val sub = listOf(Sc, Sc, Sc, Sc, SSc, SSc, SSc, SSc)

        // Instances of the different styles
        val styles = listOf(
            Style(D, 0, false),
            Style(Dc, 0, true),
            Style(T, 1, false),
            Style(Tc, 1, true),
            Style(S, 2, false),
            Style(Sc, 2, true),
            Style(SS, 3, false),
            Style(SSc, 3, true)
        )

        val DISPLAY: Style = styles[D]
        val TEXT: Style = styles[T]
    }


    /**
     * Get the style of a superscript given a base in the current style.
     */
    fun sup(): Style {
        return styles[sup[this.id]];
    }

    /**
     * Get the style of a subscript given a base in the current style.
     */
    fun sub(): Style {
        return styles[sub[this.id]];
    }
}
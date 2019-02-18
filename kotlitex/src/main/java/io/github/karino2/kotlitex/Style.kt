package io.github.karino2.kotlitex

// This file is from Style.js
/**
 * This file contains information and classes for the various kinds of styles
 * used in TeX. It provides a generic `Style` class, which holds information
 * about a specific style. It then provides instances of all the different kinds
 * of styles possible, and provides functions to move between them and get
 * information about them.
 */

/**
 * The main style class. Contains a unique id for the style, a size (which is
 * the same for cramped and uncramped version of a style), and a cramped flag.
 */
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
        val _fracNum = listOf(T, Tc, S, Sc, SS, SSc, SS, SSc)
        val _fracDen = listOf(Tc, Tc, Sc, Sc, SSc, SSc, SSc, SSc)
        val _cramp = listOf(Dc, Dc, Tc, Tc, Sc, Sc, SSc, SSc)
        val _text = listOf(D, Dc, T, Tc, T, Tc, T, Tc)


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
        val SCRIPT = styles[S]
        val SCRIPTSCRIPT = styles[SS]
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

    /**
     * Get the style of a fraction numerator given the fraction in the current
     * style.
     */
    fun fracNum(): Style {
        return styles[_fracNum[id]];
    }


    /**
     * Get the style of a fraction denominator given the fraction in the current
     * style.
     */
    fun fracDen(): Style {
        return styles[_fracDen[id]];
    }

    /**
     * Get the cramped version of a style (in particular, cramping a cramped style
     * doesn't change the style).
     */
    fun cramp(): Style {
        return styles[_cramp[this.id]];
    }

    /**
     * Get a text or display version of this style.
     */
    fun text(): Style {
        return styles[_text[this.id]];
    }

    /**
     * Return true if this style is tightly spaced (scriptstyle/scriptscriptstyle)
     */
    /*
    isTight(): boolean {
        return this.size >= 2;
    }
    */
}
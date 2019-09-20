package io.github.karino2.kotlitex

// RenderNode related CssClass.
enum class CssClass {
    accent, accent_body, accent_full, allowbreak, amsrm, base , boldsymbol, brace_left, brace_center, brace_right,
    delimcenter, delimsizing, delimsizinginner, delim_size1, delim_size4,
    enclosing, frac_line, hide_tail, halfarrow_left, halfarrow_right, large_op,
    mathbb, mathbf, mathcal, mathdefault, mathit, mathscr, mbin, mclose, mfrac, minner, mop, mopen, mord, mpunct, mrel, mtight,
    msupsub, mspace, mult, newline, nobreak, nulldelimiter, overlay,
    vlist, vlist_r, vlist_s, vlist_t, vlist_t2, pstrut, op_symbol, op_limits,
    reset_size1, reset_size2, reset_size3, reset_size4, reset_size5, reset_size6,
    reset_size7, reset_size8, reset_size9, reset_size10, reset_size11, root,
    sizing,  size1, size2, size3, size4, size5, size6, size7, size8, size9, size10, size11,
    small_op, sqrt, struct, stretchy, svg_align,
    textbf, textit, textrm, textsf, texttt,
    underline, underline_line,
    EMPTY;

    companion object {
        fun resetClass(size: Int): CssClass {
            return when (size) {
                1 -> CssClass.reset_size1
                2 -> CssClass.reset_size2
                3 -> CssClass.reset_size3
                4 -> CssClass.reset_size4
                5 -> CssClass.reset_size5
                6 -> CssClass.reset_size6
                7 -> CssClass.reset_size7
                8 -> CssClass.reset_size8
                9 -> CssClass.reset_size9
                10 -> CssClass.reset_size10
                11 -> CssClass.reset_size11
                else -> throw Exception("Unknown reset class size: $size")
            }
        }

        fun sizeClass(size: Int) : CssClass {
            return when (size) {
                1 -> CssClass.size1
                2 -> CssClass.size2
                3 -> CssClass.size3
                4 -> CssClass.size4
                5 -> CssClass.size5
                6 -> CssClass.size6
                7 -> CssClass.size7
                8 -> CssClass.size8
                9 -> CssClass.size9
                10 -> CssClass.size10
                11 -> CssClass.size11
                else -> throw Exception("Unknown size class size: $size")
            }
        }

        // Very similar set to mFamily, might be able to merge in some way.
        fun mclass(mclass: String): CssClass {
            return when(mclass) {
                "mord" -> CssClass.mord
                "mbin" -> CssClass.mbin
                "mrel"-> CssClass.mrel
                "mopen"-> CssClass.mopen
                "mclose"-> CssClass.mclose
                "mpunct"-> CssClass.mpunct
                "minner"-> CssClass.minner
                else-> throw Error("No mclass $(mclass) in CssClass::mclass")
            }
        }

        fun mFamily(family: Atoms): CssClass {
            return when (family) {
                Atoms.punct -> CssClass.mpunct
                Atoms.bin -> CssClass.mbin
                Atoms.close -> CssClass.mclose
                Atoms.inner -> CssClass.minner
                Atoms.open -> CssClass.mopen
                Atoms.rel -> CssClass.mrel
            }
        }
    }
}

fun Set<CssClass>.concat(target: Set<CssClass>): MutableSet<CssClass> {
    val newone = target.toList().toMutableSet() // is this really cloned?
    newone.addAll(this)
    return newone.filter { it != CssClass.EMPTY }.toMutableSet()
}

data class CssStyle(
    // It seems always "double + em". may be it better be double
    var height: String? = null,
    var top: String? = null,

    var color: String? = null,
    var marginLeft: String? = null,
    var marginTop: String? = null,
    var marginRight: String? = null,
    var borderBottomWidth: String? = null,
    var minWidth: String? = null,
    var paddingLeft: String? = null,
    var position: String? = null,
    var left: String? = null,
    var width: String? = null
/*
    backgroundColor: string,
    borderColor: string,
    borderRightWidth: string,
    borderTopWidth: string,
    bottom: string,

    verticalAlign: string,

 */
)
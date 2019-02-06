/*
 This file is porting of functions/op.js
*/
package io.github.karino2.kotlitex.functions

import io.github.karino2.kotlitex.*

object FunctionOp {

    data class ElemKern(val elem: RenderNode, val kern: Double)

    // NOTE: Unlike most `renderNodeBuilder`s, this one handles not only "op", but also
    // "supsub" since some of them (like \int) can affect super/subscripting.
    fun renderNodeBuilder(grp: ParseNode /* ParseNode<"supsub"> | ParseNode<NODETYPE> */, options: Options) : RenderNode {
        // Operators are handled in the TeXbook pg. 443-444, rule 13(a).
        var supGroup: ParseNode? = null
        var subGroup: ParseNode? = null
        var hasLimits = false
        val group = if(grp is PNodeSupSub) {
            // If we have limits, supsub will pass us its group to handle. Pull
            // out the superscript and subscript and set the group to the op in
            // its base.
            supGroup = grp.sup;
            subGroup = grp.sub;

            hasLimits = true;
            grp.base as PNodeOp
        } else {
            grp as PNodeOp
        }

        val style = options.style

        // Most operators have a large successor symbol, but these don't.
        val noSuccessor = arrayOf("\\smallint")

        val large = style.size == Style.DISPLAY.size &&
            group.symbol &&
            !noSuccessor.contains(group.name)

        var base = if (group.symbol) {
            // If this is a symbol, create the symbol.
            val fontName = if(large)  "Size2-Regular" else "Size1-Regular"

            var stash = ""
            if (group.name == "\\oiint" || group.name == "\\oiiint") {
                // No font glyphs yet, so use a glyph w/o the oval.
                // TODO: When font glyphs are available, delete this code.
                stash = group.name.substring(1);
                // $FlowFixMe
                group.name = if(stash == "oiint")  "\\iint" else  "\\iiint"
            }

            val base1 = RenderTreeBuilder.makeSymbol(
                group.name, fontName, Mode.MATH, options,
                mutableSetOf(CssClass.mop, CssClass.op_symbol, if(large)  CssClass.large_op else CssClass.small_op));

            // TODO: support oiint and oiint
            /*
            if (stash.isNotEmpty()) {

                // We're in \oiint or \oiiint. Overlay the oval.
                // TODO: When font glyphs are available, delete this code.
                val italic = base1.italic
                val oval = RenderTreeBuilder.staticSvg(stash + "Size"
                        + (if(large)  "2" else "1"), options);
                val base2 = RenderBuilderVList.makeVList(
                    VListParamIndividual(
                        listOf(
                            VListElemAndShift(base1, shift=0.0),
                            VListElemAndShift(oval, shift=if(large) 0.08 else 0.0)
                        )), options)
                group.name = "\\" + stash
                // Is order important?
                // Original code: base2.classes.unshift("mop")
                base2.klasses.add(CssClass.mop)
                base2.italic = italic
                base2
            } else {
                base1
            }
            */
            base1
        } else if (group.body != null) {
            // karino: Is this cast always success?
            // If this is a list, compose that list.
            val inner = RenderTreeBuilder.buildExpression(group.body as List<ParseNode>, options, true);
            if (inner.size == 1 && inner[0] is RNodeSymbol) {
                val sym = inner[0]
                // replace old mclass
                sym.klasses.clear()
                sym.klasses.add(CssClass.mop)
                sym
            } else {
                RenderTreeBuilder.makeSpan(
                    mutableSetOf(CssClass.mop), RenderTreeBuilder.tryCombineChars(inner), options)
            }
        } else {
            // Otherwise, this is a text operator. Build the text from the
            // operator's name.
            // TODO(emily): Add a space in the middle of some of these
            // operators, like \limsup
            val output: MutableList<RenderNode> = group.name.map {
                RenderTreeBuilder.mathsym(it.toString(), group.mode, null, mutableSetOf())
            }.toMutableList()

            RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.mop), output, options);
        }

        // If content of op is a single symbol, shift it vertically.
        val (baseShift, slant) =
            if ((base is RNodeSymbol
                        || group.name == "\\oiint" || group.name == "\\oiiint")
                && group.suppressBaseShift != true) {
                // We suppress the shift of the base of \overset and \underset. Otherwise,
                // shift the symbol so its center lies on the axis (rule 13). It
                // appears that our fonts have the centers of the symbols already
                // almost on the axis, so these numbers are very small. Note we
                // don't actually apply this here, but instead it is used either in
                // the vlist creation or separately when there are no limits.
                Pair( (base.height - base.depth) / 2 -
                        options.fontMetrics.axisHeight,
                    // The slant of the symbol is just its italic correction.
                    if(base is RNodeSymbol) base.italic else 0.0)
            } else Pair(0.0, 0.0)

        if (hasLimits) {
            // IE 8 clips \int if it is in a display: inline-block. We wrap it
            // in a new span so it is an inline, and works.
            base = RenderTreeBuilder.makeSpan(mutableSetOf(), mutableListOf(base))

            // We manually have to handle the superscripts and subscripts. This,
            // aside from the kern calculations, is copied from supsub.
            val sup = supGroup?.let {
                val elem = RenderTreeBuilder.buildGroup(
                    supGroup, options.havingStyle(style.sup()), options);
                ElemKern(
                        elem,
                        Math.max(
                            options.fontMetrics.bigOpSpacing1,
                            options.fontMetrics.bigOpSpacing3 - elem.depth)
                    )

            }

            val sub = subGroup?.let {
                val elem = RenderTreeBuilder.buildGroup(
                        subGroup, options.havingStyle(style.sub()), options);
                ElemKern(elem,
                                Math.max(
                                        options.fontMetrics.bigOpSpacing2,
                                        options.fontMetrics.bigOpSpacing4 - elem.height)
                            )
            }

            // Build the final group as a vlist of the possible subscript, base,
            // and possible superscript.
            val finalGroup =
            if (sup != null && sub != null) {
                val bottom = options.fontMetrics.bigOpSpacing5 +
                        sub.elem.height + sub.elem.depth +
                        sub.kern +
                        base.depth + baseShift

                RenderBuilderVList.makeVList(
                    VListParamPositioned(
                        PositionType.Bottom,
                        bottom,
                        listOf(
                            VListKern(options.fontMetrics.bigOpSpacing5),
                            VListElem(sub.elem,  "${-slant}em"),
                            VListKern(sub.kern),
                            VListElem(base),
                            VListKern(sup.kern),
                            VListElem(sup.elem, "${slant}em"),
                            VListKern(options.fontMetrics.bigOpSpacing5)
                            )
                    ), options)
            } else if (sub != null) {
                val top = base.height - baseShift;

                // Shift the limits by the slant of the symbol. Note
                // that we are supposed to shift the limits by 1/2 of the slant,
                // but since we are centering the limits adding a full slant of
                // margin will shift by 1/2 that.
                RenderBuilderVList.makeVList(
                    VListParamPositioned(
                        PositionType.Top,
                        top,
                        listOf(
                            VListKern(options.fontMetrics.bigOpSpacing5),
                            VListElem(sub.elem, "${-slant}em"),
                            VListKern(sub.kern),
                            VListElem(base)
                            )
                    ), options)
            } else if (sup != null) {
                val bottom = base.depth + baseShift;

                RenderBuilderVList.makeVList(
                    VListParamPositioned(
                        PositionType.Bottom,
                        bottom,
                        listOf(
                            VListElem(base),
                            VListKern(sup.kern),
                            VListElem(sup.elem, "${slant}em"),
                            VListKern(options.fontMetrics.bigOpSpacing5)
                            )
                    ), options);
            } else {
                // This case probably shouldn't occur (this would mean the
                // supsub was sending us a group with no superscript or
                // subscript) but be safe.
                return base;
            }

            return RenderTreeBuilder.makeSpan(
                mutableSetOf(CssClass.mop, CssClass.op_limits), mutableListOf(finalGroup), options)
        } else {
            if (baseShift != 0.0) {
                base.style.position = "relative";
                base.style.top = "${baseShift}em";
            }

            return base;
        }

    }

    /*
    TODO: support singleCharBigOps
    const singleCharBigOps: {[string]: string} = {
    "\u220F": "\\prod",
    "\u2210": "\\coprod",
    "\u2211": "\\sum",
    "\u22c0": "\\bigwedge",
    "\u22c1": "\\bigvee",
    "\u22c2": "\\bigcap",
    "\u22c3": "\\bigcap",
    "\u2a00": "\\bigodot",
    "\u2a01": "\\bigoplus",
    "\u2a02": "\\bigotimes",
    "\u2a04": "\\biguplus",
    "\u2a06": "\\bigsqcup",
};

     */

    fun defineAll() {
        LatexFunctions.defineFunction(
            FunctionSpec("op", 0),
            listOf(
                "\\coprod", "\\bigvee", "\\bigwedge", "\\biguplus", "\\bigcap",
                "\\bigcup", "\\intop", "\\prod", "\\sum", "\\bigotimes",
                "\\bigoplus", "\\bigodot", "\\bigsqcup", "\\smallint", "\u220F",
                "\u2210", "\u2211", "\u22c0", "\u22c1", "\u22c2", "\u22c3", "\u2a00",
                "\u2a01", "\u2a02", "\u2a04", "\u2a06"
            ),
            { context: FunctionContext, _ /* args */: List<ParseNode>, _ /* optArgs */ : List<ParseNode?> ->
                val parser = context.parser
                val fName = context.funcName
                // TODO: support funcName.length == 1 case.
                /*
                    if (fName.length === 1) {
                        fName = singleCharBigOps[fName];
                    }
                 */
                PNodeOp(
                    parser.mode,
                    null,
                    true,
                    null,
                    null,
                     true,
                    fName,
                    null
                )
            },
            FunctionOp::renderNodeBuilder
        )
    }

}
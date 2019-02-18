package io.github.karino2.kotlitex

import io.github.karino2.kotlitex.functions.FunctionOp
import java.lang.IllegalArgumentException


typealias RenderHandler = (ParseNode, Options)->RenderNode
object RenderBuilderSupsub {


    fun htmlBuilderDelegate(group: PNodeSupSub, options: Options) :  RenderHandler?{
        val base = group.base ?: return null
        when (base) {
            is PNodeOp -> {
                // Operators handle supsubs differently when they have limits
                // (e.g. `\displaystyle\sum_2^3`)
                val delegate = base.limits &&
                (options.style.size == Style.DISPLAY.size ||
                        base.alwaysHandleSupSub == true)
                if(delegate)
                    return FunctionOp::renderNodeBuilder
                return null
            }
            /*
            TODO:
            is PNodeAccent -> {
                return utils.isCharacterBox(base.base) ? accent.htmlBuilder : null;
            }
            is PNodeHorizBrace -> {
                const isSup = !group.sub;
                return isSup === base.isOver ? horizBrace.htmlBuilder : null;

            }
            */
            else -> return null

        }
    }

    // Super scripts and subscripts, whose precise placement can depend on other
    // functions that precede them.
    fun makeSupsub(group: ParseNode, options: Options) : RenderNode {
        if(group !is PNodeSupSub) {
            throw IllegalArgumentException("unexpected type in makeSupsub.")
        }
        // Superscript and subscripts are handled in the TeXbook on page
        // 445-446, rules 18(a-f).

        // Here is where we defer to the inner group if it should handle
        // superscripts and subscripts itself.
        val builderDelegate = htmlBuilderDelegate(group, options);
        if (builderDelegate != null) {
            return builderDelegate(group, options);
        }

        val valueBase = group.base
        val valueSup = group.sup
        val valueSub = group.sub

        val base = RenderTreeBuilder.buildGroup(valueBase, options)
        var supm : RenderNode? = null
        var subm :  RenderNode? = null

        val metrics = options.fontMetrics

        // Rule 18a
        var supShift = 0.0
        var subShift = 0.0

        // TODO:
        // val isCharacterBox = valueBase != null && utils.isCharacterBox(valueBase);
        val isCharacterBox = true

        if (valueSup != null) {
            val newOptions = options.havingStyle(options.style.sup());
            supm = RenderTreeBuilder.buildGroup(valueSup, newOptions, options);
            if (!isCharacterBox) {
                supShift = base.height - newOptions.fontMetrics.supDrop *
                        newOptions.sizeMultiplier / options.sizeMultiplier
            }
        }

        if (valueSub != null) {
            val newOptions = options.havingStyle(options.style.sub());
            subm = RenderTreeBuilder.buildGroup(valueSub, newOptions, options);
            if (!isCharacterBox) {
                subShift = base.depth + newOptions.fontMetrics.subDrop *
                        newOptions.sizeMultiplier / options.sizeMultiplier;
            }
        }

        // Rule 18c
        var minSupShift = if (options.style === Style.DISPLAY) {
            metrics.sup1;
        } else if (options.style.cramped) {
            metrics.sup3;
        } else {
            metrics.sup2;
        }

        // scriptspace is a font-size-independent size, so scale it
        // appropriately for use as the marginRight.
        val multiplier = options.sizeMultiplier;
        val marginRight = ((0.5 / metrics.ptPerEm) / multiplier).toString() + "em";

        var marginLeft : String? = null
        if (subm != null) {
            if(base is RNodeSymbol) {
                marginLeft = (-base.italic).toString() + "em"
            }

            // Subscripts shouldn't be shifted by the base's italic correction.
            // Account for that by shifting the subscript back the appropriate
            // amount. Note we only do this when the base is a single symbol.
            /* TODO: oiint. base does not have italic in our type system.
            if(group.base != null && group.base is PNodeOp && group.base.name != null) {
                if (group.base.name === "\\oiint" || group.base.name === "\\oiiint") {
                    val base1: ParseNode? = group.base
                    marginLeft = (-base.italic).toString() + "em";

                }
            }
            */
        }

        val supsub = if (supm != null && subm != null) {
            supShift = maxOf(
                supShift, minSupShift, supm.depth + 0.25 * metrics.xHeight);
            subShift = Math.max(subShift, metrics.sub2);

            val ruleWidth = metrics.defaultRuleThickness;

            // Rule 18e
            val maxWidth = 4 * ruleWidth;
            if ((supShift - supm.depth) - (subm.height - subShift) < maxWidth) {
                subShift = maxWidth - (supShift - supm.depth) + subm.height;
                val psi = 0.8 * metrics.xHeight - (supShift - supm.depth);
                if (psi > 0) {
                    supShift += psi;
                    subShift -= psi;
                }
            }

            /*
            (elem: RenderNode,
                        marginLeft: String?,
                        marginRight: String?,
                        wrapperClasses: MutableSet<CssClass>,
                        wrapperStyle: CssStyle, val shift: Int
             */
            val vlistElem = mutableListOf(
                VListElemAndShift(subm, marginLeft, marginRight, mutableSetOf<CssClass>(), CssStyle(), subShift),
                VListElemAndShift(supm, marginLeft, marginRight, mutableSetOf<CssClass>(), CssStyle(), -supShift)
            )

            RenderBuilderVList.makeVList(VListParamIndividual(vlistElem), options);
        } else if (subm != null) {
            // Rule 18b
            subShift = maxOf(
                subShift, metrics.sub1,
                subm.height - 0.8 * metrics.xHeight)

            val vlistElem = mutableListOf(
                VListElem(subm, marginLeft, marginRight)
            )

            RenderBuilderVList.makeVList(
                VListParamPositioned(PositionType.Shift, subShift, vlistElem),
                options)
        } else if (supm != null) {
            // Rule 18c, d
            supShift = maxOf(supShift, minSupShift,
                supm.depth + 0.25 * metrics.xHeight);

            RenderBuilderVList.makeVList(
                VListParamPositioned(PositionType.Shift,-supShift,
                    mutableListOf(VListElem(supm, marginRight = marginRight))), options)
        } else {
            throw Error("supsub must have either sup or sub.");
        }

        // Wrap the supsub vlist in a span.msupsub to reset text-align.
        val mclassCand = RenderTreeBuilder.getTypeOfDomTree(base, "right")
        val mclass = if(mclassCand != CssClass.EMPTY) mclassCand else CssClass.mord
        return RenderTreeBuilder.makeSpan(
            mutableSetOf(mclass),
            mutableListOf(base, RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.msupsub), mutableListOf(supsub))),
            options
        )

    }

}
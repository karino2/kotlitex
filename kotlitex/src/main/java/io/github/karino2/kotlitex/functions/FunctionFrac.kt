package io.github.karino2.kotlitex.functions

import io.github.karino2.kotlitex.*
import io.github.karino2.kotlitex.RenderTreeBuilder.calculateSize
import java.lang.IllegalArgumentException

/*
  This file is the port of functions/genfrac.js
 */


object FunctionFrac {
    fun renderNodeBuilder(group: ParseNode, options: Options) : RNodeSpan {
        if(group !is PNodeGenFrac) {
            throw IllegalArgumentException("unexpected type in frac RNodeBuilder.")
        }

        // Fractions are handled in the TeXbook on pages 444-445, rules 15(a-e).
        // Figure out what style this fraction should be in based on the
        // function used
        var style = options.style;
        if (group.size == SizeStyle.DISPLAY) {
            style = Style.DISPLAY;
        } else if (group.size == SizeStyle.TEXT &&
            style.size == Style.DISPLAY.size) {
            // We're in a \tfrac but incoming style is displaystyle, so:
            style = Style.TEXT;
        } else if (group.size == SizeStyle.SCRIPT) {
            style = Style.SCRIPT;
        } else if (group.size == SizeStyle.SCRIPTSCRIPT) {
            style = Style.SCRIPTSCRIPT;
        }

        val nstyle = style.fracNum();
        val dstyle = style.fracDen();
        var newOptions = options.havingStyle(nstyle);
        val numerm = RenderTreeBuilder.buildGroup(group.numer, newOptions, options)

        if (group.continued) {
            // \cfrac inserts a \strut into the numerator.
            // Get \strut dimensions from TeXbook page 353.
            val hStrut = 8.5 / options.fontMetrics.ptPerEm;
            val dStrut = 3.5 / options.fontMetrics.ptPerEm;
            numerm.height = if(numerm.height < hStrut)  hStrut else numerm.height
            numerm.depth = if(numerm.depth < dStrut)  dStrut else numerm.depth
        }

        newOptions = options.havingStyle(dstyle);
        val denomm = RenderTreeBuilder.buildGroup(group.denom, newOptions, options);

        val (rule, ruleWidth, ruleSpacing) =
        if (group.hasBarLine) {
            val rule = if (group.barSize != null) {
                val ruleWidth2 = calculateSize(group.barSize, options);
                RenderTreeBuilder.makeLineSpan(
                    CssClass.frac_line,
                    options,
                    ruleWidth2
                )
            } else {
                RenderTreeBuilder.makeLineSpan(
                    CssClass.frac_line,
                    options
                )
            }
            Triple(rule, rule.height, rule.height)
        } else {
            Triple(null, 0.0, options.fontMetrics.defaultRuleThickness)
        }

        // Rule 15b
        /*
        let numShift;
        let clearance;
        let denomShift;
        */
        var (numShift, clearance, denomShift) =
        if (style.size == Style.DISPLAY.size) {
            Triple(options.fontMetrics.num1,
                if (ruleWidth > 0) {
                    3 * ruleSpacing;
                } else {
                    7 * ruleSpacing;
                },
                 options.fontMetrics.denom1)
        } else {
            val (n, c) =
            if (ruleWidth > 0) {
                Pair(options.fontMetrics.num2, ruleSpacing)
            } else {
                Pair(options.fontMetrics.num3, 3 * ruleSpacing)
            }
            Triple(n, c, options.fontMetrics.denom2)
        }

        val frac = if (rule == null) {
            // Rule 15c
            val candidateClearance =
            (numShift - numerm.depth) - (denomm.height - denomShift);
            if (candidateClearance < clearance) {
                numShift += 0.5 * (clearance - candidateClearance);
                denomShift += 0.5 * (clearance - candidateClearance);
            }

            RenderBuilderVList.makeVList(
                VListParamIndividual(
                    mutableListOf(
                        VListElemAndShift(denomm, shift = denomShift),
                        VListElemAndShift(numerm, shift = -numShift)
                    )
                ), options
            )
        } else {
            // Rule 15d
            val axisHeight = options.fontMetrics.axisHeight;

            if ((numShift - numerm.depth) - (axisHeight + 0.5 * ruleWidth) <
                clearance) {
                numShift +=
                        clearance - ((numShift - numerm.depth) -
                        (axisHeight + 0.5 * ruleWidth));
            }

            if ((axisHeight - 0.5 * ruleWidth) - (denomm.height - denomShift) <
                clearance) {
                denomShift +=
                        clearance - ((axisHeight - 0.5 * ruleWidth) -
                        (denomm.height - denomShift));
            }

            val midShift = -(axisHeight - 0.5 * ruleWidth)

            RenderBuilderVList.makeVList(
                VListParamIndividual(
                    mutableListOf(
                        VListElemAndShift(denomm, shift = denomShift),
                        VListElemAndShift(rule, shift = midShift),
                        VListElemAndShift(numerm, shift = -numShift)
                    )
                ), options
            )
        }

        // Since we manually change the style sometimes (with \dfrac or \tfrac),
        // account for the possible size change here.
        newOptions = options.havingStyle(style);
        frac.height *= newOptions.sizeMultiplier / options.sizeMultiplier;
        frac.depth *= newOptions.sizeMultiplier / options.sizeMultiplier;

        // Rule 15e
        val delimSize =
        if (style.size == Style.DISPLAY.size) {
            options.fontMetrics.delim1
        } else {
            options.fontMetrics.delim2
        }

        val leftDelim =
        if (group.leftDelim == null) {
            RenderTreeBuilder.makeNullDelimiter(
                options,
                mutableSetOf(CssClass.mopen)
            );
        } else {
            RenderBuilderDelimiter.makeCustomSizedDelim(
                group.leftDelim, delimSize, true,
                options.havingStyle(style), group.mode, mutableSetOf(CssClass.mopen)
            );
        }

        val rightDelim =
        if (group.continued) {
            RenderTreeBuilder.makeSpan() // zero width for \cfrac
        } else if (group.rightDelim == null) {
            RenderTreeBuilder.makeNullDelimiter(
                options,
                mutableSetOf(CssClass.mclose)
            );
        } else {
            RenderBuilderDelimiter.makeCustomSizedDelim(
                group.rightDelim, delimSize, true,
                options.havingStyle(style), group.mode, mutableSetOf(CssClass.mclose)
            );
        }

        return RenderTreeBuilder.makeSpan(
            mutableSetOf(CssClass.mord).concat(newOptions.sizingClasses(options)),
            mutableListOf(
                leftDelim,
                RenderTreeBuilder.makeSpan(
                    mutableSetOf(CssClass.mfrac),
                    mutableListOf(frac)
                ),
                rightDelim
            ),
            options
        );
    }

    fun defineAll() {
        LatexFunctions.defineFunction(
            FunctionSpec(
                "genfrac",
                2,
                null,
                2
            ),
            listOf(
                "\\cfrac", "\\dfrac", "\\frac", "\\tfrac",
                "\\dbinom", "\\binom", "\\tbinom",
                "\\\\atopfrac", // can’t be entered directly
                "\\\\bracefrac", "\\\\brackfrac"   // ditto
            ),
            { context: FunctionContext, args: List<ParseNode>, _: List<ParseNode?> ->
                val (funcName, parser) = context
                val numer = args[0]
                val denom = args[1]
                var hasBarLine: Boolean
                var leftDelim: String? = null
                var rightDelim: String? = null
                var size = SizeStyle.AUTO

                when (funcName) {
                    "\\cfrac", "\\dfrac", "\\frac", "\\tfrac" -> hasBarLine = true;
                    "\\\\atopfrac" -> hasBarLine = false

                    "\\dbinom", "\\binom", "\\tbinom" -> {
                        hasBarLine = false
                        leftDelim = "("
                        rightDelim = ")"
                    }
                    "\\\\bracefrac" -> {
                        hasBarLine = false
                        leftDelim = "\\{"
                        rightDelim = "\\}"
                    }
                    "\\\\brackfrac" -> {
                        hasBarLine = false
                        leftDelim = "["
                        rightDelim = "]"
                    }
                    else -> throw Error("Unrecognized genfrac command");
                }

                when (funcName) {
                    "\\cfrac", "\\dfrac", "\\dbinom" -> size = SizeStyle.DISPLAY
                    "\\tfrac", "\\tbinom" -> size = SizeStyle.TEXT
                }

                PNodeGenFrac(
                    parser.mode,
                    null,
                    funcName == "\\cfrac",
                    numer,
                    denom,
                    hasBarLine,
                    leftDelim,
                    rightDelim,
                    size,
                    null
                )
            },
            FunctionFrac::renderNodeBuilder
        )

        // temporary put here.
        LatexFunctions.defineFunctionBuilder("ordgroup") { group, options ->
            if (group !is PNodeOrdGroup)
                throw IllegalArgumentException("unexpected type in ordgroup RNodeBuilder.")

            RenderTreeBuilder.makeSpan(
                mutableSetOf(CssClass.mord),
                RenderTreeBuilder.buildExpression(group.body, options, true).toMutableList(),
                options
            )
        }


    }
}
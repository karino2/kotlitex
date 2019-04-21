package io.github.karino2.kotlitex.functions

import io.github.karino2.kotlitex.*

object FunctionAccent {
    val NON_STRETCHY_ACCENT_REGEX =
        setOf(
            "\\acute", "\\grave", "\\ddot", "\\tilde", "\\bar",
            "\\breve", "\\check", "\\hat", "\\vec", "\\dot", "\\mathring"
        )

    // utils.getBaseElem
    /**
     * Sometimes we want to pull out the innermost element of a group. In most
     * cases, this will just be the group itself, but when ordgroups and colors have
     * a single element, we want to pull that out.
     */
    fun getBaseElem(group: ParseNode): ParseNode {
        if (group is PNodeOrdGroup) {
            if (group.body.size == 1) {
                return getBaseElem(group.body[0]);
            } else {
                return group
            }
        } else if (group is PNodeColor) {
            if (group.body.size == 1) {
                return getBaseElem(group.body[0]);
            } else {
                return group;
            }
        } else if (group is PNodeFont) {
            return getBaseElem(group.body);
        } else {
            return group;
        }
    }

    // utils.isCharacterBox
    /**
     * TeXbook algorithms often reference "character boxes", which are simply groups
     * with a single character in them. To decide if something is a character box,
     * we find its innermost group, and see if it is a single character.
     */
    fun isCharacterBox(group: ParseNode): Boolean {
        val baseElem = getBaseElem(group);

        // These are all they types of groups which hold single characters
        return when (baseElem) {
            is PNodeMathOrd -> true
            is PNodeTextOrd -> true
            is PNodeAtom -> true
            else -> false
        }
    }


    // NOTE: Unlike most `renderNodeBuilder`s, this one handles not only "accent", but
    // also "supsub" since an accent can affect super/subscripting.
    fun renderNodeBuilder(grp: ParseNode, options: Options): RenderNode {
        // Accents are handled in the TeXbook pg. 443, rule 12.
        val (group, base, supSubGroup) = if (grp is PNodeSupSub) {
            val supSub = grp as PNodeSupSub
            // If our base is a character box, and we have superscripts and
            // subscripts, the supsub will defer to us. In particular, we want
            // to attach the superscripts and subscripts to the inner body (so
            // that the position of the superscripts and subscripts won't be
            // affected by the height of the accent). We accomplish this by
            // sticking the base of the accent into the base of the supsub, and
            // rendering that, while keeping track of where the accent is.

            // The real accent group is the base of the supsub group
            val groupBase = supSub.base as PNodeAccent
            // The character box is the base of the accent group
            val baseBase = groupBase.base;
            // Stick the character box into the base of the supsub group
            supSub.base = baseBase;

            // Rerender the supsub group with its new base, and store that
            // result.
            val supSubGroupTmp = RenderTreeBuilder.buildGroup(supSub, options) as RNodeSpan

            // reset original base
            supSub.base = groupBase
            Triple(groupBase, baseBase, supSubGroupTmp)
        } else {
            Triple(grp as PNodeAccent, grp.base, null)
        }


        // Build the base group
        val body = RenderTreeBuilder.buildGroup(base, options.havingCrampedStyle())

        // Does the accent need to shift for the skew of a character?
        val mustShift = group.isShifty && isCharacterBox(base);

        // Calculate the skew of the accent. This is based on the line "If the
        // nucleus is not a single character, let s = 0; otherwise set s to the
        // kern amount for the nucleus followed by the \skewchar of its font."
        // Note that our skew metrics are just the kern between each character
        // and the skewchar.
        val skew = if (mustShift) {
            // If the base is a character box, then we want the skew of the
            // innermost character. To do that, we find the innermost character:
            val baseChar = getBaseElem(base);
            // Then, we render its group to get the symbol inside it
            val baseGroup = RenderTreeBuilder.buildGroup(baseChar, options.havingCrampedStyle());
            // Finally, we pull the skew off of the symbol.
            (baseGroup as RNodeSymbol).skew
            // Note that we now throw away baseGroup, because the layers we
            // removed with getBaseElem might contain things like \color which
            // we can't get rid of.
            // TODO(emily): Find a better way to get the skew
        } else {
            0.0
        }

        // calculate the amount of space between the body and the accent
        var clearance = Math.min(
            body.height,
            options.fontMetrics.xHeight
        );

        // Build the accent
        val accentBody = if (!group.isStretchy) {
            val (accent, width) =
                if (group.label === "\\vec") {
                    // Before version 0.9, \vec used the combining font glyph U+20D7.
                    // But browsers, especially Safari, are not consistent in how they
                    // render combining characters when not preceded by a character.
                    // So now we use an SVG.
                    // If Safari reforms, we should consider reverting to the glyph.
                    Pair(RenderTreeBuilder.staticPath("vec", options), RenderTreeBuilder.pathData.getValue("vec").width)
                } else {
                    val acc = RenderTreeBuilder.makeSymbol(
                        group.label, "Main-Regular", group.mode, options
                    );
                    // Remove the italic correction of the accent, because it only serves to
                    // shift the accent over to a place we don't want.
                    acc.italic = 0.0
                    Pair(acc, acc.width)
                }

            val localAccentBody = RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.accent_body), mutableListOf(accent));

            // "Full" accents expand the width of the resulting symbol to be
            // at least the width of the accent, and overlap directly onto the
            // character without any vertical offset.
            val accentFull = (group.label == "\\textcircled");
            if (accentFull) {
                localAccentBody.klasses.add(CssClass.accent_full)
                clearance = body.height;
            }

            // Shift the accent over by the skew.
            var left = skew

            // CSS defines `.katex .accent .accent-body:not(.accent-full) { width: 0 }`
            // so that the accent doesn't contribute to the bounding box.
            // We need to shift the character by its width (effectively half
            // its width) to compensate.
            if (!accentFull) {
                left -= width / 2;
            }

            localAccentBody.style.left = "${left}em";

            // \textcircled uses the \bigcirc glyph, so it needs some
            // vertical adjustment to match LaTeX.
            if (group.label == "\\textcircled") {
                localAccentBody.style.top = ".2em";
            }

            RenderBuilderVList.makeVList(
                VListParamFirstBaseLine(
                    listOf(
                        VListElem(body),
                        VListKern(-clearance),
                        VListElem(localAccentBody)
                    )
                ), options
            );

        } else {
            val localAccentBody = Stretchy.pathSpan(group, options);

            RenderBuilderVList.makeVList(
                VListParamFirstBaseLine(
                    listOf(
                        VListElem(body),
                        VListElem(
                            localAccentBody,
                            wrapperClasses = mutableSetOf(CssClass.svg_align),
                            wrapperStyle = CssStyle() // TODO: support skew need calc... if(skew > 0.0) { CssStyle(width=)} else { CssStyle() }
                            /*
                            wrapperStyle: skew > 0 ? {
                                width: `calc(100% - ${2 * skew}em)`,
                                marginLeft: `${(2 * skew)}em`,
                              } : undefined
                         */
                        )
                    )
                ), options
            )
        }

        val accentWrap =
            RNodePathSpan(mutableListOf(accentBody), klasses = mutableSetOf(CssClass.mord, CssClass.accent))

        if (supSubGroup != null) {
            // Here, we replace the "base" child of the supsub with our newly
            // generated accent.
            supSubGroup.children[0] = accentWrap

            // Since we don't rerun the height calculation after replacing the
            // accent, we manually recalculate height.
            supSubGroup.height = Math.max(accentWrap.height, supSubGroup.height)

            // Accents should always be ords, even when their innards are not.
            // JS code: supSubGroup.classes[0] = "mord";
            // I clear all other element. it's different from just replace first element.
            // I use set instead of list for klasses, so there are no order in our code.
            // I think real intention of assin to first element is always like this, but may be I'm wrong.
            supSubGroup.klasses.clear()
            supSubGroup.klasses.add(CssClass.mord)

            return supSubGroup
        } else {
            return accentWrap
        }
    }

    fun defineAll() {
        LatexFunctions.defineFunction(
            FunctionSpec(
                "accent",
                1
            ),
            listOf(
                "\\acute", "\\grave", "\\ddot", "\\tilde", "\\bar", "\\breve",
                "\\check", "\\hat", "\\vec", "\\dot", "\\mathring", "\\widecheck",
                "\\widehat", "\\widetilde", "\\overrightarrow", "\\overleftarrow",
                "\\Overrightarrow", "\\overleftrightarrow", "\\overgroup",
                "\\overlinesegment", "\\overleftharpoon", "\\overrightharpoon"
            ),
            { context: FunctionContext, args: List<ParseNode>, _ /* optArgs */: List<ParseNode?> ->
                val base = args[0]

                val isStretchy = !NON_STRETCHY_ACCENT_REGEX.contains(context.funcName)
                val isShifty = !isStretchy ||
                        context.funcName == "\\widehat" ||
                        context.funcName == "\\widetilde" ||
                        context.funcName == "\\widecheck"

                PNodeAccent(
                    context.parser.mode,
                    loc = null,
                    label = context.funcName,
                    isStretchy = isStretchy,
                    isShifty = isShifty,
                    base = base
                )
            },
            FunctionAccent::renderNodeBuilder
        )
        // Text-mode accents
        LatexFunctions.defineFunction(
            FunctionSpec(
                "accent",
                1,
                allowedInText = true,
                allowedInMath = false
            ),
            listOf(
                "\\'", "\\`", "\\^", "\\~", "\\=", "\\u", "\\.", "\\\"",
                "\\r", "\\H", "\\v", "\\textcircled"
            ),
            { context: FunctionContext, args: List<ParseNode>, _ /* optArgs */: List<ParseNode?> ->
                val base = args[0];

                PNodeAccent(
                    context.parser.mode,
                    loc = null,
                    label = context.funcName,
                    isStretchy = false,
                    isShifty = true,
                    base = base
                )
            },
            FunctionAccent::renderNodeBuilder
        )
    }
}
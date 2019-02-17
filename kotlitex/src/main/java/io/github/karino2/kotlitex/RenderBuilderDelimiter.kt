package io.github.karino2.kotlitex

/**
 * This file deals with creating delimiters of various sizes. The TeXbook
 * discusses these routines on page 441-442, in the "Another subroutine sets box
 * x to a specified variable delimiter" paragraph.
 *
 * There are three main routines here. `makeSmallDelim` makes a delimiter in the
 * normal font, but in either text, script, or scriptscript style.
 * `makeLargeDelim` makes a delimiter in textstyle, but in one of the Size1,
 * Size2, Size3, or Size4 fonts. `makeStackedDelim` makes a delimiter out of
 * smaller pieces that are stacked on top of one another.
 *
 * The functions take a parameter `center`, which determines if the delimiter
 * should be centered around the axis.
 *
 * Then, there are three exposed functions. `sizedDelim` makes a delimiter in
 * one of the given sizes. This is used for things like `\bigl`.
 * `customSizedDelim` makes a delimiter with a given total height+depth. It is
 * called in places like `\sqrt`. `leftRightDelim` makes an appropriate
 * delimiter which surrounds an expression of a given height an depth. It is
 * used in `\left` and `\right`.
 */


/**
 * There are three different sequences of delimiter sizes that the delimiters
 * follow depending on the kind of delimiter. This is used when creating custom
 * sized delimiters to decide whether to create a small, large, or stacked
 * delimiter.
 *
 * In real TeX, these sequences aren't explicitly defined, but are instead
 * defined inside the font metrics. Since there are only three sequences that
 * are possible for the delimiters that TeX defines, it is easier to just encode
 * them explicitly here.
 */

sealed class  Delimiter

data class SmallDelimiter(val style: Style) : Delimiter()

enum class DelimiterSize(val size: Int) {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4)
}
data class LargeDelimiter(val size: DelimiterSize) : Delimiter()
object StackDelimiter: Delimiter()

object RenderBuilderDelimiter {

    // There are three kinds of delimiters, delimiters that stack when they become
    // too large
    val stackLargeDelimiters = listOf(
    "(", "\\lparen", ")", "\\rparen",
    "[", "\\lbrack", "]", "\\rbrack",
    "\\{", "\\lbrace", "\\}", "\\rbrace",
    "\\lfloor", "\\rfloor", "\u230a", "\u230b",
    "\\lceil", "\\rceil", "\u2308", "\u2309",
    "\\surd"
    )

    // delimiters that always stack
    val stackAlwaysDelimiters = listOf(
    "\\uparrow", "\\downarrow", "\\updownarrow",
    "\\Uparrow", "\\Downarrow", "\\Updownarrow",
    "|", "\\|", "\\vert", "\\Vert",
    "\\lvert", "\\rvert", "\\lVert", "\\rVert",
    "\\lgroup", "\\rgroup", "\u27ee", "\u27ef",
    "\\lmoustache", "\\rmoustache", "\u23b0", "\u23b1"
        )

    // and delimiters that never stack
    val stackNeverDelimiters = listOf(
    "<", ">", "\\langle", "\\rangle", "/", "\\backslash", "\\lt", "\\gt")

    // Metrics of the different sizes. Found by looking at TeX's output of
    // $\bigl| // \Bigl| \biggl| \Biggl| \showlists$
    // Used to create stacked delimiters of appropriate sizes in makeSizedDelim.
    val sizeToMaxHeight = listOf(0.0, 1.2, 1.8, 2.4, 3.0)



    // Delimiters that never stack try small delimiters and large delimiters only
    val stackNeverDelimiterSequence = listOf(
        SmallDelimiter(Style.SCRIPTSCRIPT),
        SmallDelimiter(Style.SCRIPT),
        SmallDelimiter(Style.TEXT),
        LargeDelimiter(DelimiterSize.ONE),
        LargeDelimiter(DelimiterSize.TWO),
        LargeDelimiter(DelimiterSize.THREE),
        LargeDelimiter(DelimiterSize.FOUR)
    )

// Delimiters that always stack try the small delimiters first, then stack
    val stackAlwaysDelimiterSequence =  listOf(
    SmallDelimiter(Style.SCRIPTSCRIPT),
    SmallDelimiter(Style.SCRIPT),
    SmallDelimiter(Style.TEXT),
    StackDelimiter)

// Delimiters that stack when large try the small and then large delimiters, and
// stack afterwards
    val stackLargeDelimiterSequence = listOf(
    SmallDelimiter(Style.SCRIPTSCRIPT),
    SmallDelimiter(Style.SCRIPT),
    SmallDelimiter(Style.TEXT),
    LargeDelimiter(DelimiterSize.ONE),
    LargeDelimiter(DelimiterSize.TWO),
    LargeDelimiter(DelimiterSize.THREE),
    LargeDelimiter(DelimiterSize.FOUR),
    StackDelimiter)

    // All surds have 0.08em padding above the viniculum inside the SVG.
    // That keeps browser span height rounding error from pinching the line.
    val vbPad = 80   // padding above the surd, measured inside the viewBox.
    val emPad = 0.08 // padding, in ems, measured in the document.


    /**
     * Get the metrics for a given symbol and font, after transformation (i.e.
     * after following replacement from symbols.js)
     */
    fun getMetrics(
        symbol: String,
        font: String,
        mode: Mode
    ): CharacterMetrics {
        val replace = Symbols.mathMap[symbol]?.replace ?: symbol
        return Symbols.getCharacterMetrics(replace, font, mode) ?:
            throw Error("Unsupported symbol $symbol and font size $font.");
    };


    /**
     * Get the font used in a delimiter based on what kind of delimiter it is.
     * TODO(#963) Use more specific font family return type once that is introduced.
     */
    fun delimTypeToFont(type: Delimiter): String {
        return when(type) {
            is SmallDelimiter-> "Main-Regular"
            is LargeDelimiter -> "Size${type.size.size}-Regular"
            is StackDelimiter ->"Size4-Regular"
        }
    }

    /**
     * Traverse a sequence of types of delimiters to decide what kind of delimiter
     * should be used to create a delimiter of the given height+depth.
     */
    fun traverseSequence(
        delim: String,
        height: Double,
        sequence: List<Delimiter>,
        options: Options
    ): Delimiter {
        // Here, we choose the index we should start at in the sequences. In smaller
        // sizes (which correspond to larger numbers in style.size) we start earlier
        // in the sequence. Thus, scriptscript starts at index 3-3=0, script starts
        // at index 3-2=1, text starts at 3-1=2, and display starts at min(2,3-0)=2
        val start = Math.min(2, 3 - options.style.size);
        for(seq in sequence.drop(start)) {
            if (seq is StackDelimiter) {
                // This is always the last delimiter, so we just break the loop now.
                break
            }

            val metrics = getMetrics(delim, delimTypeToFont(seq), Mode.MATH)
            var heightDepth = metrics.height + metrics.depth

            // Small delimiters are scaled down versions of the same font, so we
            // account for the style change size.

            if (seq is SmallDelimiter) {
                val newOptions = options.havingBaseStyle(seq.style);
                heightDepth *= newOptions.sizeMultiplier;
            }

            // Check if the delimiter at this size works for the given height.
            if (heightDepth > height) {
                return seq
            }
        }

        // If we reached the end of the sequence, return the last sequence element.
        return sequence.last()
    }


    /**
     * Puts a delimiter span in a given style, and adds appropriate height, depth,
     * and maxFontSizes.
     */
    fun styleWrap(
        delim: RenderNode,
        toStyle: Style,
        options: Options,
        classes: MutableSet<CssClass>
        ): RNodeSpan {
        val newOptions = options.havingBaseStyle(toStyle);

        val span = RenderTreeBuilder.makeSpan(
                classes.concat(newOptions.sizingClasses(options)),
        mutableListOf(delim), options)

        val delimSizeMultiplier =
        newOptions.sizeMultiplier / options.sizeMultiplier;
        span.height *= delimSizeMultiplier;
        span.depth *= delimSizeMultiplier;
        span.maxFontSize = newOptions.sizeMultiplier;

        return span;
    }

    fun centerSpan(
        span: RNodeSpan,
        options: Options,
        style: Style
    ) {
        val newOptions = options.havingBaseStyle(style);
        val shift =
        (1 - options.sizeMultiplier / newOptions.sizeMultiplier) *
                options.fontMetrics.axisHeight

        span.klasses.add(CssClass.delimcenter)
        span.style.top = "${shift}em";
        span.height -= shift
        span.depth += shift
    }


    /**
     * Makes a small delimiter. This is a delimiter that comes in the Main-Regular
     * font, but is restyled to either be in textstyle, scriptstyle, or
     * scriptscriptstyle.
     */
    fun makeSmallDelim(
        delim: String,
        style: Style,
        center: Boolean,
        options: Options,
        mode: Mode,
        classes: MutableSet<CssClass>
    ): RNodeSpan {
        val text = RenderTreeBuilder.makeSymbol(delim, "Main-Regular", mode, options)
        val span = styleWrap(text, style, options, classes)
        if (center) {
            centerSpan(span, options, style)
        }
        return span
    }

    /**
     * Builds a symbol in the given font size (note size is an integer)
     */
    fun mathrmSize(
        value: String,
        size: Int,
        mode: Mode,
        options: Options
    ): RNodeSymbol {
        return RenderTreeBuilder.makeSymbol(value, "Size${size}-Regular", mode, options);
    };

    /**
     * Makes a large delimiter. This is a delimiter that comes in the Size1, Size2,
     * Size3, or Size4 fonts. It is always rendered in textstyle.
     */
    fun makeLargeDelim(delim: String,
        size: Int,
        center: Boolean,
        options: Options,
        mode: Mode,
        classes: MutableSet<CssClass>
    ): RNodeSpan {
        val inner = mathrmSize(delim, size, mode, options)
        val span = styleWrap(
                RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.delimsizing, CssClass.sizeClass(size)), mutableListOf(inner), options),
                Style.TEXT, options, classes)
        if (center) {
            centerSpan(span, options, Style.TEXT)
        }
        return span
    }

    /**
     * Make an inner span with the given offset and in the given font. This is used
     * in `makeStackedDelim` to make the stacking pieces for the delimiter.
     */
    fun makeInner(
        symbol: String,
        font: String, /* "Size1-Regular" | "Size4-Regular",*/
        mode: Mode
    ): VListElem {
        // Apply the correct CSS class to choose the right font.
        val sizeClass = if (font == "Size1-Regular") {
            CssClass.delim_size1
        } else /* if (font == "Size4-Regular") */ {
            CssClass.delim_size4
        }

        val inner = RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.delimsizing, sizeClass),
                        mutableListOf(
                            RenderTreeBuilder.makeSpan(mutableSetOf(),
                                mutableListOf(RenderTreeBuilder.makeSymbol(symbol, font, mode)))
                        ))

        // Since this will be passed into `makeVList` in the end, wrap the element
        // in the appropriate tag that VList uses.
        return VListElem(inner)
    }

    fun Double.ceilToInt() : Int = Math.ceil(this).toInt()


    /**
     * Make a stacked delimiter out of a given delimiter, with the total height at
     * least `heightTotal`. This routine is mentioned on page 442 of the TeXbook.
     */
    fun makeStackedDelim(
        delim: String,
        heightTotal: Double,
        center: Boolean,
        options: Options,
        mode: Mode,
        classes: MutableSet<CssClass>
    ): RNodeSpan {
        // There are four parts, the top, an optional middle, a repeated part, and a
        // bottom.
        var top = delim
        var middle : String?
        var repeat = delim
        var bottom = delim
        middle = null;
        // Also keep track of what font the delimiters are in
        var font = "Size1-Regular";

        // We set the parts and font based on the symbol. Note that we use
        // '\u23d0' instead of '|' and '\u2016' instead of '\\|' for the
        // repeats of the arrows
        if (delim == "\\uparrow") {
            repeat = "\u23d0";
            bottom = repeat
        } else if (delim == "\\Uparrow") {
            bottom = "\u2016"
            repeat = bottom
        } else if (delim == "\\downarrow") {
            repeat = "\u23d0"
            top = repeat
        } else if (delim == "\\Downarrow") {
            repeat = "\u2016"
            top = repeat
        } else if (delim == "\\updownarrow") {
            top = "\\uparrow"
            repeat = "\u23d0"
            bottom = "\\downarrow"
        } else if (delim == "\\Updownarrow") {
            top = "\\Uparrow"
            repeat = "\u2016"
            bottom = "\\Downarrow"
        } else if (delim == "[" || delim == "\\lbrack") {
            top = "\u23a1"
            repeat = "\u23a2";
            bottom = "\u23a3";
            font = "Size4-Regular";
        } else if (delim == "]" || delim == "\\rbrack") {
            top = "\u23a4";
            repeat = "\u23a5";
            bottom = "\u23a6";
            font = "Size4-Regular";
        } else if (delim == "\\lfloor" || delim == "\u230a") {
            top = "\u23a2"
            repeat = top
            bottom = "\u23a3";
            font = "Size4-Regular";
        } else if (delim == "\\lceil" || delim == "\u2308") {
            top = "\u23a1";
            bottom = "\u23a2"
            repeat = bottom
            font = "Size4-Regular";
        } else if (delim == "\\rfloor" || delim == "\u230b") {
            top = "\u23a5"
            repeat = top
            bottom = "\u23a6";
            font = "Size4-Regular";
        } else if (delim == "\\rceil" || delim == "\u2309") {
            top = "\u23a4";
            bottom = "\u23a5"
            repeat = bottom
            font = "Size4-Regular";
        } else if (delim == "(" || delim == "\\lparen") {
            top = "\u239b"
            repeat = "\u239c"
            bottom = "\u239d"
            font = "Size4-Regular";
        } else if (delim == ")" || delim == "\\rparen") {
            top = "\u239e"
            repeat = "\u239f"
            bottom = "\u23a0"
            font = "Size4-Regular"
        } else if (delim == "\\{" || delim == "\\lbrace") {
            top = "\u23a7"
            middle = "\u23a8"
            bottom = "\u23a9"
            repeat = "\u23aa"
            font = "Size4-Regular"
        } else if (delim == "\\}" || delim == "\\rbrace") {
            top = "\u23ab"
            middle = "\u23ac"
            bottom = "\u23ad"
            repeat = "\u23aa"
            font = "Size4-Regular"
        } else if (delim == "\\lgroup" || delim == "\u27ee") {
            top = "\u23a7"
            bottom = "\u23a9"
            repeat = "\u23aa"
            font = "Size4-Regular"
        } else if (delim == "\\rgroup" || delim == "\u27ef") {
            top = "\u23ab"
            bottom = "\u23ad"
            repeat = "\u23aa"
            font = "Size4-Regular"
        } else if (delim == "\\lmoustache" || delim == "\u23b0") {
            top = "\u23a7"
            bottom = "\u23ad"
            repeat = "\u23aa"
            font = "Size4-Regular"
        } else if (delim == "\\rmoustache" || delim == "\u23b1") {
            top = "\u23ab"
            bottom = "\u23a9"
            repeat = "\u23aa"
            font = "Size4-Regular"
        }

        // Get the metrics of the four sections
        val topMetrics = getMetrics(top, font, mode)
        val topHeightTotal = topMetrics.height + topMetrics.depth
        val repeatMetrics = getMetrics(repeat, font, mode)
        val repeatHeightTotal = repeatMetrics.height + repeatMetrics.depth
        val bottomMetrics = getMetrics(bottom, font, mode)
        val bottomHeightTotal = bottomMetrics.height + bottomMetrics.depth

        var middleHeightTotal = 0.0
        var middleFactor = 1
        if (middle !== null) {
            val middleMetrics = getMetrics(middle, font, mode);
            middleHeightTotal = middleMetrics.height + middleMetrics.depth;
            middleFactor = 2; // repeat symmetrically above and below middle
        }

        // Calcuate the minimal height that the delimiter can have.
        // It is at least the size of the top, bottom, and optional middle combined.
        val minHeight = topHeightTotal + bottomHeightTotal + middleHeightTotal;

        // Compute the number of copies of the repeat symbol we will need
        val repeatCount = ((heightTotal - minHeight) / (middleFactor * repeatHeightTotal)).ceilToInt()

        // Compute the total height of the delimiter including all the symbols
        val realHeightTotal =
            minHeight + repeatCount * middleFactor * repeatHeightTotal;

        // The center of the delimiter is placed at the center of the axis. Note
        // that in this context, "center" means that the delimiter should be
        // centered around the axis in the current style, while normally it is
        // centered around the axis in textstyle.
        var axisHeight = options.fontMetrics.axisHeight;
        if (center) {
            axisHeight *= options.sizeMultiplier;
        }
        // Calculate the depth
        val depth = realHeightTotal / 2 - axisHeight;

        // Now, we start building the pieces that will go into the vlist

        // Keep a list of the inner pieces
        val inners = mutableListOf<VListElem>()

        // Add the bottom symbol
        inners.add(makeInner(bottom, font, mode));

        if (middle == null) {
            // Add that many symbols
            repeat(repeatCount) {
                inners.add(makeInner(repeat, font, mode));
            }
        } else {
            // When there is a middle bit, we need the middle part and two repeated
            // sections
            repeat(repeatCount){
                inners.add(makeInner(repeat, font, mode));
            }
            inners.add(makeInner(middle, font, mode));
            repeat(repeatCount) {
                inners.add(makeInner(repeat, font, mode));
            }
        }

        // Add the top symbol
        inners.add(makeInner(top, font, mode));

        // Finally, build the vlist
        val newOptions = options.havingBaseStyle(Style.TEXT);
        val inner = RenderBuilderVList.makeVList(
                        VListParamPositioned(
                            PositionType.Bottom,
                            depth,
                            inners),
                        newOptions);

        return styleWrap(
            RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.delimsizing, CssClass.mult),
                mutableListOf(inner), newOptions),
            Style.TEXT, options, classes);
    }


    /**
     * Make a delimiter of a given height+depth, with optional centering. Here, we
     * traverse the sequences, and create a delimiter that the sequence tells us to.
     */
    fun makeCustomSizedDelim(
        in_delim: String,
        height: Double,
        center: Boolean,
        options: Options,
        mode: Mode,
        classes: MutableSet<CssClass>
        ): RNodeSpan {
        val delim = when (in_delim) {
                "<", "\\lt", "\u27e8" -> "\\langle"
                ">", "\\gt", "\u27e9" -> "\\rangle"
                else -> in_delim
            }

        // Decide what sequence to use
        val sequence = when (delim) {
            in stackNeverDelimiters -> stackNeverDelimiterSequence
            in stackLargeDelimiters -> stackLargeDelimiterSequence
            else -> stackAlwaysDelimiterSequence
        }

        // Look through the sequence
        val delimType = traverseSequence(delim, height, sequence, options);

        // Get the delimiter from font glyphs.
        // Depending on the sequence element we decided on, call the
        // appropriate function.
        return when(delimType) {
            is SmallDelimiter -> makeSmallDelim(delim, delimType.style, center, options,
                mode, classes)
            is LargeDelimiter -> makeLargeDelim(delim, delimType.size.size, center, options, mode,
                classes)
            is StackDelimiter -> makeStackedDelim(delim, height, center, options, mode,
                classes)
        }
    }

    fun sqrtSvg(sqrtName: String,
                    height: Double,
                    viewBoxHeight: Double,
                    options: Options
                    ): RNodePathSpan {
        // TODO: support sqrtTail
        /*
    let alternate;
    if (sqrtName === "sqrtTall") {
        // sqrtTall is from glyph U23B7 in the font KaTeX_Size4-Regular
        // One path edge has a variable length. It runs from the viniculumn
        // to a point near (14 units) the bottom of the surd. The viniculum
        // is 40 units thick. So the length of the line in question is:
        const vertSegment = viewBoxHeight - 54 - vbPad;
        alternate = `M702 ${vbPad}H400000v40H742v${vertSegment}l-4 4-4 4c-.667.7
-2 1.5-4 2.5s-4.167 1.833-6.5 2.5-5.5 1-9.5 1h-12l-28-84c-16.667-52-96.667
-294.333-240-727l-212 -643 -85 170c-4-3.333-8.333-7.667-13 -13l-13-13l77-155
 77-156c66 199.333 139 419.667 219 661 l218 661zM702 ${vbPad}H400000v40H742z`;
    }
    */

        val pathNode = RNodePath(sqrtName)

        // Note: 1000:1 ratio of viewBox to document em width.
        val svg  : RenderNode =  RNodePathHolder(mutableListOf(pathNode),
            "400em",
            height.toString() + "em",
            ViewBox(0.0, 0.0, 400000.0, viewBoxHeight),
            "xMinYMin slice")

        return RNodePathSpan(mutableSetOf(CssClass.hide_tail), mutableListOf(svg), options)
    }


    /**
     * Make a sqrt image of the given height,
     */
    fun makeSqrtImage(height: Double, options: Options) : Triple<RNodePathSpan, /*advancedWidth*/ Double, /*ruleWidth*/ Double> {
        // Define a newOptions that removes the effect of size changes such as \Huge.
        // We don't pick different a height surd for \Huge. For it, we scale up.
        val newOptions = options.havingBaseSizing()

        // Pick the desired surd glyph from a sequence of surds.
        val delim = traverseSequence("\\surd", height * newOptions.sizeMultiplier, stackLargeDelimiterSequence, newOptions)

        var sizeMultiplier = newOptions.sizeMultiplier  // default

        // Create a span containing an SVG image of a sqrt symbol.
        var span : RNodePathSpan
        var spanHeight: Double
        var texHeight: Double
        var viewBoxHeight:Double
        var advanceWidth : Double

        // We create viewBoxes with 80 units of "padding" above each surd.
        // Then browser rounding error on the parent span height will not
        // encroach on the ink of the viniculum. But that padding is not
        // included in the TeX-like `height` used for calculation of
        // vertical alignment. So texHeight = span.height < span.style.height.

        when(delim) {
            is SmallDelimiter -> {
                // Get an SVG that is derived from glyph U+221A in font KaTeX-Main.
                viewBoxHeight = (1000 + vbPad).toDouble()  // 1000 unit glyph height.
                if (height < 1.0) {
                    sizeMultiplier = 1.0;   // mimic a \textfont radical
                } else if (height < 1.4) {
                    sizeMultiplier = 0.7;   // mimic a \scriptfont radical
                }
                spanHeight = (1.0 + emPad) / sizeMultiplier;
                texHeight = 1.00 / sizeMultiplier;
                span = sqrtSvg("sqrtMain", spanHeight, viewBoxHeight, options);
                span.style.minWidth = "0.853em";
                advanceWidth = 0.833 / sizeMultiplier;  // from the font.
            }
            is LargeDelimiter -> {
                // These SVGs come from fonts: KaTeX_Size1, _Size2, etc.
                viewBoxHeight = (1000 + vbPad).toDouble() * sizeToMaxHeight[delim.size.size];
                texHeight = sizeToMaxHeight[delim.size.size] / sizeMultiplier;
                spanHeight = (sizeToMaxHeight[delim.size.size] + emPad) / sizeMultiplier;
                span = sqrtSvg("sqrtSize" + delim.size, spanHeight, viewBoxHeight, options);
                span.style.minWidth = "1.02em";
                advanceWidth = 1.0 / sizeMultiplier; // 1.0 from the font.

            }
            else -> {
                // Tall sqrt. In TeX, this would be stacked using multiple glyphs.
                // We'll use a single SVG to accomplish the same thing.
                spanHeight = height + emPad;
                texHeight = height;
                viewBoxHeight = Math.floor(1000 * height) + vbPad;
                span = sqrtSvg("sqrtTall", spanHeight, viewBoxHeight, options);
                span.style.minWidth = "0.742em";
                advanceWidth = 1.056;
            }
        }


        span.height = texHeight;
        span.style.height = spanHeight.toString() + "em";

        return Triple(
            span,
            advanceWidth,
            // Calculate the actual line width.
            // This actually should depend on the chosen font -- e.g. \boldmath
            // should use the thicker surd symbols from e.g. KaTeX_Main-Bold, and
            // have thicker rules.
            options.fontMetrics.sqrtRuleThickness * sizeMultiplier
        )

    }


}
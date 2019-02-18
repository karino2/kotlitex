package io.github.karino2.kotlitex

import android.util.Log
import kotlin.IllegalArgumentException

data class Measurement(val number: Int, val unit: String) {
    val isValidUnit: Boolean
    get() {
        return when(unit) {
            "ex", "em", "mu" -> true
            else -> false
        }
        /* TODO:
        return (unit in ptPerUnit || unit in relativeUnit || unit === "ex");
        */
    }
}


object RenderTreeBuilder {
    val groupBuilders : MutableMap<String, RenderNodeHandlerType>
        get()= LatexFunctions.renderGroupBuilders

    // defineFunctionBuilder in js.
    fun registerBuilder(nodeType: String, builder: (ParseNode, Options)->RenderNode) {
        groupBuilders[nodeType] = builder
    }

    /**
     * Calculate the height, depth, and maxFontSize of an element based on its
     * children.
     */
    fun sizeElementFromChildren(
            elem: RNodeSpan
    ) {
        var height = elem.children.map {it.height }.max() ?: 0.0
        var depth = elem.children.map {it.depth }.max() ?: 0.0
        var maxFontSize = elem.children.map {it.maxFontSize}.max() ?: 0.0

        elem.height = height
        elem.depth = depth
        elem.maxFontSize = maxFontSize
    };


    fun makeSpan(klasses: MutableSet<CssClass> = mutableSetOf(), children: MutableList<RenderNode> = mutableListOf(), options: Options? = null, style: CssStyle = CssStyle()) : RNodeSpan {
        val span = RNodeSpan(klasses, children, options, style)
        sizeElementFromChildren(span);
        return span
    }

    fun makeLineSpan(
        className: CssClass,
        options: Options,
        thickness: Double? = null
    ) : RNodeSpan {
        val line = makeSpan(mutableSetOf(className), mutableListOf(), options);
        line.height = thickness ?: options.fontMetrics.defaultRuleThickness;
        line.style.borderBottomWidth = line.height.toString() + "em"
        line.maxFontSize = 1.0;
        return line;
    }

    fun makeNullDelimiter(
        options: Options,
        classes: MutableSet<CssClass>
    ): RNodeSpan {

        val moreClasses = mutableSetOf(CssClass.nulldelimiter, *options.baseSizingClasses)

        return makeSpan(classes.concat(moreClasses))
    }



    /**
     * buildGroup is the function that takes a group and calls the correct groupType
     * function for it. It also handles the interaction of size and style changes
     * between parents and children.
     */
    fun buildGroup(group: ParseNode?, options: Options,
                   baseOptions: Options? = null) : RenderNode {
        if (group == null) {
            return makeSpan()
        }

        if (groupBuilders.containsKey(group.type)) {
            // Call the groupBuilders function
            // $FlowFixMe
            var groupNode = groupBuilders[group.type]!!(group, options)

            // If the size changed between the parent and the current group, account
            // for that size difference.
            if (baseOptions != null && options.size != baseOptions.size) {
                groupNode = makeSpan(options.sizingClasses(baseOptions),
                    mutableListOf(groupNode), options);

                val multiplier =
                options.sizeMultiplier / baseOptions.sizeMultiplier;

                groupNode.height *= multiplier;
                groupNode.depth *= multiplier;
            }

            return groupNode;
        } else {
            throw ParseError("Got group of unknown type: '${group.type}'", null)
        }

    }



    /**
     * Makes a symbolNode after translation via the list of symbols in symbols.js.
     * Correctly pulls out metrics for the character, and optionally takes a list of
     * classes to be attached to the node.
     *
     * TODO: make argument order closer to makeSpan
     * TODO: add a separate argument for math class (e.g. `mop`, `mbin`), which
     * should if present come first in `classes`.
     * TODO(#953): Make `options` mandatory and always pass it in.
     */
    fun makeSymbol(
        in_value: String,
        fontName: String,
        mode: Mode,
        options: Options? = null,
        classes: MutableSet<CssClass> = mutableSetOf()
    ): RNodeSymbol {
        val (value, metrics) = Symbols.lookupSymbol(in_value, fontName, mode);

        var symbolNode = if (metrics != null) {
            var italic = metrics.italic;
            if (mode == Mode.TEXT || (options != null && options.font == "mathit")) {
                italic = 0.0;
            }
            RNodeSymbol(
                    value, height=metrics.height, depth=metrics.depth, italic = italic, skew = metrics.skew,
            width=metrics.width, klasses = classes)
        } else {
            // TODO(emily): Figure out a good way to only print this in development
            Log.d("kotlitex", "No character metrics for '$value' in style '$fontName'")

            RNodeSymbol(value, 0.0, 0.0, 0.0, classes, 0.0);
        }

        if (options != null) {
            symbolNode.maxFontSize = options.sizeMultiplier;
            if (options.style.isTight) {
                symbolNode.klasses.add(CssClass.mtight)
            }
            val color = options.color
            if (color != null) {
                symbolNode.style.color = color
            }
        }

        return symbolNode;
    }



    /**
     * Determines which of the two font names (Main-Italic and Math-Italic) and
     * corresponding style tags (maindefault or mathit) to use for default math font,
     * depending on the symbol.
     */
    fun mathdefault(value: String,
    mode: Mode,
    options: Options,
    classes: MutableSet<CssClass>
    ): Pair<String, CssClass> { // {| fontName: string, fontClass: string |}
        return Pair(
            /* fontName:*/ "Math-Italic",
            /* fontClass: */ CssClass.mathdefault)


        /*
        if (/[0-9]/.test(value.charAt(0)) ||
                // glyphs for \imath and \jmath do not exist in Math-Italic so we
                // need to use Main-Italic instead
                utils.contains(mathitLetters, value)) {
            return {
                fontName: "Main-Italic",
                fontClass: "mathit",
            };
        } else {
            return {
                fontName: "Math-Italic",
                fontClass: "mathdefault",
            };
        }
        */
    }

    /**
     * Makes a symbol in Main-Regular or AMS-Regular.
     * Used for rel, bin, open, close, inner, and punct.
     *
     * TODO(#953): Make `options` mandatory and always pass it in.
     */
    fun mathsym(
        value: String,
        mode: Mode,
        options: Options?,
        classes: MutableSet<CssClass>
    ): RNodeSymbol {
        // Decide what font to render the symbol in by its entry in the symbols
        // table.
        // Have a special case for when the value = \ because the \ is used as a
        // textord in unsupported command errors but cannot be parsed as a regular
        // text ordinal and is therefore not present as a symbol in the symbols
        // table for text, as well as a special case for boldsymbol because it
        // can be used for bold + and -
        if ((options?.font == "boldsymbol") &&
            Symbols.lookupSymbol(value, "Main-Bold", mode).second != null) {
            return makeSymbol(value, "Main-Bold", mode, options,
                classes.concat(setOf(CssClass.mathbf)))
        } else if (value == "\\" || Symbols.get(mode)[value]?.font == "main") {
            return makeSymbol(value, "Main-Regular", mode, options, classes)
        } else {
            return makeSymbol(
                value, "AMS-Regular", mode, options, classes.concat(setOf(CssClass.amsrm)))
        }
    }


    // Takes font options, and returns the appropriate fontLookup name
    fun retrieveTextFontName(fontFamily: String,
    fontWeight: CssClass,
    fontShape: CssClass
    ): String {
        val baseFontName = when (fontFamily) {
            "amsrm" -> "AMS"
            "textrm" -> "Main"
            "textsf" -> "SansSerif"
            "texttt" -> "Typewriter"
            else -> fontFamily; // use fonts added by a plugin
        }

        val fontStylesName = if (fontWeight ==  CssClass.textbf  && fontShape == CssClass.textit) {
             "BoldItalic";
        } else if (fontWeight == CssClass.textbf) {
             "Bold";
        } else if (fontWeight == CssClass.textit) {
            "Italic";
        } else {
            "Regular";
        }

        return "${baseFontName}-${fontStylesName}"
    }


    /**
     * Makes either a mathord or textord in the correct font and color.
     */
    fun makeOrd(group: ParseNode, options: Options, type: String) : RNodeSymbol {
        if(group !is PNodeOrd) {
            throw IllegalArgumentException("unexpected type in makeOrd.")
        }

        val mode = group.mode
        val text = group.text

        val classes = mutableSetOf(CssClass.mord)

        // Math mode or Old font (i.e. \rm)
        val isFont = mode == Mode.MATH || (mode == Mode.TEXT && options.font != "");
        /*
        val fontOrFamily = if(isFont)  options.font else options.fontFamily
        TODO:
        if (text.charCodeAt(0) === 0xD835) {
            // surrogate pairs get special treatment
            const [wideFontName, wideFontClass] = wideCharacterFont(text, mode);
            return makeSymbol(text, wideFontName, mode, options,
                classes.concat(wideFontClass));
        } else if (fontOrFamily) {
            let fontName;
            let fontClasses;
            if (fontOrFamily === "boldsymbol" || fontOrFamily === "mathnormal") {
                const fontData = fontOrFamily === "boldsymbol"
                ? boldsymbol(text, mode, options, classes)
                : mathnormal(text, mode, options, classes);
                fontName = fontData.fontName;
                fontClasses = [fontData.fontClass];
            } else if (utils.contains(mathitLetters, text)) {
                fontName = "Main-Italic";
                fontClasses = ["mathit"];
            } else if (isFont) {
                fontName = fontMap[fontOrFamily].fontName;
                fontClasses = [fontOrFamily];
            } else {
                fontName = retrieveTextFontName(fontOrFamily, options.fontWeight,
                    options.fontShape);
                fontClasses = [fontOrFamily, options.fontWeight, options.fontShape];
            }

            if (lookupSymbol(text, fontName, mode).metrics) {
                return makeSymbol(text, fontName, mode, options,
                    classes.concat(fontClasses));
            } else if (ligatures.hasOwnProperty(text) &&
                fontName.substr(0, 10) === "Typewriter") {
                // Deconstruct ligatures in monospace fonts (\texttt, \tt).
                const parts = [];
                for (let i = 0; i < text.length; i++) {
                    parts.push(makeSymbol(text[i], fontName, mode, options,
                        classes.concat(fontClasses)));
                }
                return makeFragment(parts);
            }
        }
        */

        // Makes a symbol in the default font for mathords and textords.
        if (group is PNodeMathOrd) {
            val (fontName, fontClass) = mathdefault(text, mode, options, classes)
            classes.add(fontClass)
            return makeSymbol(text, fontName, mode, options, classes);
        } else if (group is PNodeTextOrd) {
            val font = Symbols.get(mode)[text]?.font
            if (font == "ams") {
                val fontName = retrieveTextFontName("amsrm", options.fontWeight,
                options.fontShape);
                return makeSymbol(
                    text, fontName, mode, options,
                    classes.concat(setOf(CssClass.amsrm, options.fontWeight, options.fontShape)));
            } else if (font == "main" || font == null) {
                val fontName = retrieveTextFontName("textrm", options.fontWeight,
                options.fontShape);
                return makeSymbol(
                    text, fontName, mode, options,
                    classes.concat(setOf(options.fontWeight, options.fontShape)));
            } else { // fonts added by plugins

                val fontName = retrieveTextFontName(font, options.fontWeight,
                options.fontShape);
                // We add font name as a css class
                return makeSymbol(
                    text, fontName, mode, options,
                    classes.concat(setOf(/* fontName, */ options.fontWeight, options.fontShape)));
                /* TODO:
                       fontName should be css classname in this case. How should we?
                */
            }
        } else {
            // never reached here in kotlin.
            throw Error("unexpected type: " + type + " in makeOrd");
        }
    }



    // Return the outermost node of a domTree.
    fun getOutermostNode(
            node: RenderNode,
            side: String /* "left" or "right" */
            ): RenderNode
    {
        // TODO:
        /*
        if (node instanceof DocumentFragment ||
            node instanceof Anchor) {
            const children = node.children;
            if (children.length) {
                if (side === "right") {
                    return getOutermostNode(children[children.length - 1], "right");
                } else if (side === "left") {
                    return getOutermostNode(children[0], "right");
                }
            }
        }
        */
        return node;
    }

    fun getTypeOfDomTree(in_node: RenderNode?, side: String /* left or right */) : CssClass {
        if (in_node == null) {
            return CssClass.EMPTY
        }

        val node = getOutermostNode(in_node, side)
        // This makes a lot of assumptions as to where the type of atom
        // appears.  We should do a better job of enforcing this.
        // TODO:
        /*
        return DomEnum[node.classes[0]] || null;
        */
        if(node.hasClass(CssClass.mord))
            return CssClass.mord
        return CssClass.EMPTY
    }

    // Binary atoms (first class `mbin`) change into ordinary atoms (`mord`)
    // depending on their surroundings. See TeXbook pg. 442-446, Rules 5 and 6,
    // and the text before Rule 19.
    fun isBinLeftCanceller(
        node: RenderNode?,
        isRealGroup: Boolean
        ): Boolean
    {
        // karino: Below comment is original node one, our situationmight be different.
        // TODO: This code assumes that a node's math class is the first element
        // of its `classes` array. A later cleanup should ensure this, for
        // instance by changing the signature of `makeSpan`.
        if (node != null) {
            return when(getTypeOfDomTree(node, "right")) {
                CssClass.mbin, CssClass.mopen, CssClass.mrel,
                CssClass.mop, CssClass.mpunct -> true
                else -> false
            }
        } else {
            return isRealGroup;
        }
    };

    fun isBinRightCanceller(
        node: RenderNode?,
        isRealGroup: Boolean
        ): Boolean
    {
        if (node != null) {
            return when(getTypeOfDomTree(node, "left")) {
                CssClass.mrel, CssClass.mclose, CssClass.mpunct -> true
                else -> false
            }
        } else {
            return isRealGroup;
        }
    }

    val thinspace = Measurement(3, "mu")
    val mediumspace = Measurement(4, "mu")
    val thickspace = Measurement(5, "mu")

    // Spacing relationships for display and text styles
    fun getSpacings(leftType: CssClass, rightType: CssClass) : Measurement? {
        return when(leftType) {
            CssClass.mord -> {
                when(rightType) {
                    CssClass.mop, CssClass.minner -> thinspace
                    CssClass.mbin-> mediumspace
                    CssClass.mrel-> thickspace
                    else -> null
                }
            }
            CssClass.mop -> {
                when(rightType) {
                    CssClass.mord, CssClass.mop, CssClass.minner -> thinspace
                    CssClass.mrel -> thickspace
                    else -> null
                }
            }
            CssClass.mbin -> {
                when(rightType) {
                    CssClass.mord, CssClass.mop, CssClass.mopen, CssClass.minner -> mediumspace
                    else -> null
                }
            }
            CssClass.mrel -> {
                when(rightType) {
                    CssClass.mord, CssClass.mop, CssClass.mopen, CssClass.minner -> thickspace
                    else -> null
                }
            }
            CssClass.mopen -> null
            CssClass.mclose -> {
                when(rightType) {
                    CssClass.mop, CssClass.minner -> thinspace
                    CssClass.mbin -> mediumspace
                    CssClass.mrel -> thickspace
                    else -> null
                }
            }
            CssClass.mpunct -> {
                when(rightType) {
                    CssClass.mrel -> thickspace
                    CssClass.mord, CssClass.mop, CssClass.mopen,
                        CssClass.mclose, CssClass.mpunct, CssClass.minner -> thinspace
                    else -> null
                }
            }
            CssClass.minner -> {
                when(rightType) {
                    CssClass.mbin -> mediumspace
                    CssClass.mrel -> thickspace
                    CssClass.mord, CssClass.mop, CssClass.mopen, CssClass.mpunct, CssClass.minner -> thinspace
                    else -> null
                }
            }
            else -> null
        }
    }

    // Spacing relationships for script and scriptscript styles
    fun getTightSpacings(leftType: CssClass, rightType: CssClass) : Measurement? {
        when(leftType) {
            CssClass.mbin, CssClass.mrel, CssClass.mopen, CssClass.mpunct -> return null
            else -> {}
        }

        return when(Pair(leftType, rightType)) {
            Pair(CssClass.mord, CssClass.mop) -> thinspace
            Pair(CssClass.mop, CssClass.mord) -> thinspace
            Pair(CssClass.mop, CssClass.mop) -> thinspace
            Pair(CssClass.mclose, CssClass.mop) -> thinspace
            Pair(CssClass.minner, CssClass.mop) -> thinspace
            else -> null
        }
    }

    // If `node` is an atom return whether it's been assigned the mtight class.
    // If `node` is a document fragment, return the value of isLeftTight() for the
    // leftmost node in the fragment.
    // 'mtight' indicates that the node is script or scriptscript style.
    fun isLeftTight(in_node: RenderNode): Boolean {
        val node = getOutermostNode(in_node, "left");
        return node.hasClass(CssClass.mtight);
    }

    fun calculateSize(sizeValue: Measurement, options: Options) : Double {
        // TODO: now only support mu.
        val scale = options.fontMetrics.cssEmPerMu
        return Math.min(sizeValue.number * scale, options.maxSize);
    }

    // Glue is a concept from TeX which is a flexible space between elements in
    // either a vertical or horizontal list. In KaTeX, at least for now, it's
    // static space between elements in a horizontal layout.
    fun makeGlue(measurement: Measurement, options: Options): RNodeSpan {
        // Make an empty span for the space
        val rule = makeSpan(mutableSetOf(CssClass.mspace), mutableListOf(), options);
        val size = calculateSize(measurement, options);
        rule.style.marginRight = "${size}em";
        return rule;
    };


    /**
     * Take a list of nodes, build them in order, and return a list of the built
     * nodes. documentFragments are flattened into their contents, so the
     * returned list contains no fragments. `isRealGroup` is true if `expression`
     * is a real group (no atoms will be added on either side), as opposed to
     * a partial group (e.g. one created by \color). `surrounding` is an array
     * consisting type of nodes that will be added to the left and right.
     */
    fun buildExpression(
        expression: List<ParseNode>,
        options: Options,
        isRealGroup: Boolean
        // TODO: , surrounding: [?DomType, ?DomType] = [null, null],
        ): List<RenderNode> {
        // Parse expressions into `groups`.
        val rawGroups = mutableListOf<RenderNode>();
        for (expr in expression) {
            val output = buildGroup(expr, options);
            /* TODO:
            if (output instanceof DocumentFragment) {
                const children: HtmlDomNode[] = output.children;
                rawGroups.push(...children);
            } else {
                rawGroups.push(output);
            }
            */
            rawGroups.add(output)
        }
        // At this point `rawGroups` consists entirely of `symbolNode`s and `span`s.

        // Ignore explicit spaces (e.g., \;, \,) when determining what implicit
        // spacing should go between atoms of different classes, and add dummy
        // spans for determining spacings between surrounding atoms.
        val rowNonSpace = rawGroups.filter{group-> group != null && !group.klasses.contains(CssClass.mspace)}
        val nonSpaces = listOf<RenderNode?>(null, *rowNonSpace.toTypedArray(), null)
        /* TODO:
        const nonSpaces: (?HtmlDomNode)[] = [
        surrounding[0] ? makeSpan([surrounding[0]], [], options) : null,
        ...rawGroups.filter(group => group && group.classes[0] !== "mspace"),
        surrounding[1] ? makeSpan([surrounding[1]], [], options) : null,
        ];
        */

        // Before determining what spaces to insert, perform bin cancellation.
        // Binary operators change to ordinary symbols in some contexts.
        for (i in 1 until (nonSpaces.size-1)) {
            val nonSpacesI = nonSpaces[i]!!
            val left = getOutermostNode(nonSpacesI, "left");
            if (left.klasses.contains(CssClass.mbin) &&
                isBinLeftCanceller(nonSpaces[i - 1], isRealGroup)) {
                left.klasses.remove(CssClass.mbin)
                left.klasses.add(CssClass.mord)
            }

            val right = getOutermostNode(nonSpacesI, "right")
            if (right.klasses.contains(CssClass.mbin) &&
                isBinRightCanceller(nonSpaces[i + 1], isRealGroup)) {
                right.klasses.remove(CssClass.mbin)
                right.klasses.add(CssClass.mord)
            }
        }

        val groups = mutableListOf<RenderNode>();
        var j = 0
        var i = 0

        // inside loop, sometime i is changed so we cannot use normal for loop
        while(i < rawGroups.size) {
            groups.add(rawGroups[i]);

            // For any group that is not a space, get the next non-space.  Then
            // lookup what implicit space should be placed between those atoms and
            // add it to groups.
            if (!rawGroups[i].klasses.contains(CssClass.mspace) && j < nonSpaces.size - 1) {
                // if current non-space node is left dummy span, add a glue before
                // first real non-space node
                if (j == 0) {
                    groups.removeAt(groups.size-1)
                    i--;
                }

                // Get the type of the current non-space node.  If it's a document
                // fragment, get the type of the rightmost node in the fragment.
                val left = getTypeOfDomTree(nonSpaces[j], "right")

                // Get the type of the next non-space node.  If it's a document
                // fragment, get the type of the leftmost node in the fragment.
                val right = getTypeOfDomTree(nonSpaces[j + 1], "left")

                // We use buildExpression inside of sizingGroup, but it returns a
                // document fragment of elements.  sizingGroup sets `isRealGroup`
                // to false to avoid processing spans multiple times.
                if (left != CssClass.EMPTY && right != CssClass.EMPTY && isRealGroup) {
                    val nonSpacesJp1 = nonSpaces[j + 1]!!
                    val space = if(isLeftTight(nonSpacesJp1)) getTightSpacings(left, right) else getSpacings(left, right)

                    if (space != null) {
                        var glueOptions = options

                        if (expression.size == 1) {
                            /* TODO: sizing, styling is not exists now. porting later.
                            val node =expression[0] as
                            checkNodeType(expression[0], "sizing") ||
                                    checkNodeType(expression[0], "styling");
                            if (node == null) {
                                // No match.
                            } else if (node.type === "sizing") {
                                glueOptions = options.havingSize(node.size);
                            } else if (node.type === "styling") {
                                glueOptions = options.havingStyle(styleMap[node.style]);
                            }
                                    */
                        }

                        groups.add(makeGlue(space, glueOptions))
                    }
                }
                j++;
            }
            i++
        }

        return groups;
    }

    fun wrapFragment(group: RenderNode, options: Options): RenderNode {
        /*
        TODO:
            if (group instanceof DocumentFragment) {
                return makeSpan([], [group], options);
            }
         */
        return group
    }

    fun tryCombineChars(in_chars: List<RenderNode>): MutableList<RenderNode> {
        val chars = in_chars.toMutableList()
        var i = 0
        while(i < chars.size-1) {
            val prev = chars[i];
            val next = chars[i + 1];
            if (prev is RNodeSymbol
                && next is RNodeSymbol
                && canCombine(prev, next)) {

                prev.text += next.text;
                prev.height = Math.max(prev.height, next.height);
                prev.depth = Math.max(prev.depth, next.depth);
                // Use the last character's italic correction since we use
                // it to add padding to the right of the span created from
                // the combined characters.
                prev.italic = next.italic;
                chars.removeAt(i+1)
                i--;
            }
            i++
        }
        return chars
    }


    private fun canCombine(prev: RNodeSymbol, next: RNodeSymbol): Boolean {
        if (prev.klasses != next.klasses
            || prev.skew != next.skew
            || prev.maxFontSize != next.maxFontSize) {
            return false
        }

        /*
        for (style in prev.style) {
            if (prev.style.hasOwnProperty(style)
                && prev.style[style] !== next.style[style]) {
                return false;
            }
        }
        for (const style in next.style) {
            if (next.style.hasOwnProperty(style)
                && prev.style[style] !== next.style[style]) {
                return false;
            }
        }
        */
        if(prev.style != next.style)
            return false

        return true
    }


    init {
        registerBuilder("mathord") { node, opt -> makeOrd(node, opt, "mathord") }
        registerBuilder("textord") { node, opt -> makeOrd(node, opt, "textord") }
        registerBuilder("supsub") {node, opt-> RenderBuilderSupsub.makeSupsub(node, opt) }
        registerBuilder("atom") { node, opt ->
            if(node !is PNodeAtom) {
                throw IllegalArgumentException("atom but not PNodeAtom")
            }
            RenderTreeBuilder.mathsym(node.text, node.mode, opt, mutableSetOf(CssClass.mFamily(node.family)))
        }
    }

}
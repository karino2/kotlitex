package io.github.karino2.kotlitex

import android.util.Log
import java.lang.IllegalArgumentException

object RenderTreeBuilder {
    val groupBuilders = mutableMapOf<String, (ParseNode, Options)->RenderNode>()

    fun registerBuilder(nodeType: String, builder: (ParseNode, Options)->RenderNode) {
        groupBuilders[nodeType] = builder
    }

    fun makeSpan(klasses: MutableSet<CssClass> = mutableSetOf(), children: MutableList<RenderNode> = mutableListOf(), options: Options? = null, style: CssStyle = CssStyle()) : SpanNode {

        val span = SpanNode(klasses, children, options, style)
        /* TODO:
        sizeElementFromChildren(span);
        */
        return span
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
        value: String,
        fontName: String,
        mode: Mode,
        options: Options?,
        classes: MutableSet<CssClass>
    ): SymbolNode {
        val (value, metrics) = Symbols.lookupSymbol(value, fontName, mode);

        var symbolNode = if (metrics != null) {
            var italic = metrics.italic;
            if (mode == Mode.TEXT || (options != null && options.font == "mathit")) {
                italic = 0.0;
            }
            SymbolNode(
                    value, height=metrics.height, depth=metrics.depth, italic = italic, skew = metrics.skew,
            width=metrics.width, klasses = classes)
        } else {
            // TODO(emily): Figure out a good way to only print this in development
            Log.d("kotlitex", "No character metrics for '$value' in style '$fontName'")

            SymbolNode(value, 0.0, 0.0, 0.0, classes, 0.0);
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
    fun makeOrd(group: ParseNode, options: Options, type: String) : SymbolNode {
        if(group !is NodeOrd) {
            throw IllegalArgumentException("unexpected type in makeOrd.")
        }

        val mode = group.mode
        val text = group.text

        val classes = mutableSetOf(CssClass.mord)

        // Math mode or Old font (i.e. \rm)
        val isFont = mode == Mode.MATH || (mode == Mode.TEXT && options.font != null);
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
        if (group is NodeMathOrd) {
            val (fontName, fontClass) = mathdefault(text, mode, options, classes)
            classes.add(fontClass)
            return makeSymbol(text, fontName, mode, options, classes);
        } else if (group is NodeTextOrd) {
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




    fun getTypeOfDomTree(node: RenderNode, side: String /* left or right */) : CssClass {
        // TODO:
        return CssClass.mord
    }

    init {
        registerBuilder("mathord") { node, opt -> makeOrd(node, opt, "mathord") }
        registerBuilder("textord") { node, opt -> makeOrd(node, opt, "textord") }
        registerBuilder("supsub") {node, opt-> RenderBuilderSupsub.makeSupsub(node, opt) }
    }

}
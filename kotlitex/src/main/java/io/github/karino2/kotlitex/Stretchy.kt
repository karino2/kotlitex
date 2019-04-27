package io.github.karino2.kotlitex

object Stretchy {

    /* Below comment is from katex/stretchy.js. */
// Many of the KaTeX SVG images have been adapted from glyphs in KaTeX fonts.
// Copyright (c) 2009-2010, Design Science, Inc. (<www.mathjax.org>)
// Copyright (c) 2014-2017 Khan Academy (<www.khanacademy.org>)
// Licensed under the SIL Open Font License, Version 1.1.
// See \nhttp://scripts.sil.org/OFL

// Very Long SVGs
//    Many of the KaTeX stretchy wide elements use a long SVG image and an
//    overflow: hidden tactic to achieve a stretchy image while avoiding
//    distortion of arrowheads or brace corners.

//    The SVG typically contains a very long (400 em) arrow.

//    The SVG is in a container span that has overflow: hidden, so the span
//    acts like a window that exposes only part of the  SVG.

//    The SVG always has a longer, thinner aspect ratio than the container span.
//    After the SVG fills 100% of the height of the container span,
//    there is a long arrow shaft left over. That left-over shaft is not shown.
//    Instead, it is sliced off because the span's CSS has overflow: hidden.

//    Thus, the reader sees an arrow that matches the subject matter width
//    without distortion.

//    Some functions, such as \cancel, need to vary their aspect ratio. These
//    functions do not get the overflow SVG treatment.

// Second Brush Stroke
//    Low resolution monitors struggle to display images in fine detail.
//    So browsers apply anti-aliasing. A long straight arrow shaft therefore
//    will sometimes appear as if it has a blurred edge.

//    To mitigate this, these SVG files contain a second "brush-stroke" on the
//    arrow shafts. That is, a second long thin rectangular SVG path has been
//    written directly on top of each arrow shaft. This reinforcement causes
//    some of the screen pixels to display as black instead of the anti-aliased
//    gray pixel that a  single path would generate. So we get arrow shafts
//    whose edges appear to be sharper.

// In the katexImagesData object just below, the dimensions all
// correspond to path geometry inside the relevant SVG.
// For example, \overrightarrow uses the same arrowhead as glyph U+2192
// from the KaTeX Main font. The scaling factor is 1000.
// That is, inside the font, that arrowhead is 522 units tall, which
// corresponds to 0.522 em inside the document.

    data class KatexImageData(val paths: List<String>, val minWidth: Double, val viewBoxHeight: Int, val align: String = "")

    val katexImagesData = mapOf(
        //   path(s), minWidth, height, align
        "overrightarrow" to KatexImageData(listOf("rightarrow"), 0.888, 522, "xMaxYMin"),
        "overleftarrow" to KatexImageData(listOf("leftarrow"), 0.888, 522, "xMinYMin"),
        "underrightarrow" to KatexImageData(listOf("rightarrow"), 0.888, 522, "xMaxYMin"),
        "underleftarrow" to KatexImageData(listOf("leftarrow"), 0.888, 522, "xMinYMin"),
        "xrightarrow" to KatexImageData(listOf("rightarrow"), 1.469, 522, "xMaxYMin"),
        "xleftarrow" to KatexImageData(listOf("leftarrow"), 1.469, 522, "xMinYMin"),
        "Overrightarrow" to KatexImageData(listOf("doublerightarrow"), 0.888, 560, "xMaxYMin"),
        "xRightarrow" to KatexImageData(listOf("doublerightarrow"), 1.526, 560, "xMaxYMin"),
        "xLeftarrow" to KatexImageData(listOf("doubleleftarrow"), 1.526, 560, "xMinYMin"),
        "overleftharpoon" to KatexImageData(listOf("leftharpoon"), 0.888, 522, "xMinYMin"),
        "xleftharpoonup" to KatexImageData(listOf("leftharpoon"), 0.888, 522, "xMinYMin"),
        "xleftharpoondown" to KatexImageData(listOf("leftharpoondown"), 0.888, 522, "xMinYMin"),
        "overrightharpoon" to KatexImageData(listOf("rightharpoon"), 0.888, 522, "xMaxYMin"),
        "xrightharpoonup" to KatexImageData(listOf("rightharpoon"), 0.888, 522, "xMaxYMin"),
        "xrightharpoondown" to KatexImageData(listOf("rightharpoondown"), 0.888, 522, "xMaxYMin"),
        "xlongequal" to KatexImageData(listOf("longequal"), 0.888, 334, "xMinYMin"),
        "xtwoheadleftarrow" to KatexImageData(listOf("twoheadleftarrow"), 0.888, 334, "xMinYMin"),
        "xtwoheadrightarrow" to KatexImageData(listOf("twoheadrightarrow"), 0.888, 334, "xMaxYMin"),

        "overleftrightarrow" to KatexImageData(listOf("leftarrow", "rightarrow"), 0.888, 522),
        "overbrace" to KatexImageData(listOf("leftbrace", "midbrace", "rightbrace"), 1.6, 548),
        "underbrace" to KatexImageData(listOf("leftbraceunder", "midbraceunder", "rightbraceunder"),
        1.6, 548),
        "underleftrightarrow" to KatexImageData(listOf("leftarrow", "rightarrow"), 0.888, 522),
        "xleftrightarrow" to KatexImageData(listOf("leftarrow", "rightarrow"), 1.75, 522),
        "xLeftrightarrow" to KatexImageData(listOf("doubleleftarrow", "doublerightarrow"), 1.75, 560),
        "xrightleftharpoons" to KatexImageData(listOf("leftharpoondownplus", "rightharpoonplus"), 1.75, 716),
        "xleftrightharpoons" to KatexImageData(listOf("leftharpoonplus", "rightharpoondownplus"),
        1.75, 716),
        "xhookleftarrow" to KatexImageData(listOf("leftarrow", "righthook"), 1.08, 522),
        "xhookrightarrow" to KatexImageData(listOf("lefthook", "rightarrow"), 1.08, 522),
        "overlinesegment" to KatexImageData(listOf("leftlinesegment", "rightlinesegment"), 0.888, 522),
        "underlinesegment" to KatexImageData(listOf("leftlinesegment", "rightlinesegment"), 0.888, 522),
        "overgroup" to KatexImageData(listOf("leftgroup", "rightgroup"), 0.888, 342),
        "undergroup" to KatexImageData(listOf("leftgroupunder", "rightgroupunder"), 0.888, 342),
        "xmapsto" to KatexImageData(listOf("leftmapsto", "rightarrow"), 1.5, 522),
        "xtofrom" to KatexImageData(listOf("leftToFrom", "rightToFrom"), 1.75, 528),

        // The next three arrows are from the mhchem package.
        // In mhchem.sty, min-length is 2.0em. But these arrows might appear in the
        // document as \xrightarrow or \xrightleftharpoons. Those have
        // min-length = 1.75em, so we set min-length on these next three to match.
        "xrightleftarrows" to KatexImageData(listOf("baraboveleftarrow", "rightarrowabovebar"), 1.75, 901),
        "xrightequilibrium" to KatexImageData(listOf("baraboveshortleftharpoon",
        "rightharpoonaboveshortbar"), 1.75, 716),
        "xleftequilibrium" to KatexImageData(listOf("shortbaraboveleftharpoon",
        "shortrightharpoonabovebar"), 1.75, 716)
    )

    // from stretchy.js
    fun groupLength(arg: ParseNode): Int {
        return if (arg is PNodeOrdGroup) {
            arg.body.size
        } else {
            1
        }
    }

    /*
    group: ParseNode<"accent"> | ParseNode<"accentUnder"> | ParseNode<"xArrow">
         | ParseNode<"horizBrace">
     */
    // stretchy.buildSvgSpan_ in js. inner function of svgSpan.
    fun _buildPathSpan(group: ParseNode, options: Options): Triple</* span: */ RNodePathSpan, /* minWidth: */ Double, /* height: */ Double> {
        var viewBoxWidth = 400000  // default
        val label = when (group) {
            is PNodeAccent -> group.label.substring(1)
            is PNodeAccentUnder -> group.label.substring(1)
            is PNodeXArrow -> group.label.substring(1)
            is PNodeHorizBrace -> group.label.substring(1)
            else -> throw IllegalArgumentException("unexpected type in stretch.buildPathSpan.")
        }
        if (setOf("widehat", "widecheck", "widetilde", "utilde").contains(label)) {
            // Each type in the `if` statement corresponds to one of the ParseNode
            // types below. This narrowing is required to access `grp.base`.
            val groupbase = when (group) {
                is PNodeAccent -> group.base
                is PNodeAccentUnder -> group.base
                else -> throw IllegalArgumentException("unexpected type with labels widehat... etc.")
            }
            // There are four SVG images available for each function.
            // Choose a taller image when there are more characters.
            val numChars = groupLength(groupbase)

            val (viewBoxHeight, height, pathName) =
                if (numChars > 5) {
                    if (label == "widehat" || label == "widecheck") {
                        viewBoxWidth = 2364
                        Triple(420, 0.42, label + "4")
                    } else {
                        viewBoxWidth = 2340
                        Triple(312, 0.34, "tilde4")
                    }
                } else {
                    // const imgIndex = [1, 1, 2, 2, 3, 3][numChars];
                    val imgIndex = arrayOf(1, 1, 2, 2, 3, 3)[numChars]
                    if (label == "widehat" || label == "widecheck") {
                        viewBoxWidth = arrayOf(0, 1062, 2364, 2364, 2364)[imgIndex]
                        Triple(
                            arrayOf(0, 239, 300, 360, 420)[imgIndex],
                            arrayOf(0.0, 0.24, 0.3, 0.3, 0.36, 0.42)[imgIndex],
                            label + imgIndex
                        )
                    } else {
                        viewBoxWidth = arrayOf(0, 600, 1033, 2339, 2340)[imgIndex]
                        Triple(
                            arrayOf(0, 260, 286, 306, 312)[imgIndex],
                            arrayOf(0.0, 0.26, 0.286, 0.3, 0.306, 0.34)[imgIndex],
                            "tilde$imgIndex"
                        )
                    }
                }
            val path = RNodePath(pathName)
            val svgNode = RNodePathHolder(mutableListOf(path),
                widthStr = "100%",
                heightStr = "${height}em",
                viewBox = ViewBox(0.0, 0.0, viewBoxWidth.toDouble(), viewBoxHeight.toDouble()),
                preserveAspectRatio = "none"
            )
            return Triple(RNodePathSpan(mutableSetOf<CssClass>(), mutableListOf<RenderNode>(svgNode), options), 0.0, height)
        } else {
            val spans = mutableListOf<RenderNode>()

            val data = katexImagesData[label]!!
            val (paths, minWidth, viewBoxHeight) = data
            val height = viewBoxHeight / 1000

            val numSvgChildren = paths.size
            val (widthClasses, aligns) = when (numSvgChildren) {
                1 -> {
                    // $FlowFixMe: All these cases must be of the 4-tuple type.
                    val align1 = data.align
                    Pair(listOf(CssClass.hide_tail), listOf(align1))
                }
                2 -> Pair(listOf(CssClass.halfarrow_left, CssClass.halfarrow_right),
                    listOf("xMinYMin", "xMaxYMin"))
                3 -> Pair(listOf(CssClass.brace_left, CssClass.brace_center, CssClass.brace_right),
                    listOf("xMinYMin", "xMidYMin", "xMaxYMin"))
                else -> throw IllegalArgumentException(
                    "Correct katexImagesData or update code here to support $numSvgChildren children.")
            }

            (0 until numSvgChildren).forEach { i ->
                val path = RNodePath(paths[i])

                val svgNode = RNodePathHolder(mutableListOf(path),
                    widthStr = "400em",
                    heightStr = "${height}em",
                    viewBox = ViewBox(0.0, 0.0, viewBoxWidth.toDouble(), viewBoxHeight.toDouble()),
                    preserveAspectRatio = aligns[i] + " slice")

                val span = RNodePathSpan(mutableSetOf(widthClasses[i]),
                    mutableListOf<RenderNode>(svgNode),
                    options)

                if (numSvgChildren == 1) {
                    return Triple(span, minWidth, height.toDouble())
                } else {
                    span.style.height = "${height}em"
                    spans.add(span)
                }
            }

            return Triple(
                RenderTreeBuilder.makeSpan(
                    mutableSetOf(CssClass.stretchy),
                    spans,
                    options
                ),
                minWidth,
                height.toDouble()
            )
        }
    }

    // stretchy.svgSpan in js.
    fun pathSpan(group: ParseNode, options: Options): RNodePathSpan {
        val (span, minWidth, height) = _buildPathSpan(group, options)

        // Note that we are returning span.depth = 0.
        // Any adjustments relative to the baseline must be done in buildHTML.
        span.height = height
        span.style.height = "${height}em"
        if (minWidth > 0) {
            span.style.minWidth = "${minWidth}em"
        }
        return span
    }
}
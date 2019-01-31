package io.github.karino2.kotlitex.functions

import android.graphics.Path
import io.github.karino2.kotlitex.*
import io.github.karino2.kotlitex.RenderBuilderDelimiter.makeSqrtImage
import java.lang.IllegalArgumentException

/*
  This file is the port of functions/sqrt.js
 */

object FunctionSqrt {
    fun renderNodeBuilder(group: ParseNode, options: Options) : RNodeSpan {
        if(group !is PNodeSqrt) {
            throw IllegalArgumentException("unexpected type in sqrt RNodeBuilder.")
        }


        // Square roots are handled in the TeXbook pg. 443, Rule 11.
        // First, we do the same steps as in overline to build the inner group
        // and line
        var inner = RenderTreeBuilder.buildGroup(group.body, options.havingCrampedStyle())

        if (inner.height == 0.0) {
            // Render a small surd.
            inner.height = options.fontMetrics.xHeight
        }

        // Some groups can return document fragments.  Handle those by wrapping
        // them in a span.
        inner = RenderTreeBuilder.wrapFragment(inner, options) // Calculate the minimum size for the \surd delimiter

        val metrics = options.fontMetrics
        val theta = metrics.defaultRuleThickness;
        var phi = theta;

        if (options.style.id < Style.TEXT.id) {
            phi = options.fontMetrics.xHeight;
        }

        // Calculate the clearance between the body and line


        var lineClearance = theta + phi / 4.0
        val minDelimiterHeight = inner.height + inner.depth + lineClearance + theta // Create a sqrt SVG of the required minimum size

        // Create a sqrt SVG of the required minimum size
        val (img, ruleWidth, advanceWidth) =
                makeSqrtImage(minDelimiterHeight, options);

        val delimDepth = img.height - ruleWidth;

        // Adjust the clearance based on the delimiter size
        if (delimDepth > inner.height + inner.depth + lineClearance) {
            lineClearance =
                    (lineClearance + delimDepth - inner.height - inner.depth) / 2;
        }

        // Shift the sqrt image
        val imgShift = img.height - inner.height - lineClearance - ruleWidth;

        inner.style.paddingLeft = advanceWidth.toString() + "em";

            // Overlay the image and the argument.
            val body = RenderBuilderVList.makeVList(
                VListParamFirstBaseLine(
                    mutableListOf(
                        VListElem(inner, wrapperClasses = mutableSetOf(CssClass.svg_align)),
                        VListKern(-(inner.height + imgShift)),
                        VListElem(img),
                        VListKern(ruleWidth)
                        )
                ), options)

            if (group.index == null) {
                return RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.mord, CssClass.sqrt), mutableListOf(body), options)
            } else {
                // Handle the optional root index

                // The index is always in scriptscript style
                val newOptions = options.havingStyle(Style.SCRIPTSCRIPT);
                val rootm = RenderTreeBuilder.buildGroup(group.index, newOptions, options);

                // The amount the index is shifted by. This is taken from the TeX
                // source, in the definition of `\r@@t`.
                val toShift = 0.6 * (body.height - body.depth);

                // Build a VList with the superscript shifted up correctly
                val rootVList = RenderBuilderVList.makeVList(
                    VListParamPositioned(
                        PositionType.Shift,
                        -toShift,
                        mutableListOf(VListElem(rootm))
                    ), options);
                // Add a class surrounding it so we can add on the appropriate
                // kerning
                val rootVListWrap =
                    RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.root), mutableListOf(rootVList))

                return RenderTreeBuilder.makeSpan(mutableSetOf(CssClass.mord, CssClass.sqrt), mutableListOf(rootVListWrap, body), options);
            }

    }


    fun defineAll() {
        LatexFunctions.defineFunction(
            FunctionSpec(
                "sqrt",
                1,
                numOptionalArgs = 1
            ),
            listOf("\\sqrt"),
            { context: FunctionContext, args: List<ParseNode>, optArgs: List<ParseNode?> ->
                val index = if(optArgs.isEmpty()) null else optArgs[0]
                PNodeSqrt(context.parser.mode, null, args[0], index)
            },
            FunctionSqrt::renderNodeBuilder
        )
    }
}


object SvgGeometry {
    /*
const hLinePad = 80;  // padding above a sqrt viniculum.
    // sqrtMain path geometry is from glyph U221A in the font KaTeX Main
    // All surds have 80 units padding above the viniculumn.
    sqrtMain: `M95,${622 + hLinePad}c-2.7,0,-7.17,-2.7,-13.5,-8c-5.8,-5.3,-9.5,
-10,-9.5,-14c0,-2,0.3,-3.3,1,-4c1.3,-2.7,23.83,-20.7,67.5,-54c44.2,-33.3,65.8,
-50.3,66.5,-51c1.3,-1.3,3,-2,5,-2c4.7,0,8.7,3.3,12,10s173,378,173,378c0.7,0,
35.3,-71,104,-213c68.7,-142,137.5,-285,206.5,-429c69,-144,104.5,-217.7,106.5,
-221c5.3,-9.3,12,-14,20,-14H400000v40H845.2724s-225.272,467,-225.272,467
s-235,486,-235,486c-2.7,4.7,-9,7,-19,7c-6,0,-10,-1,-12,-3s-194,-422,-194,-422
s-65,47,-65,47z M834 ${hLinePad}H400000v40H845z`,
     */
    /*
<path d='M95,702c-2.7,0,-7.17,-2.7,-13.5,-8c-5.8,-5.3,-9.5,
                            -10,-9.5,-14c0,-2,0.3,-3.3,1,-4c1.3,-2.7,23.83,-20.7,67.5,-54c44.2,-33.3,65.8,
                            -50.3,66.5,-51c1.3,-1.3,3,-2,5,-2c4.7,0,8.7,3.3,12,10s173,378,173,378c0.7,0,
                            35.3,-71,104,-213c68.7,-142,137.5,-285,206.5,-429c69,-144,104.5,-217.7,106.5,
                            -221c5.3,-9.3,12,-14,20,-14H400000v40H845.2724s-225.272,467,-225.272,467
                            s-235,486,-235,486c-2.7,4.7,-9,7,-19,7c-6,0,-10,-1,-12,-3s-194,-422,-194,-422
                            s-65,47,-65,47z M834 80H400000v40H845z'/>

     */
    val sqrtMain by lazy {
        val path = Path()
        // https://www.w3.org/TR/SVG/paths.html
        path.moveTo(95f, 702f)
        path.rCubicTo(-2.7f, 0f, -7.17f, -2.7f, -13.5f, -8f)
        path.rCubicTo(-5.8f,-5.3f,-9.5f, -10f,-9.5f,-14f)
        path.rCubicTo(0f,-2f,0.3f,-3.3f,1f,-4f)
        path.rCubicTo(1.3f,-2.7f,23.83f,-20.7f,67.5f,-54f)
        path.rCubicTo(44.2f,-33.3f,65.8f, -50.3f,66.5f,-51f)
        path.rCubicTo(1.3f,-1.3f,3f,-2f,5f,-2f)
        path.rCubicTo(4.7f,0f,8.7f,3.3f,12f,10f)

        //s173,378,173,378
        path.rCubicTo(12f, 10f,173f,378f,173f,378f)

        path.rCubicTo(0.7f,0f, 35.3f,-71f,104f,-213f)
        path.rCubicTo(68.7f,-142f,137.5f,-285f,206.5f,-429f)
        path.rCubicTo(69f,-144f,104.5f,-217.7f,106.5f,-221f)
        path.rCubicTo(5.3f,-9.3f,12f,-14f,20f,-14f)

        // H400000v40H845.2724
        // TODO: this should be H400000, not h. But I don't know how to get last y, so I couldn't specify y.
        path.rLineTo(400000f, 0f)
        path.rLineTo(0f, 40f)
        // H845.2724
        path.rLineTo(845.2724f, 0f)

        // s-225.272,467,-225.272,467
        path.rCubicTo(845.2724f, 0f, -225.272f,467f,-225.272f,467f)
        // s-235,486,-235,486c-2.7,4.7,-9,7,-19,7c-6,0,-10,-1,-12,-3s-194,-422,-194,-422
        path.rCubicTo(-225.272f,467f, -235f,486f,-235f,486f)
        path.rCubicTo(-2.7f,4.7f,-9f,7f,-19f,7f)
        path.rCubicTo(-6f,0f,-10f,-1f,-12f,-3f)
        // s-194,-422,-194,-422)
        path.rCubicTo(-12f,-3f, -194f,-422f,-194f,-422f)
        // s-65,47,-65,47
        path.rCubicTo(-194f,-422f, -65f,47f,-65f,47f)
        path.close()

        path.moveTo(834f, 80f)
        path.lineTo(400000f, 80f)
        path.rLineTo(0f, 40f)
        path.lineTo(845f, 120f)
        path.close()
        path
    }
}


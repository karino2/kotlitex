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
        val (img, advanceWidth, ruleWidth) =
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

class PathBuilder {
    val path = Path()

    var lastX = 0.0
    var lastY = 0.0
    var secondLastX = 0.0
    var secondLastY = 0.0

    var secondValid = false

    fun M(x: Double, y:Double) {
        path.moveTo(x.toFloat(), y.toFloat())
        lastX = x
        lastY = y
        secondValid = false
    }
    fun c(x1: Double, y1: Double, x2:Double, y2:Double, x3:Double, y3:Double) {
        path.rCubicTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), x3.toFloat(), y3.toFloat())
        secondLastX =lastX+x2
        secondLastY = lastY+y2
        lastX += x3
        lastY += y3
        secondValid = true
    }
    fun s(x1: Double, y1:Double, x2:Double, y2:Double) {
        val (firstCtrlRelX, firstCtrlRelY) = if(secondValid) {
            // (a+secl)/2 = l
            // a = 2l-secl
            val firstCtrlAbsX = 2*lastX-secondLastX
            val firstCtrlAbsY = 2*lastY-secondLastY

            Pair((firstCtrlAbsX-lastX).toFloat(), (firstCtrlAbsY - lastY).toFloat())
        } else {
            Pair(0f, 0f)
        }

        path.rCubicTo(firstCtrlRelX, firstCtrlRelY, x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

        secondLastX = lastX+x1
        secondLastY = lastY+y1
        lastX += x2
        lastY += y2
        secondValid = true

    }

    fun z() {
        path.close()
        secondLastX = 0.0
        secondLastY = 0.0
        secondValid = false
    }
    fun H(x : Double){
        path.lineTo(x.toFloat(), lastY.toFloat())
        lastX = x
        secondValid = false
    }

    fun v(y: Double) {
        path.rLineTo(0f, y.toFloat())
        lastY +=y
        secondValid = false
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
    fun build(body: PathBuilder.()->Unit ) : Path {
        val builder = PathBuilder()
        builder.body()
        return builder.path
    }


    val sqrtMain by lazy {
        build {
            // https://www.w3.org/TR/SVG/paths.html

            M(95.0,702.0)
            c(-2.7,0.0,-7.17,-2.7,-13.5,-8.0)
            c(-5.8,-5.3,-9.5,
                -10.0,-9.5,-14.0)
            c(0.0,-2.0,0.3,-3.3,1.0,-4.0)
            c(1.3,-2.7,23.83,-20.7,67.5,-54.0)
            c(44.2,-33.3,65.8, -50.3,66.5,-51.0)
            c(1.3,-1.3,3.0,-2.0,5.0,-2.0)
            c(4.7,0.0,8.7,3.3,12.0,10.0)
            s(173.0,378.0,173.0,378.0)
            c(0.7,0.0, 35.3,-71.0,104.0,-213.0)
            c(68.7,-142.0,137.5,-285.0,206.5,-429.0)
            c(69.0,-144.0,104.5,-217.7,106.5, -221.0)
            c(5.3,-9.3,12.0,-14.0,20.0,-14.0)
            // Too long H cause path to ignore even though we clip. I don't know the reason.
            // H(400000.0)
            H(40000.0)
            v(40.0)
            H(845.2724)
            s(-225.272,467.0,-225.272,467.0)
            s(-235.0,486.0,-235.0,486.0)
            c(-2.7,4.7,-9.0,7.0,-19.0,7.0)
            c(-6.0,0.0,-10.0,-1.0,-12.0,-3.0)
            s(-194.0,-422.0,-194.0,-422.0)
            s(-65.0,47.0,-65.0,47.0)
            z()
            M(834.0, 80.0)
            // H(400000.0)
            H(40000.0)
            v(40.0)
            H(845.0)
            z()
        }

    }
}


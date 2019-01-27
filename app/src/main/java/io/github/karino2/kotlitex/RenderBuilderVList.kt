package io.github.karino2.kotlitex


sealed class VListChild

open class VListElem(
    val elem: RenderNode,
    val marginLeft: String? = null,
    val marginRight: String? = null,
    val wrapperClasses: MutableSet<CssClass> = mutableSetOf(),
    val wrapperStyle: CssStyle = CssStyle()) : VListChild()

class VListElemAndShift(elem: RenderNode,
                        marginLeft: String? = null,
                        marginRight: String? = null,
                        wrapperClasses: MutableSet<CssClass> = mutableSetOf(),
                        wrapperStyle: CssStyle = CssStyle(), val shift: Double = 0.0) : VListElem(elem, marginLeft, marginRight, wrapperClasses, wrapperStyle)


class VListKern(val size: Double) : VListChild()

enum class PositionType {
    IndividualShift, Top, Bottom, Shift, FirstBaseLine
}

sealed class VListParam {
    abstract val positionType: PositionType
}
data class VListParamIndividual(val children: List<VListElemAndShift>): VListParam() {
    override val positionType: PositionType
        get() = PositionType.IndividualShift
}

// Top, Bottom, Shift
data class VListParamPositioned(override val positionType: PositionType, val positionData: Double, val children: List<VListChild>) : VListParam()

data class VListParamFirstBaseLine(val children: List<VListChild>) : VListParam(){
    override val positionType : PositionType
        get() = PositionType.FirstBaseLine
}

object RenderBuilderVList {

    fun makeSpan(klasses: MutableSet<CssClass> = mutableSetOf(), children: MutableList<RenderNode> = mutableListOf(), options: Options? = null, style: CssStyle = CssStyle()) : RNodeSpan = RenderTreeBuilder.makeSpan(klasses, children, options, style)

    // Computes the updated `children` list and the overall depth.
    //
    // This helper function for makeVList makes it easier to enforce type safety by
    // allowing early exits (returns) in the logic.
    fun getVListChildrenAndDepth(params: VListParam):  Pair<List<VListChild>, Double>{
        when(params) {
            is VListParamIndividual -> {
                val oldChildren = params.children;
                val children : MutableList<VListChild> = mutableListOf(oldChildren[0])

                // Add in kerns to the list of params.children to get each element to be
                // shifted to the correct specified shift
                val depth = -oldChildren[0].shift - oldChildren[0].elem.depth;
                var currPos = depth
                for(i in 1 until oldChildren.size) {
                    val diff = -oldChildren[i].shift - currPos -
                            oldChildren[i].elem.depth;
                    val size = diff -
                            (oldChildren[i - 1].elem.height +
                                    oldChildren[i - 1].elem.depth);

                    currPos += diff;

                    children.add(VListKern(size))
                    children.add(oldChildren[i])
                }

                return Pair(children, depth)

            }
            is VListParamFirstBaseLine -> {
                val firstChild = params.children[0] as? VListElem ?: throw Exception("First child must have type \"elem\".")
                return Pair(params.children, -firstChild.elem.depth)
            }
            is VListParamPositioned -> {
                val depth = when(params.positionType) {
                    PositionType.Top -> {
                        // We always start at the bottom, so calculate the bottom by adding up
                        // all the sizes
                        var bottom = params.positionData;
                        for (i in 0 until params.children.size) {
                            val child = params.children[i];

                            bottom -= when(child) {
                                is VListKern -> child.size
                                is VListElem -> child.elem.height + child.elem.depth
                            }
                        }
                        bottom
                    }
                    PositionType.Bottom-> {
                        -params.positionData;
                    }
                    else -> {
                        val firstChild = params.children[0] as? VListElem ?: throw Exception("First child must have type \"elem\".")
                        if (params.positionType == PositionType.Shift) {
                            -firstChild.elem.depth - params.positionData;
                        } else {
                            throw Exception("Invalid positionType ${params.positionType}.")
                        }
                    }
                }
                return Pair(params.children, depth)
            }
        }
    }


    /**
     * Makes a vertical list by stacking elements and kerns on top of each other.
     * Allows for many different ways of specifying the positioning method.
     *
     * See VListParam documentation above.
     */
    fun makeVList(params: VListParam, options: Options): RNodeSpan {
        val (children, depth) = getVListChildrenAndDepth(params);

        // Create a strut that is taller than any list item. The strut is added to
        // each item, where it will determine the item's baseline. Since it has
        // `overflow:hidden`, the strut's top edge will sit on the item's line box's
        // top edge and the strut's bottom edge will sit on the item's baseline,
        // with no additional line-height spacing. This allows the item baseline to
        // be positioned precisely without worrying about font ascent and
        // line-height.
        var pstrutSize = 0.0
        for(child in children) {
            if (child is VListElem) {
                val elem = child.elem;
                pstrutSize = maxOf(pstrutSize, elem.maxFontSize, elem.height);
            }
        }
        pstrutSize += 2
        val pstrut = makeSpan(mutableSetOf(CssClass.pstruct))
        pstrut.style.height = pstrutSize.toString() + "em"

        // Create a new list of actual children at the correct offsets
        val realChildren :MutableList<RenderNode> = mutableListOf()
        var minPos = depth
        var maxPos = depth
        var currPos = depth
        for (child in children) {
            when(child) {
                is VListKern -> {
                    currPos += child.size;
                }
                is VListElem -> {
                    val elem = child.elem;
                    val classes = child.wrapperClasses
                    val style = child.wrapperStyle

                    val childWrap = makeSpan(classes, mutableListOf(pstrut, elem), null, style);
                    childWrap.style.top = (-pstrutSize - currPos - elem.depth).toString() + "em";
                    if (child.marginLeft != null) {
                        childWrap.style.marginLeft = child.marginLeft;
                    }
                    if (child.marginRight != null) {
                        childWrap.style.marginRight = child.marginRight;
                    }

                    realChildren.add(childWrap)
                    currPos += elem.height + elem.depth;
                }
            }
            minPos = Math.min(minPos, currPos);
            maxPos = Math.max(maxPos, currPos);
        }

        // The vlist contents go in a table-cell with `vertical-align:bottom`.
        // This cell's bottom edge will determine the containing table's baseline
        // without overly expanding the containing line-box.
        val vlist = makeSpan(mutableSetOf(CssClass.vlist), realChildren);
        vlist.style.height = maxPos.toString() + "em";

        // A second row is used if necessary to represent the vlist's depth.
        val rows : MutableList<RenderNode> = if (minPos < 0) {
            // We will define depth in an empty span with display: table-cell.
            // It should render with the height that we define. But Chrome, in
            // contenteditable mode only, treats that span as if it contains some
            // text content. And that min-height over-rides our desired height.
            // So we put another empty span inside the depth strut span.
            val emptySpan = makeSpan();
            val depthStrut = makeSpan(mutableSetOf(CssClass.vlist), mutableListOf(emptySpan));
            depthStrut.style.height = (-minPos).toString() + "em"

            // Safari wants the first row to have inline content; otherwise it
            // puts the bottom of the *second* row on the baseline.
            val topStrut = makeSpan(mutableSetOf(CssClass.vlist_s), mutableListOf(RNodeSymbol("\u200b")))

            mutableListOf(makeSpan(mutableSetOf(CssClass.vlist_r), mutableListOf(vlist, topStrut)),
                makeSpan(mutableSetOf(CssClass.vlist_r), mutableListOf(depthStrut)))
        } else {
            mutableListOf(makeSpan(mutableSetOf(CssClass.vlist_r), mutableListOf(vlist)))
        }

        val vtable = makeSpan(mutableSetOf(CssClass.vlist_t), rows)
        if (rows.size == 2) {
            vtable.klasses.add(CssClass.vlist_t2)
        }
        vtable.height = maxPos;
        vtable.depth = -minPos;
        return vtable;
    }

}
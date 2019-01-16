package io.github.karino2.kotlitex

data class FontMetrics(val cssEmPerMu: Double,
                       val slant: Double,
                       val space: Double,
                       val stretch: Double,
                       val shrink: Double,
                       val xHeight: Double,
                       val quad: Int,
                       val extraSpace: Double,
                       val num1: Double,
                       val num2: Double,
                       val num3: Double,
                       val denom1: Double,
                       val denom2: Double,
                       val sup1: Double,
                       val sup2: Double,
                       val sup3: Double,
                       val sub1: Double,
                       val sub2: Double,
                       val supDrop: Double,
                       val subDrop: Double,
                       val delim1: Double,
                       val delim2: Double,
                       val axisHeight: Double,
                       val defaultRuleThickness: Double,
                       val bigOpSpacing1: Double,
                       val bigOpSpacing2: Double,
                       val bigOpSpacing3: Double,
                       val bigOpSpacing4: Double,
                       val bigOpSpacing5: Double,
                       val sqrtRuleThickness: Double,
                       val ptPerEm: Int,
                       val doubleRuleSep: Double
) {
    companion object {
        /**
         * This file contains metrics regarding fonts and individual symbols. The sigma
         * and xi variables, as well as the metricMap map contain data extracted from
         * TeX, TeX font metrics, and the TTF files. These data are then exposed via the
         * `metrics` variable and the getCharacterMetrics function.
         */

        // In TeX, there are actually three sets of dimensions, one for each of
        // textstyle (size index 5 and higher: >=9pt), scriptstyle (size index 3 and 4:
        // 7-8pt), and scriptscriptstyle (size index 1 and 2: 5-6pt).  These are
        // provided in the the arrays below, in that order.
        //
        // The font metrics are stored in fonts cmsy10, cmsy7, and cmsy5 respsectively.
        // This was determined by running the following script:
        //
        //     latex -interaction=nonstopmode \
        //     '\documentclass{article}\usepackage{amsmath}\begin{document}' \
        //     '$a$ \expandafter\show\the\textfont2' \
        //     '\expandafter\show\the\scriptfont2' \
        //     '\expandafter\show\the\scriptscriptfont2' \
        //     '\stop'
        //
        // The metrics themselves were retreived using the following commands:
        //
        //     tftopl cmsy10
        //     tftopl cmsy7
        //     tftopl cmsy5
        //
        // The output of each of these commands is quite lengthy.  The only part we
        // care about is the FONTDIMEN section. Each value is measured in EMs.
        val sigmasAndXis = mapOf(
            "slant" to arrayOf(0.250, 0.250, 0.250),       // sigma1
            "space" to arrayOf(0.000, 0.000, 0.000),       // sigma2
            "stretch" to arrayOf(0.000, 0.000, 0.000),     // sigma3
            "shrink" to arrayOf(0.000, 0.000, 0.000),      // sigma4
            "xHeight" to arrayOf(0.431, 0.431, 0.431),     // sigma5
            "quad" to arrayOf(1.000, 1.171, 1.472),        // sigma6
            "extraSpace" to arrayOf(0.000, 0.000, 0.000),  // sigma7
            "num1" to arrayOf(0.677, 0.732, 0.925),        // sigma8
            "num2" to arrayOf(0.394, 0.384, 0.387),        // sigma9
            "num3" to arrayOf(0.444, 0.471, 0.504),        // sigma10
            "denom1" to arrayOf(0.686, 0.752, 1.025),      // sigma11
            "denom2" to arrayOf(0.345, 0.344, 0.532),      // sigma12
            "sup1" to arrayOf(0.413, 0.503, 0.504),        // sigma13
            "sup2" to arrayOf(0.363, 0.431, 0.404),        // sigma14
            "sup3" to arrayOf(0.289, 0.286, 0.294),        // sigma15
            "sub1" to arrayOf(0.150, 0.143, 0.200),        // sigma16
            "sub2" to arrayOf(0.247, 0.286, 0.400),        // sigma17
            "supDrop" to arrayOf(0.386, 0.353, 0.494),     // sigma18
            "subDrop" to arrayOf(0.050, 0.071, 0.100),     // sigma19
            "delim1" to arrayOf(2.390, 1.700, 1.980),      // sigma20
            "delim2" to arrayOf(1.010, 1.157, 1.420),      // sigma21
            "axisHeight" to arrayOf(0.250, 0.250, 0.250),  // sigma22

            // These font metrics are extracted from TeX by using tftopl on cmex10.tfm;
            // they correspond to the font parameters of the extension fonts (family 3).
            // See the TeXbook, page 441. In AMSTeX, the extension fonts scale; to
            // match cmex7, we'd use cmex7.tfm values for script and scriptscript
            // values.
            "defaultRuleThickness" to arrayOf(0.04, 0.049, 0.049), // xi8; cmex7: 0.049
            "bigOpSpacing1" to arrayOf(0.111, 0.111, 0.111),       // xi9
            "bigOpSpacing2" to arrayOf(0.166, 0.166, 0.166),       // xi10
            "bigOpSpacing3" to arrayOf(0.2, 0.2, 0.2),             // xi11
            "bigOpSpacing4" to arrayOf(0.6, 0.611, 0.611),         // xi12; cmex7: 0.611
            "bigOpSpacing5" to arrayOf(0.1, 0.143, 0.143),         // xi13; cmex7: 0.143

            // The \sqrt rule width is taken from the height of the surd character.
            // Since we use the same font at all sizes, this thickness doesn't scale.
            "sqrtRuleThickness" to arrayOf(0.04, 0.04, 0.04),

            // This value determines how large a pt is, for metrics which are defined
            // in terms of pts.
            // This value is also used in katex.less; if you change it make sure the
            // values match.
            "ptPerEm" to arrayOf(10.0, 10.0, 10.0),

            // The space between adjacent `|` columns in an array definition. From
            // `\showthe\doublerulesep` in LaTeX. Equals 2.0 / ptPerEm.
            "doubleRuleSep" to arrayOf(0.2, 0.2, 0.2)
        )

        // Original code is too JS specific way. We just write down without thinking...

        val sigmasAndXisKeyList = listOf(
            "slant",       // sigma1
            "space",       // sigma2
            "stretch",     // sigma3
            "shrink",      // sigma4
            "xHeight",     // sigma5
            "quad",        // sigma6
            "extraSpace",  // sigma7
            "num1",        // sigma8
            "num2",        // sigma9
            "num3",        // sigma10
            "denom1",      // sigma11
            "denom2",      // sigma12
            "sup1",        // sigma13
            "sup2",        // sigma14
            "sup3",        // sigma15
            "sub1",        // sigma16
            "sub2",        // sigma17
            "supDrop",     // sigma18
            "subDrop",     // sigma19
            "delim1",      // sigma20
            "delim2",      // sigma21
            "axisHeight",  // sigma22

            // These font metrics are extracted from TeX by using tftopl on cmex10.tfm;
            // they correspond to the font parameters of the extension fonts (family 3).
            // See the TeXbook, page 441. In AMSTeX, the extension fonts scale; to
            // match cmex7, we'd use cmex7.tfm values for script and scriptscript
            // values.
            "defaultRuleThickness", // xi8; cmex7: 0.049
            "bigOpSpacing1",       // xi9
            "bigOpSpacing2",       // xi10
            "bigOpSpacing3",             // xi11
            "bigOpSpacing4",         // xi12; cmex7: 0.611
            "bigOpSpacing5",         // xi13; cmex7: 0.143

            // The \sqrt rule width is taken from the height of the surd character.
            // Since we use the same font at all sizes, this thickness doesn't scale.
            "sqrtRuleThickness",

            // This value determines how large a pt is, for metrics which are defined
            // in terms of pts.
            // This value is also used in katex.less; if you change it make sure the
            // values match.
            "ptPerEm",

            // The space between adjacent `|` columns in an array definition. From
            // `\showthe\doublerulesep` in LaTeX. Equals 2.0 / ptPerEm.
            "doubleRuleSep"
        )
        fun createFontMetrics(cssEmPerMu : Double, argVals: List<Double>) : FontMetrics {
            val slant = argVals[0]
            val space = argVals[1]
            val stretch = argVals[2]
            val shrink = argVals[3]
            val xHeight = argVals[4]
            val quad = argVals[5].toInt()
            val extraSpace = argVals[6]
            val num1 = argVals[7]
            val num2 = argVals[8]
            val num3 = argVals[9]
            val denom1 = argVals[10]
            val denom2 = argVals[11]
            val sup1 = argVals[12]
            val sup2 = argVals[13]
            val sup3 = argVals[14]
            val sub1 = argVals[15]
            val sub2 = argVals[16]
            val supDrop = argVals[17]
            val subDrop = argVals[18]
            val delim1 = argVals[19]
            val delim2 = argVals[20]
            val axisHeight = argVals[21]
            val defaultRuleThickness = argVals[22]
            val bigOpSpacing1 = argVals[23]
            val bigOpSpacing2 = argVals[24]
            val bigOpSpacing3 = argVals[25]
            val bigOpSpacing4 = argVals[26]
            val bigOpSpacing5 = argVals[27]
            val sqrtRuleThickness = argVals[28]
            val ptPerEm = argVals[29].toInt()
            val doubleRuleSep = argVals[30]

            return FontMetrics(cssEmPerMu,
            slant,
            space,
            stretch,
            shrink,
            xHeight,
            quad,
            extraSpace,
            num1,
            num2,
            num3,
            denom1,
            denom2,
            sup1,
            sup2,
            sup3,
            sub1,
            sub2,
            supDrop,
            subDrop,
            delim1,
            delim2,
            axisHeight,
            defaultRuleThickness,
            bigOpSpacing1,
            bigOpSpacing2,
            bigOpSpacing3,
            bigOpSpacing4,
            bigOpSpacing5,
            sqrtRuleThickness,
            ptPerEm,
            doubleRuleSep
            )
        }
        var fontMetricsBySizeIndex = mutableMapOf<Int, FontMetrics>()

        /**
         * Get the font metrics for a given size.
         */
        fun getGlobalMetrics(size: Int) : FontMetrics {
            val sizeIndex = when {
                size >= 5 -> 0
                size >= 3 -> 1
                else -> 2
            }
            if(!fontMetricsBySizeIndex.containsKey(sizeIndex)) {
                val cssEmPerMu = sigmasAndXis["quad"]!![sizeIndex] / 18.0
                val restVals =   sigmasAndXisKeyList.map { sigmasAndXis.get(it)!![sizeIndex]}

                fontMetricsBySizeIndex[sizeIndex] = createFontMetrics(cssEmPerMu, restVals)
            }
            return fontMetricsBySizeIndex[sizeIndex]!!
        }
    }
}
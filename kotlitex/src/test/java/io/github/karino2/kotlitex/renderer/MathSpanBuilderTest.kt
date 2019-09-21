package io.github.karino2.kotlitex.renderer

import io.github.karino2.kotlitex.view.MathSpanBuilder
import io.github.karino2.kotlitex.view.MathSpanHandler
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class MathSpanBuilderTest {
    class HandlerForTest : MathSpanHandler {
        var counter = 0
        val normals = ArrayList<Pair<Int, String>>()
        val displayMathExps = ArrayList<Pair<Int, String>>()
        val inlineMathExps = ArrayList<Pair<Int, String>>()
        val eols = ArrayList<Int>()

        fun reset() {
            counter = 0
            normals.clear()
            displayMathExps.clear()
            inlineMathExps.clear()
            eols.clear()
        }

        override fun appendNormal(text: String) {
            normals.add(Pair(counter++, text))
        }

        override fun appendDisplayMathExp(text: String) {
            displayMathExps.add(Pair(counter++, text))
        }

        override fun appendInlineMathExp(exp: String) {
            inlineMathExps.add(Pair(counter++, exp))
        }

        override fun appendEndOfLine() {
            eols.add(counter++)
        }

    }

    val handler = HandlerForTest()

    val target = MathSpanBuilder(handler)

    @Test
    fun testMathExpPat_MathLine() {
        val actual = target.mathExpLinePat.matches("\$\$x^2\$\$")
        assertTrue(actual)
    }

    @Test
    fun testMathExpPat_MathExpButNotMathLineEnd() {
        val actual = target.mathExpLinePat.matches("\$\$x^2\$\$abc")
        assertFalse(actual)
    }
    @Test
    fun testMathExpPat_MathExpButNotMathLineBegin() {
        val actual = target.mathExpLinePat.matches("123\$\$x^2\$\$")
        assertFalse(actual)
    }

    @Test
    fun testMathExpPath() {
        val actual = target.mathExpPat.find("abc\$\$x_2\$\$")
        assertNotNull(actual)
    }

    @Test
    fun testMathExpPat_MathBegEnd_ButNotMathLine() {
        val actual = target.mathExpLinePat.matches("\$\$x^2\$\$abc\$\$y_2\$\$")
        assertFalse(actual)
    }

    @Before
    fun setupHandler() {
        handler.reset()
    }


    fun Pair<Int, String>.assert(expectPos:Int, expectVal: String) {
        assertEquals(expectPos, this.first)
        assertEquals(expectVal, this.second)
    }

    @Test
    fun testMathLine() {
        target.oneLine("\$\$x^2\$\$")
        handler.displayMathExps[0].assert(0, "x^2")
    }

    @Test
    fun testOneLine_EmptyLine() {
        target.oneLine("")

        assertTrue(handler.displayMathExps.isEmpty())
        assertTrue(handler.inlineMathExps.isEmpty())
        assertTrue(handler.normals.isEmpty())
        assertEquals(1, handler.eols.size)
        assertEquals(0, handler.eols[0])
    }


    @Test
    fun testOneLine_Mixed() {
        target.oneLine("abc\$\$x^2\$\$")

        assertTrue(handler.displayMathExps.isEmpty())
        assertEquals(1, handler.inlineMathExps.size)
        assertEquals(1, handler.normals.size)
        assertEquals(1, handler.eols.size)

        handler.normals[0].assert(0, "abc")
        handler.inlineMathExps[0].assert(1, "x^2")
        assertEquals(2, handler.eols[0])
    }

    @Test
    fun testOneNormal_normalEnd() {
        target.oneLine("\$\$x^2\$\$def")
        handler.normals[0].assert(1, "def")
        handler.inlineMathExps[0].assert(0, "x^2")
    }

    @Test
    fun testOneNormal_complex() {
        target.oneLine("abc\$\$x^2\$\$def\$\$y_2\$\$ghi")

        assertEquals(3, handler.normals.size)
        assertEquals(2, handler.inlineMathExps.size)

        handler.normals[0].assert(0, "abc")
        handler.normals[1].assert(2, "def")
        handler.normals[2].assert(4, "ghi")

        handler.inlineMathExps[0].assert(1, "x^2")
        handler.inlineMathExps[1].assert(3, "y_2")

    }

    @Test
    fun testOneNormal_noMath() {
        target.oneLine("abc def")

        assertEquals(1, handler.normals.size)
        assertTrue(handler.inlineMathExps.isEmpty())

        handler.normals[0].assert(0, "abc def")
    }

    @Test
    fun testOneNormal_mathStart_mathEnd() {
        target.oneLine("\$\$x^2\$\$abc\$\$y_2\$\$")

        assertEquals(1, handler.normals.size)
        assertEquals(2, handler.inlineMathExps.size)

        handler.normals[0].assert(1, "abc")

        handler.inlineMathExps[0].assert(0, "x^2")
        handler.inlineMathExps[1].assert(2, "y_2")

    }

    @Test
    fun testOneNormal_MathExpButNotMathLineEnd() {
        target.oneLine("\$\$x^2\$\$abc")

        assertEquals(1, handler.normals.size)
        assertEquals(1, handler.inlineMathExps.size)

        handler.normals[0].assert(1, "abc")

        handler.inlineMathExps[0].assert(0, "x^2")
    }


    @Test
    fun testMultiLineMathBegin() {
        assertFalse(target.inMultiLineMath)
        target.oneLine("\$\$")
        assertTrue(target.inMultiLineMath)
        assertEquals(0, handler.normals.size)
        assertEquals(0, handler.inlineMathExps.size)
    }

    @Test
    fun testMultiLineMathEndWithEmpty() {
        target.oneLine("\$\$")
        target.oneLine("\$\$")
        assertFalse(target.inMultiLineMath)
        assertEquals(0, handler.normals.size)
        assertEquals(0, handler.displayMathExps.size)
    }

    @Test
    fun testMultiLineMath() {
        target.oneLine("\$\$")
        target.oneLine("x+y \\\\")
        target.oneLine("z+w")
        target.oneLine("\$\$")
        assertFalse(target.inMultiLineMath)
        assertEquals(1, handler.displayMathExps.size)
        assertEquals(0, handler.normals.size)
    }

    @Test
    fun testMultiLineMathAfter() {
        target.oneLine("\$\$")
        target.oneLine("x+y \\\\")
        target.oneLine("z+w")
        target.oneLine("\$\$")
        assertFalse(target.inMultiLineMath)
        target.oneLine("abc")
        assertEquals(1, handler.displayMathExps.size)
        assertEquals(1, handler.normals.size)
    }

}
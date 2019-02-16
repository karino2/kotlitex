package io.github.karino2.kotlitex

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class RenderTreeBuilderInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("io.github.karino2.kotlitex.test", appContext.packageName)
    }

    fun parse(input: String) : List<ParseNode> {
        val parser = Parser(input)
        return  parser.parse()
    }

    val options = Options(Style.TEXT)
    // This is used to check intermediate result by debugger.
    @Test
    fun buildExpression_sqrt_call_success() {
        val input = parse("\\sqrt{3}")
        val actual = RenderTreeBuilder.buildExpression(input, options, true)

        assertTrue(actual.isNotEmpty())
    }

    @Test
    fun buildExpression_sqrt() {
        val opt = Options(Style.DISPLAY)
        val input = parse("\\sqrt{3}")
        val actual = RenderTreeBuilder.buildExpression(input, opt, true)

        assertEquals(1, actual.size)
        val target = actual[0]

        assertSpan(target) { ac(0) { ac(0){ ac(0){ac(1)
        {
            styleTop("-2.916095em")
        }
        }}}}
    }



}

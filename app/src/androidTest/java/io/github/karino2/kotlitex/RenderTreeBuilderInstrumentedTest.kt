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

    @Test
    fun buildExpression_sqrt() {
        val opt = Options(Style.DISPLAY)
        val input = parse("\\sqrt{3}")
        val actual = RenderTreeBuilder.buildExpression(input, opt, true)

        assertEquals(1, actual.size)
        val target = actual[0]

        assertSpan(target) { ac(0) { ac(0){
            ac(0){
                style(CssStyle("0.956095em"))
                ac(0) {
                    // svg-align
                    ac(1) {
                        style(CssStyle(paddingLeft="0.833em"))
                    }
                }
                ac(1) {
                    styleTop("-2.916095em")
                }
            }}}}
    }



}

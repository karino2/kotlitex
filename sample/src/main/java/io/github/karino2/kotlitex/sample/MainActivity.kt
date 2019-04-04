package io.github.karino2.kotlitex.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.TextView
import io.github.karino2.kotlitex.MarkdownView
import io.github.karino2.kotlitex.MathExpressionSpan

class MainActivity : AppCompatActivity() {

    // private val PHYSICAL_BASE_SIZE = 100.0f // big size for debug
    // private val PHYSICAL_BASE_SIZE = 42f // textView.textSize, 14sp in my device.
    private val PHYSICAL_BASE_SIZE = 72f // 24sp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)
        // Log.d("kotlitex", "textSize = ${textView.textSize}")
        val spannable = SpannableStringBuilder("01234567 91 345 789\n234 678 and the remaining text.")
        spannable.setSpan(createMathSpan("x^2", PHYSICAL_BASE_SIZE), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(createMathSpan("\\frac{1}{2}", PHYSICAL_BASE_SIZE), 2, 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(createMathSpan("\\sqrt{3}", PHYSICAL_BASE_SIZE), 4, 6, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(createMathSpan("\\frac{1}{1+\\frac{1}{x^2}}", PHYSICAL_BASE_SIZE), 6, 8, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(createMathSpan("\\sum^N_{k=1} k", PHYSICAL_BASE_SIZE), 22, 25, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        textView.text = spannable

        findViewById<MarkdownView>(R.id.markdownView).setMarkdown("""Hello, this is math test ${"$$"}x^2${"$$"} end.
            |${"$$"} \sum^N_{k=1} k${"$$"}
            |${"$$"} \mathcal{X} = \{1, 2, 3\} ${"$$"}
            |Above is math line. This is a little different from inline text mode like ${"$$"} \sum^N_{k=1} k${"$$"}.
            |${"$$"} \sqrt{5} ${"$$"}
        """.trimMargin())
    }

    fun createMathSpan(expr: String, baseSize: Float) = MathExpressionSpan(expr, baseSize, assets, true)
}

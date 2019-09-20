package io.github.karino2.kotlitex.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.TextView
import io.github.karino2.kotlitex.view.MarkdownView
import io.github.karino2.kotlitex.view.MathExpressionSpan

class MainActivity : AppCompatActivity() {

    // private val PHYSICAL_BASE_SIZE = 100.0f // big size for debug
    // private val PHYSICAL_BASE_SIZE = 42f // textView.textSize, 14sp in my device.
    private val PHYSICAL_BASE_SIZE = 72f // 24sp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)
        val spannable = SpannableStringBuilder("01234 This is direct math span test.")
        spannable.setSpan(createMathSpan("x^2", PHYSICAL_BASE_SIZE), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        textView.text = spannable

        MarkdownView.CACHE_ENABLED = false
        findViewById<MarkdownView>(R.id.markdownView).setMarkdown("""yyyy gg. No math line with large descent(y).
            |${"$$"} \sum^N_{k=1} k${"$$"}
            |Inline math ${"$$"}x^2${"$$"} support.
            |${"$$"} \mathbb{R} ${"$$"}, ${"$$"} \mathscr{F} ${"$$"}, ${"$$"} \bar{A} ${"$$"},
            |${"$$"} x\ y${"$$"}
            |${"$$"} \mathcal{X} = \{1, 2, 3\} ${"$$"}
            |${"$$"}P_x(a) = \frac{N(a|x)}{n} ${"$$"}
            |${"$$"}\underline{p_i} ${"$$"}
            |Some text between math lines.
            |${"$$"} \frac{1}{1+\frac{1}{x^2}} ${"$$"}
            |${"$$"} a\bm{a} v\bm{v} ${"$$"}
            |${"$$"} x_1 \ldots x_n ${"$$"}
            |${"$$"} \sqrt{5} \log x${"$$"}
            |${"$$"} \lim_{t \to 0} (1+t)^{\frac{1}{t}} ${"$$"}
            |${"$$"} o_t = tanh(W_c[h_t; c_t]) ${"$$"}
            |Above are math lines. These are a little different from inline text mode like ${"$$"} \sum^N_{k=1} k${"$$"}.
            |${"$$"} \sqrt{5} ${"$$"} text ${"$$"} \sum^N_{k=1} k${"$$"}
        """.trimMargin())
    }

    fun createMathSpan(expr: String, baseSize: Float) =
        MathExpressionSpan(expr, baseSize, assets, true)
}

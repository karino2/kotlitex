package io.github.karino2.kotlitex.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.TextView
import io.github.karino2.kotlitex.MathExpressionSpan

class MainActivity : AppCompatActivity() {
    private val BIGGER_SIZE_FOR_DEBUGGING = 100.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)
        val spannable = SpannableStringBuilder("01234567 91 345 789 234 678 and the remaining text.")

        spannable.setSpan(createMathSpan("x^2", BIGGER_SIZE_FOR_DEBUGGING), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(createMathSpan("\\frac{1}{2}", BIGGER_SIZE_FOR_DEBUGGING), 2, 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(createMathSpan("\\sqrt{3}", BIGGER_SIZE_FOR_DEBUGGING), 4, 6, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(createMathSpan("\\frac{1}{1+\\frac{1}{x^2}}", BIGGER_SIZE_FOR_DEBUGGING), 6, 8, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(createMathSpan("\\sum^N_{k=1} k", BIGGER_SIZE_FOR_DEBUGGING), 22, 25, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        textView.text = spannable
    }

    fun createMathSpan(expr: String, baseSize: Float) = MathExpressionSpan(expr, baseSize, assets)
}

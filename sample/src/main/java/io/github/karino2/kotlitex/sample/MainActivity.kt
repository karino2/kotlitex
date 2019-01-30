package io.github.karino2.kotlitex.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.widget.TextView
import io.github.karino2.kotlitex.MathExpressionSpan

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textView)
        val spannable = SpannableStringBuilder("0123 and the remaining text.")
        spannable.setSpan(MathExpressionSpan("x^2"), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable.setSpan(MathExpressionSpan("\\frac{1}{2}"), 2, 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        textView.text = spannable
    }
}

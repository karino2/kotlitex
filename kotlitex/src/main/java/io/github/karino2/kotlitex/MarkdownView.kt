package io.github.karino2.kotlitex

import android.content.Context
import android.content.res.AssetManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.widget.TextView
import kotlinx.coroutines.*

interface MathSpanHandler {
    fun appendNormal(text: String)
    fun appendMathExp(exp: String)
    fun appendMathLineExp(text: String)
    fun appendEndOfLine()
}

// to make this class unit testable.
class MathSpanBuilder(val handler: MathSpanHandler) {
    val builder = SpannableStringBuilder()

    val mathExpLinePat = "^\\$\\$(.*)\\$\\$\$".toRegex()
    val mathExpPat = "\\$\\$([^$]+)\\$\\$".toRegex()
    // val mathExpPat = "\\$\\$(.*)\$\$".toRegex()

    fun oneNormalLineWithoutEOL(line: String) {
        if(line.isEmpty())
            return

        var lastMatchPos = 0
        var res = mathExpPat.find(line)
        if(res == null) {
            handler.appendNormal(line)
            return
        }

        while(res != null) {
            if(lastMatchPos != res.range.start)
                handler.appendNormal(line.substring(lastMatchPos, res.range.start))
            handler.appendMathExp(res.groupValues[1])
            lastMatchPos = res.range.last+1
            res = res.next()
        }
        if(lastMatchPos != line.length)
            handler.appendNormal(line.substring(lastMatchPos))
    }

    fun oneLine(line:String) {
        mathExpLinePat.matchEntire(line)?.let {
            handler.appendMathLineExp(it.groupValues[1])
            return
        }
        oneNormalLineWithoutEOL(line)
        handler.appendEndOfLine()
    }

}

class SpannableMathSpanHandler(val assetManager: AssetManager, val baseSize: Float) : MathSpanHandler {
    fun reset() {
        // spannable.clear() seems slow.
        spannable = SpannableStringBuilder()
        isMathExist = false
    }

    var isMathExist = false

    override fun appendNormal(text: String) {
        spannable.append(text)
    }

    override fun appendMathExp(exp: String) {
        appendMathSpan(exp, false)
    }

    private fun appendMathSpan(exp: String, isMathMode: Boolean) {
        isMathExist = true
        val span = MathExpressionSpan(exp, baseSize, assetManager, isMathMode)
        val begin = spannable.length
        spannable.append("\$\$${exp}\$\$")
        spannable.setSpan(span, begin, spannable.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    override fun appendMathLineExp(text: String) {
        appendMathSpan(text, true)
        spannable.append("\n")
    }

    override fun appendEndOfLine() {
        spannable.append("\n")
    }

    var spannable = SpannableStringBuilder()


}

class MarkdownView(context : Context, attrSet: AttributeSet) : TextView(context, attrSet) {
    var job : Job? = null

    val handler by lazy {
        SpannableMathSpanHandler(context.assets, textSize)
    }

    val builder by lazy {
        MathSpanBuilder(handler)
    }

    fun setMarkdown(text: String) {
        job?.let { it.cancel() }
        handler.reset()
        setText(text)
        job = GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val lines = text.split("\n")
                repeat(lines.size) {
                    val line = lines[it]
                    builder.oneLine(line)
                }
            }
            if(handler.isMathExist) {
                withContext(Dispatchers.Main) {
                    setText(handler.spannable)
                }
            }
        }

    }

}
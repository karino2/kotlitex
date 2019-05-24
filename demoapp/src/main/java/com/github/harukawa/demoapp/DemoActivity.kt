package com.github.harukawa.demoapp

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import io.github.karino2.kotlitex.view.MarkdownView
import io.github.karino2.kotlitex.view.MathExpressionSpan
import kotlinx.android.synthetic.main.activity_demo.*

data class ViewHolder(val markdownView: MarkdownView)

class DemoActivity : AppCompatActivity() {

    private val PHYSICAL_BASE_SIZE = 72f // 24sp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        var data: MutableList<String> = mutableListOf()
        data.add("${"$$"} x^2 ${"$$"}")
        data.add("${"$$"} \\frac{1}{1+\\frac{1}{x^2}} ${"$$"}")
        data.add("${"$$"} x_1 \\ldots x_n ${"$$"}")
        data.add("""${"$$"}\underline{p_i} ${"$$"}
            |${"$$"} \frac{1}{1+\frac{1}{x^2}} ${"$$"}
            |${"$$"} x_1 \ldots x_n ${"$$"}
        """.trimMargin())
        data.add("""Hello
            |${"$$"} \sqrt{5} ${"$$"}
            |End
        """.trimMargin())

        val adapter: ArrayAdapter<String> = MarkListAdapter(this, data)

        val listView: ListView = findViewById<ListView>(R.id.list)
        listView.setAdapter(adapter)

        var listPosition: Int = 0
        var isListClickFlag: Boolean = false

        listView.setOnItemClickListener() { _, view, position, id ->
            isListClickFlag = true
            findViewById<TextView>(R.id.editText).text = data[position].toString()
            listPosition = position
        }

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener { view ->
            adapter.add("empty")
        }

        val editText: EditText = findViewById<EditText>(R.id.editText)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isListClickFlag == false) {
                    data[listPosition] = s.toString()
                    adapter.notifyDataSetChanged()
                }
                isListClickFlag = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }
    fun createMathSpan(expr: String, baseSize: Float) =
        MathExpressionSpan(expr, baseSize, assets, true)
}

class MarkListAdapter(context: Context, marks: List<String>) : ArrayAdapter<String>(context, 0, marks) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.list_markdown, parent, false)

        val markDown = view.findViewById<MarkdownView>(R.id.markdownView)

        markDown.setMarkdown(super.getItem(position).toString())

        return view
    }
}
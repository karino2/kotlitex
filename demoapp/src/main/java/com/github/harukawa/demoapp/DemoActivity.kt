package com.github.harukawa.demoapp

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import io.github.karino2.kotlitex.view.MarkdownView
import io.github.karino2.kotlitex.view.MathExpressionSpan

data class ViewHolder(val markdownView: MarkdownView)

class DemoActivity : AppCompatActivity() {

    private val PHYSICAL_BASE_SIZE = 72f // 24sp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        var data: MutableList<String> = mutableListOf()
        data.add("${"$$"} \\sum^N_{k=1} k${"$$"}")
        data.add("${"$$"} \\mathcal{X} = \\{1, 2, 3\\} ${"$$"}")

        var adapter: ArrayAdapter<String> = MarkListAdapter(this, data)

        val listView: ListView = findViewById<ListView>(R.id.list)
        listView.setAdapter(adapter)

        listView.setOnItemClickListener() { _, view, position, id ->
            var itemTextView: TextView = view.findViewById(android.R.id.text1)
            val spannable = SpannableStringBuilder("")
            spannable.setSpan(createMathSpan(data[position], PHYSICAL_BASE_SIZE), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            //findViewById<TextView>(R.id.editText).text = data[position].toString()
            findViewById<TextView>(R.id.editText).text = spannable
        }

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener { view ->
            adapter.add("empty")
        }

        var editText: EditText = findViewById<EditText>(R.id.editText)

        editText.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {

                    finish()
                    true
                }
                else -> false
            }
        }

    }
    fun createMathSpan(expr: String, baseSize: Float) =
        MathExpressionSpan(expr, baseSize, assets, true)
}

class MarkListAdapter(context: Context, marks: List<String>) : ArrayAdapter<String>(context, 0, marks) {
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val inflater : LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.list_markdown, parent, false)

        val markDown = view.findViewById<MarkdownView>(R.id.markdownView)

        markDown.setMarkdown(super.getItem(position).toString())

        return view
    }
}
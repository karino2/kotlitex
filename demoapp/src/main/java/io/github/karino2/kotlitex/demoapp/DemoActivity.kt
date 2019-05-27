package io.github.karino2.kotlitex.demoapp

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

class DemoActivity : AppCompatActivity() {

    val initialContents = arrayOf(
        """Here is kotlitex demo.
            |You can change the cell by select and edit.
            |Math expression should be enclosed with $$.
            |Like this. ${"$$"}x^2${"$$"}.
       """.trimMargin(),
        """If a line is solely math expression, this app handle the line as math mode. On the other hand, if the math expression is inside some other text, we treat it as inline mode.
            |Below here is math mode examples.
            |
            |${"$$"} \sum^N_{k=1} k${"$$"}
            |${"$$"} \frac{1}{1+\frac{1}{x^2}} ${"$$"}
            |${"$$"} x_1 \ldots x_n ${"$$"}
            |${"$$"} \sqrt{5} ${"$$"}
       """.trimMargin(),
        """  Here is inline mode example. Note that the same expression rendered a little differently from above math mode's.
            |${"$$"} \sum^N_{k=1} k${"$$"} inline.
            |${"$$"} \frac{1}{1+\frac{1}{x^2}} ${"$$"} inline.
            |${"$$"} x_1 \ldots x_n ${"$$"} inline.
            |${"$$"} \sqrt{5} ${"$$"} inline.
       """.trimMargin(),
        """Other examples.
            |${"$$"} \mathbb{R} ${"$$"}, ${"$$"} \mathscr{F} ${"$$"}, ${"$$"} \bar{A} ${"$$"},
            |${"$$"} \mathcal{X} = \{1, 2, 3\} ${"$$"}
            |${"$$"}P_x(a) = \frac{N(a|x)}{n} ${"$$"}
            |${"$$"}\underline{p_i} ${"$$"}
       """.trimMargin(),
        """${"$$"} \lim_{t \to 0} (1+t)^{\frac{1}{t}} ${"$$"}
            |${"$$"} o_t = tanh(W_c[h_t; c_t]) ${"$$"}
       """.trimMargin(),
        """Please modify cells or add another cell and put some math exp by yourself!
            |${"$$"} x^2 ${"$$"}
       """.trimMargin()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        var data: MutableList<String> = mutableListOf()
        initialContents.forEach { data.add(it) }

        val adapter: ArrayAdapter<String> = MarkListAdapter(this, data)

        val listView: ListView = findViewById<ListView>(R.id.list)
        listView.adapter = adapter

        var listPosition: Int = 0
        var isListClickFlag: Boolean = false

        listView.setOnItemClickListener() { _, _ /* view */, position, _ /* id */ ->
            isListClickFlag = true
            findViewById<TextView>(R.id.editText).text = data[position].toString()
            listPosition = position
        }

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener { _ /* view */ ->
            adapter.add("empty")
        }

        val editText: EditText = findViewById<EditText>(R.id.editText)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isListClickFlag) {
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
}

class MarkListAdapter(context: Context, marks: List<String>) : ArrayAdapter<String>(context, 0, marks) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.list_markdown, parent, false)

        val markDown = view.findViewById<MarkdownView>(R.id.markdownView)

        markDown.setMarkdown(super.getItem(position)?.toString() ?: "")

        return view
    }
}
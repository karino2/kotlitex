package com.github.harukawa.demoapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.github.harukawa.demoapp.R

class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        var data: MutableList<String> = mutableListOf()
        data.add("1");
        data.add("2");

        var adapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)

        val listView: ListView = findViewById(R.id.list)
        listView.setAdapter(adapter)

        listView.setOnItemClickListener()  {_, view, position, id ->
            var itemTextView : TextView = view.findViewById(android.R.id.text1)
            findViewById<TextView>(R.id.editText).text = data[position].toString()//itemTextView.text.toString()
        }

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {view ->
            adapter.add("empty")
        }

        var editText:EditText = findViewById(R.id.editText)

        editText.setOnEditorActionListener { v, actionId, event ->
            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {

                    finish()
                    true
                }
                else -> false
            }
        }
    }
}

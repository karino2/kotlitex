package io.github.karino2.kotlitex

import android.graphics.Color
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import io.github.karino2.kotlitex.renderer.VirtualNodeBuilder
import io.github.karino2.kotlitex.renderer.node.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VirtualNodeBuilderInstrumentedTest {
    val TAG = "VirtualNodeBuilderInstrumentedTest"
    private fun printTree(parent: VirtualCanvasNode, level: Int = 0) {
        when (parent) {
            is VirtualContainerNode<*> -> {
                println("  ".repeat(level) + "${parent.javaClass.simpleName} {")
                parent.nodes.forEach {
                    printTree(it, level + 1)
                }
                println("  ".repeat(level) + "}")
            }
            else -> {
                println("  ".repeat(level) + parent.toString())
            }
        }
    }

    @Test
    fun build() {
        val parser = Parser("\\sqrt{3}")
        val renderTree = RenderTreeBuilder.buildExpression(parser.parse(), Options(Style.DISPLAY), true)
        val builder = VirtualNodeBuilder(renderTree, 100.0)
        val virtualNodeTree = builder.build();

        println("\\sqrt{3}")
        printTree(virtualNodeTree)

        // TODO: Write actual assertions. Right now, this test is just for showing the tree.
        assertNotNull(virtualNodeTree)
    }
}
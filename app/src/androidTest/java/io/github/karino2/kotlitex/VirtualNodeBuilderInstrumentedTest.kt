package io.github.karino2.kotlitex

import android.support.test.runner.AndroidJUnit4
import io.github.karino2.kotlitex.renderer.VirtualNodeBuilder
import io.github.karino2.kotlitex.renderer.node.*
import org.junit.Assert.assertEquals
import org.junit.Test

import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VirtualNodeBuilderInstrumentedTest {
    private fun printTree(parent: RenderNode, level: Int) {
        when (parent) {
            is RNodeSpan -> {
                println("  ".repeat(level) + "${parent.javaClass.simpleName} {")
                parent.children.forEach {
                    printTree(it, level + 1)
                }
                println("  ".repeat(level) + "}")
            }
            else -> {
                println("  ".repeat(level) + parent.toString())
            }
        }
    }

    private fun printTree(parents: List<RenderNode>, level: Int = 0) {
        parents.forEach {
            printTree(it, level)
        }
    }

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

    private fun findFirst(parent: VirtualCanvasNode, f: (n: VirtualCanvasNode) -> Boolean): VirtualCanvasNode? {
        if (f(parent)) {
            return parent
        }

        when (parent) {
            is VirtualContainerNode<*> -> {
                parent.nodes.forEach {
                    val x = findFirst(it, f)
                    if (x != null) {
                        return x
                    }
                }
            }
        }
        return null
    }

    @Test
    fun build_sqrt() {
        val parser = Parser("\\sqrt{3}")
        val renderTree = RenderTreeBuilder.buildExpression(parser.parse(), Options(Style.DISPLAY), true)

        val builder = VirtualNodeBuilder(renderTree, 100.0)
        val virtualNodeTree = builder.build();

        val n = findFirst(virtualNodeTree) {
            (it is TextNode) && it.text == "3"
        }

        if (n is TextNode) {
            // This test may be flaky, but better than nothing
            assertEquals(83.3, n.bounds.x, 0.001)
        }
    }

    private fun printTrees(expr: String) {
        val parser = Parser(expr)
        val renderTree = RenderTreeBuilder.buildExpression(parser.parse(), Options(Style.DISPLAY), true)
        println("Render Tree")
        printTree(renderTree)

        val builder = VirtualNodeBuilder(renderTree, 100.0)
        val virtualNodeTree = builder.build();

        println("Virtual Node Tree")
        printTree(virtualNodeTree)
    }
}
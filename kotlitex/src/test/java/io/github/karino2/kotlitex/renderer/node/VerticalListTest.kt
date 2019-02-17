package io.github.karino2.kotlitex.renderer.node

import org.junit.Assert.*
import org.junit.Test

class VerticalListTest {
    @Test
    fun getNextNodePlacement() {
        val list = VerticalList(Alignment.CENTER, 10.0, setOf())
        list.margin.left = 20.0
        assertEquals(30, list.getNextNodePlacement().toInt())

        // Is it okay to ignore the list's margin.left?
        list.addRow(VerticalListRow(setOf()))
        assertEquals(10, list.getNextNodePlacement().toInt())
    }

    @Test
    fun getNextNodePlacementWithRowAndNode() {
        val list = VerticalList(Alignment.CENTER, 10.0, setOf())
        list.margin.left = 20.0
        assertEquals(30, list.getNextNodePlacement().toInt())

        val child = VerticalListRow(setOf())
        child.addNode(VerticalListRow(setOf()))

        list.addRow(child)
        assertEquals(0, list.getNextNodePlacement().toInt())
    }
}
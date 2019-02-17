package io.github.karino2.kotlitex.renderer.node

import org.junit.Test

import org.junit.Assert.*

class VirtualContainerNodeTest {

    @Test
    fun bounds() {
        val row = VerticalListRow(setOf())

        val a = VerticalListRow(setOf())
        a.bounds.x = 10.0
        a.bounds.y = 10.0
        a.bounds.width = 10.0
        a.bounds.height = 10.0
        row.addNode(a)

        val b = VerticalListRow(setOf())
        b.bounds.x = 10.0
        b.bounds.y = 10.0
        b.bounds.width = 20.0
        b.bounds.height = 40.0
        row.addNode(b)

        assertEquals(Bounds(10.0, 10.0, 20.0, 40.0), row.bounds)
    }
}
package io.github.karino2.kotlitex.renderer.node

import org.junit.Test

import org.junit.Assert.*

class BoundsTest {

    @Test
    fun extendWithSameOrigin() {
        val a = Bounds(0.0, 0.0, 1.0, 2.0)
        val b = Bounds(0.0, 0.0, 2.0, 4.0)
        a.extend(b)
        assertEquals(b, a)
    }

    @Test
    fun extendWithDifferentOrigin() {
        val a = Bounds(10.0, 10.0, 1.0, 2.0)
        val b = Bounds(0.0, 0.0, 2.0, 4.0)
        a.extend(b)

        // Include both a and b
        assertEquals(Bounds(0.0, 0.0, 11.0, 12.0), a)
    }
}
package io.github.karino2.kotlitex

import org.junit.Test

import org.junit.Assert.*


class LexerTest {
    @Test
    fun basicTest() {
        val lexer = Lexer("x ^ 2")
        var res1 = lexer.lex()
        assertEquals("x", res1.text)

        res1 = lexer.lex()
        assertEquals(" ", res1.text)

        res1 = lexer.lex()
        assertEquals("^", res1.text)
        assertEquals(2, res1.loc?.start)
        assertEquals(3, res1.loc?.end)

        res1 = lexer.lex()
        assertEquals(" ", res1.text)

        res1 = lexer.lex()
        assertEquals("2", res1.text)

        res1 = lexer.lex()
        assertEquals("EOF", res1.text)

    }
}

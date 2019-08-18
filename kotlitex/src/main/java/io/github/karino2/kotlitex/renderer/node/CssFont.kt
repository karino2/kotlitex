package io.github.karino2.kotlitex.renderer.node

/**
 * This class is designed to 1) run unit tests without mocking Android classes such as Typesafe and
 * 2) make the structure similar to canvas-latex.
 */
data class CssFont(val family: String, val variant: String, val weight: String, val size: Double) {
    constructor(family: String, size: Double) : this(family, "", "", size)
}
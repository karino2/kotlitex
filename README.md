# KotliTeX

[![Build Status](https://travis-ci.org/karino2/kotlitex.svg?branch=master)](https://travis-ci.org/karino2/kotlitex)

KotliTeX is [KaTeX](https://katex.org)'s Android port, written in Kotlin.

This library provides `com.karino2.kotlitex.MathExpressionSpan`.

## Porting Guidelines

Currently we are porting [KaTeX 0.10.0](https://github.com/KaTeX/KaTeX/releases/tag/v0.10.0) and [canvas-latex](https://github.com/CurriculumAssociates/canvas-latex) (which doesn't have releases) mostly as is, while merging the 2 parts may be technically possible.

We believe that keeping the original structure makes future updates easier.

## Coding Style

Use [ktlint](https://ktlint.github.io). `./gradlew format` format all .kt files with ktlint.

# Current status (2019/02/17)

Just start rendering something.
Unsupported element cause exception (and crash) for ease of development.

![Sccreen Shot](https://raw.githubusercontent.com/karino2/kotlitex/master/screen_shot.jpg)


(Partially) supported

- frac
- sqrt
- Sum, Prod

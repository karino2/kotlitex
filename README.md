# KotliTeX

[![Build Status](https://travis-ci.org/karino2/kotlitex.svg?branch=master)](https://travis-ci.org/karino2/kotlitex)

KotliTeX is [KaTeX](https://katex.org)'s Android port, written in Kotlin.

This library provides `com.karino2.kotlitex.view.MathExpressionSpan` and `com.karino2.kotlitex.view.MarkdownView`.

## Porting Guidelines

Currently we are porting [KaTeX 0.10.0](https://github.com/KaTeX/KaTeX/releases/tag/v0.10.0) and [canvas-latex](https://github.com/CurriculumAssociates/canvas-latex) (which doesn't have releases) mostly as is, while merging the 2 parts may be technically possible.

We believe that keeping the original structure makes future updates easier.

### How to port new function

We basically have two part in our library.
Expression tree building and rendering it.

Expression tree building is essentially the porting of KaTeX, while rendering is similar to canavs-latex (though this is sometime not the direct porting because of difference between HTML5 canvas and Android).

1. Add functions/FunctionXXX.kt, which is port of KaTeX/src/functions/XX.js and call defineAll() from Parser.kt (Expression tree part)
2. Add rendering logic under renderer/ someway

[mathcal PR](https://github.com/karino2/kotlitex/pull/106) is typical and good starting point for investigate.

## Coding Style

Use [ktlint](https://ktlint.github.io). `./gradlew format` format all .kt files with ktlint.

# Current status (2019/04/09)

MathExpression Span now render inside TextView.

We provide MarkdownView class, which parse math expression of jekyll style asynchronously. (Though other markup is not yet supported).

![Sccreen Shot](https://raw.githubusercontent.com/karino2/kotlitex/master/screen_shot.jpg)

(Partially) supported

- frac
- sqrt
- Sum, Prod
- mathcal

# Include kotlitex to other project

1. Make kotlitex-kotlitex of release flavor to create kotlitex-release.aar and place to your project as kotlitex.aar
2. Import kotlitex.aar as official document says. [https://developer.android.com/studio/projects/android-library](https://developer.android.com/studio/projects/android-library)
3. Add build.gralde as following (We use kotlin version 1.3.20)

```
    // kotlitex
    implementation project(":kotlitex")
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1'
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1"
```

Then use `io.github.karino2.kotlitex.view.MarkdownView` and call setMarkdown(text) to render math expression.

# License

kotlitex itself is under [MIT license](https://raw.githubusercontent.com/karino2/kotlitex/master/LICENSE).
Also, our library contains other libraries.

- This library contain [KaTeX font](https://github.com/KaTeX/katex-fonts), which is under MIT license.
- Some data table and comment is from [KaTeX](https://github.com/KaTeX/KaTeX/), which is under MIT license.
- Rendering part, code is from scratch, but logic is very similar to [Canvas-Latex](https://github.com/CurriculumAssociates/canvas-latex), which is under MIT license.

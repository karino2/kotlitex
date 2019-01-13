現状(2019/01/03)のメモ

### 作ろうとしているもの

katexのkotlin移植。
最終的にはDynamicDrawableSpanにしたいと思っている（ただ、最初はCustomViewかも）。

[github: KaTeX](https://github.com/KaTeX/KaTeX)

### ベースのバージョン

v.0.10.0

### 基本的な考え

1. 最初のターゲットはx^2をレンダリングする、という事
2. buildHTMLまではほとんど同じ処理にして、HTMLのVirtualDOMをレンダリングする
   - 複数のハードコードされたCSSクラスを持つ。これはenumのSetでどうだろう？canvas-latexのコードを見るとそれくらいで良さそう。
   - ただし[3年前のPR](https://github.com/KaTeX/KaTeX/pull/251)で、buildHTML下も変更が要る、みたいな事は言っているが、理解してない
3. レンダリングはcanvasに書くというライブラリとほぼ同じコードで良さそう。 [canvas-latex](https://github.com/CurriculumAssociates/canvas-latex)

最終的にはkatexのフォントを含めるつもりだが、とりあえずはPaintで取れる値だけでやっていけそうならやっていってもいいかも（そこまで行ったら考えるつもり）

### 現状

1. parseTreeに相当するものが制限つきで動いた
2. buildExpressionに関して生成されるデータを見たりしていた(作業はまだ)

### 進め方について

- canvas-latexの真似をしたレンダリング側を作る人と、buildHTMLに相当する所を作る人、という感じで分担したい
- 可能なら、最初にx^2の結果を表すクラスだけは決めてハードコードでこのデータを作りたい
- buildHTML側の進め方
    1. データ型を決める
    2. buildExpressionをx^2の結果がnodeと同じになるように実装
       - 途中でfontmetricsが必要になると思うので、そこまで行ったらさしあたって必要な物を理解して、レンダリング側の人がAndroidでのコードを書く
    3. buildHTML相当の事を実装

ここまでの印象だと、buildExpressionとその中のbuildGroupが一番大変そう。


## メモ

node版をつついたメモ
dist下でやる。

### parseTreeの結果（実装済み）

```
> const katex = require('./katex.js')
> katex.__parse('x^2')
[ { type: 'supsub',
    mode: 'math',
    base: { type: 'mathord', mode: 'math', loc: [Object], text: 'x' },
    sup: { type: 'textord', mode: 'math', loc: [Object], text: '2' },
    sub: undefined } ]

```

### buildExpressionを適当に公開して呼んでみた結果

```
> const katex = require('./katex.js')
> var resx = katex.expressionTest('x')
undefined
> resx
[ SymbolNode {
    text: 'x',
    height: 0.43056,
    depth: 0,
    italic: 0,
    skew: 0.02778,
    width: 0.57153,
    maxFontSize: 1,
    classes: [ 'mord', 'mathdefault' ],
    style: {} } ]
> katex.renderToString('x')
<span class="katex">
  <span class="katex-mathml"><math><semantics><mrow><mi>x</mi></mrow><annotation encoding="application/x-tex">x</annotation></semantics></math></span>
  <span class="katex-html" aria-hidden="true">
    <span class="base">
      <span class="strut" style="height:0.43056em;vertical-align:0em;"></span>
      <span class="mord mathdefault">x</span>
    </span>
   </span>
</span>

> katex.expressionTest('x^2')
> res[0]
Span {
  children:
   [ SymbolNode {
       text: 'x',
       height: 0.43056,
       depth: 0,
       italic: 0,
       skew: 0.02778,
       width: 0.57153,
       maxFontSize: 1,
       classes: [Array],
       style: {} },
     Span {
       children: [Array],
       attributes: {},
       classes: [Array],
       height: 0.8141079999999999,
       depth: 0,
       width: undefined,
       maxFontSize: 0.7,
       style: {} } ],
  attributes: {},
  classes: [ 'mord' ],
  height: 0.8141079999999999,
  depth: 0,
  width: undefined,
  maxFontSize: 1,
  style: {} }

  > res[0].children
[ SymbolNode {
    text: 'x',
    height: 0.43056,
    depth: 0,
    italic: 0,
    skew: 0.02778,
    width: 0.57153,
    maxFontSize: 1,
    classes: [ 'mord', 'mathdefault' ],
    style: {} },
  Span {
    children: [ [Object] ],
    attributes: {},
    classes: [ 'msupsub' ],
    height: 0.8141079999999999,
    depth: 0,
    width: undefined,
    maxFontSize: 0.7,
    style: {} } ]

    > res[0].children[1].children
[ Span {
    children: [ [Object] ],
    attributes: {},
    classes: [ 'vlist-t' ],
    height: 0.8141079999999999,
    depth: -0.363,
    width: undefined,
    maxFontSize: 0.7,
    style: {} } ]

    > res[0].children[1].children[0].children
[ Span {
    children: [ [Object] ],
    attributes: {},
    classes: [ 'vlist-r' ],
    height: 0.45110799999999995,
    depth: 0,
    width: undefined,
    maxFontSize: 0.7,
    style: {} } ]
> res[0].children[1].children[0].children[0].children
[ Span {
    children: [ [Object] ],
    attributes: {},
    classes: [ 'vlist' ],
    height: 0.45110799999999995,
    depth: 0,
    width: undefined,
    maxFontSize: 0.7,
    style: { height: '0.8141079999999999em' } } ]
> res[0].children[1].children[0].children[0].children[0].children
[ Span {
    children: [ [Object], [Object] ],
    attributes: {},
    classes: [],
    height: 0.45110799999999995,
    depth: 0,
    width: undefined,
    maxFontSize: 0.7,
    style: { top: '-3.063em', marginRight: '0.05em' } } ]
> res[0].children[1].children[0].children[0].children[0].children[0].children
[ Span {
    children: [],
    attributes: {},
    classes: [ 'pstrut' ],
    height: 0,
    depth: 0,
    width: undefined,
    maxFontSize: 0,
    style: { height: '2.7em' } },
  Span {
    children: [ [Object] ],
    attributes: {},
    classes: [ 'sizing', 'reset-size6', 'size3', 'mtight' ],
    height: 0.45110799999999995,
    depth: 0,
    width: undefined,
    maxFontSize: 0.7,
    style: {} } ]
> res[0].children[1].children[0].children[0].children[0].children[0].children[1].children
[ SymbolNode {
    text: '2',
    height: 0.64444,
    depth: 0,
    italic: 0,
    skew: 0,
    width: 0.5,
    maxFontSize: 0.7,
    classes: [ 'mord', '', '', 'mtight' ],
    style: {} } ]
```


### x^yをrenderToStringしてみた

```
> const katex = require('./dist/katex.js')
> var html = katex.renderToString("x^y");
> console.log(html);


<span class="katex">
    <span class="katex-mathml">
      <math>
         <semantics><mrow><msup><mi>x</mi><mi>y</mi></msup></mrow>
              <annotation encoding="application/x-tex">x^y</annotation>
         </semantics>
      </math>
    </span>
    <span class="katex-html" aria-hidden="true">
        <span class="base">
            <span class="strut" style="height:0.664392em;vertical-align:0em;"></span>
            <span class="mord">
                <span class="mord mathdefault">x</span>
                <span class="msupsub">
                    <span class="vlist-t"><span class="vlist-r">
                        <span class="vlist" style="height:0.664392em;">
                            <span style="top:-3.063em;margin-right:0.05em;">
                                <span class="pstrut" style="height:2.7em;"></span>
                                <span class="sizing reset-size6 size3 mtight">
                                    <span class="mord mathdefault mtight" style="margin-right:0.03588em;">y</span>
                                </span>
                            </span>
                        </span>
                    </span></span>
                </span>
            </span>
        </span>
    </span>
</span>
```



現状(2019/01/03)のメモ

### 作ろうとしているもの

katexのkotlin移植。
最終的にはDynamicDrawableSpanにしたいと思っている（ただ、最初はCustomViewかも）。

### ベースのバージョン

v.0.10.0

### 基本的な考え

1. 最初のターゲットはx^2をレンダリングする、という事
2. buildHTMLまではほとんど同じ処理にして、HTMLのVirtualDOMをレンダリングする
   - ただし[3年前のPR](https://github.com/KaTeX/KaTeX/pull/251)で、buildHTML下も変更が要る、みたいな事は言っているが、理解してない
   - 複数のハードコードされたCSSクラスを持つ。これはenumのSetでどうだろう？
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



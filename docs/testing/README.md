# snapshot 测试框架使用手册

分层选择(从快到稳,从局部到全局):
1. **布局/数值断言**(无栅格化):`rootLayout { … }` 得到 `LayoutNode`,对根与子做
   `assertSize` / `assertGlobalRect` / `assertApproxEq`;按类型找节点 `findType<RenderX> { … }`。
2. **像素采样断言**(局部像素,无需基准):`snapshotPixels { … }` → `expectColorAt` /
   `regionStats` / `expectRegionOpaque/Transparent/Uniform`。
3. **golden 基准比对**(整图回归,只用于确定性内容):`golden(id) { … }` 或
   `goldenPixels(id, w, h) { canvas -> … }`。

禁止进 golden 的内容:OS 字体渲染的文本、外部/网络图片、随机/时变输出。
文本场景请用"数值区间断言 + artifact PNG 人眼检视"(参考 `TextMetricsTest`);文本 artifact 落在
`core/build/test-results/test-image-outputs/…`(与 golden 失配产物 `core/build/test-results/golden/<id>/` 不同目录)。
文本只做区间/单调断言,不锁字形与像素绝对值(勿按单机收紧)。

golden 三态:
```bash
./gradlew :core:test -PsnapshotTest.mode=record --tests '*SomeTest'   # 生成新基准(已存在则报错)
./gradlew :core:test --tests '*SomeTest'                              # 默认 verify
./gradlew :core:test -PsnapshotTest.mode=update --tests '*SomeTest'   # 覆盖基准(先人工审阅 actual)
```
失配产物在 `core/build/test-results/golden/<id>/actual.png`、`diff.png`。
最小可用示例:`GradientGoldenTest.kt`(paint/gradient,`goldenPixels`)、`ClipGoldenTest.kt`(widget,`golden`)。
容差用法:`assertImageMatchesBaseline(image, id, perPixelTolerance, allowMismatchRatio)` —— 需要对抗锯齿确定性几何放宽容差时使用。
命名/位置约定:测试放 `core/src/test/kotlin/com/muedsa/snapshot/<area>/…Test.kt`,golden 基准图放 `core/src/test/resources/golden/<id>.png`。

新功能测试的推荐做法:按上面 1→2→3 顺序,能数值断言的先数值断言;确定性可视内容再叠 golden。

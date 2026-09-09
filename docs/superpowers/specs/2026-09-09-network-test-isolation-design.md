# 网络测试隔离设计(`network` 标签 + 默认排除)

日期:2026-09-09
状态:设计(做法已与用户逐节确认)

## 背景

仓库的默认 `./gradlew test` 目前会真实访问外网:多个测试依赖 `samples-files.com` / `zhimg.com` / `i0.hdslb.com` 下载图片。这带来两个问题——CI 结果受外网可用性影响;本地断网时默认套件失败。

仓库已有**同类先例**:`@Tag("sample")` 标注的样例再生成测试(`Sample`/`LogoCreator`/`ParserSample`)默认排除,由 `-PincludeSamples` 显式触发(`README.md:156` 有文档,`core`/`parser` 的 `tasks.test` 各自实现)。本批把网络测试按同一模式隔离。

**联网测试实测清单**(含间接路径,已逐一核实):

| 模块 | 文件 | 联网方式 | 是否整类联网 |
|---|---|---|---|
| core | `tools/SimpleLimitedNetworkImageCacheTest` | `cache.getImage(url)` | 是(5/5 用例) |
| core | `tools/SimpleNoLimitedNetworkImageCacheTest` | `getImage(url)` | 是(5/5 用例) |
| core | `tools/LimitedImageInputStreamTest` | `URL(url).openStream()` | **否**(仅 `image_format_test`;`header_buffer_read_test` 纯本地) |
| core | `widget/CachedNetworkImageTest` | `CachedNetworkImage` | 是(2/2 用例) |
| core | `widget/text/TextTest` | `ImageEmojiSpan(url)` | **否**(仅 `image_emoji_test`;其余 4 个用例本地) |
| parser | `parser/widget/ImageParserTest` | 经 `SnapshotElement.getNetworkImageCache()` 间接下载,并断言 480×360 | 是 |
| parser | `parser/widget/TextParserTest` | `TextParser` 的 `provider` → 缓存下载 | **否**(仅 `rich_text_test`) |

`tools/LimitedInputStreamTest` 纯本地,不需要标签。

## 目标与完成标准

- [ ] `network` 标签建立:默认 `./gradlew test` **不再执行任何联网用例**;`-PincludeNetwork` 可单独运行它们;
- [ ] `-PincludeSamples` 既有行为不变;
- [ ] 本批触及的测试文件不再使用 `org.junit.jupiter.api.assertThrows` / `org.junit.jupiter.api.Test`(标签注解 `@Tag` 仍来自 org.junit,不可避免);
- [ ] `CachedNetworkImageTest` 从零断言冒烟改为最小断言(布局尺寸 + 像素非空);
- [ ] 默认套件全绿;`git status` 干净。

## 已确认做法决策

1. **开关形态**:与 sample 对称——默认 `excludeTags("sample", "network")`;`-PincludeNetwork` → `includeTags("network")`;两个属性同时给出则跑并集。
2. **标签粒度**:**混合类用方法级**(只给联网的 `@Test` 打标签),整类联网的用类级。这样同类的本地用例继续留在默认套件,覆盖不丢。
3. **`CachedNetworkImageTest`**:打标签 + 补最小断言(去 `println`/`drawWidget`,改用 testkit)。
4. **org.junit 转换范围**:只转本批触及的文件;`parser` 的 8 个非网络文件(`attr/*`、`ParserTest`、`TokenizerTest` 等)留给单独的"legacy 统一 kotlin.test"批次。
5. **构建配置组织**:沿用现状,`core` 与 `parser` 各自就地复制(不引入 buildSrc 约定插件)。

## 构建配置改动

`core/build.gradle.kts` 与 `parser/build.gradle.kts` 的 `tasks.test` 各改一处(其余不变):

```kotlin
tasks.test {
    useJUnitPlatform()
    // 默认排除需外部环境/人工触发的用例;显式 -PincludeSamples / -PincludeNetwork 时只跑对应标签
    val onlyTags = buildList {
        if (providers.gradleProperty("includeSamples").isPresent) add("sample")
        if (providers.gradleProperty("includeNetwork").isPresent) add("network")
    }
    if (onlyTags.isEmpty()) {
        useJUnitPlatform { excludeTags("sample", "network") }
    } else {
        useJUnitPlatform { includeTags(*onlyTags.toTypedArray()) }
    }
    systemProperty("snapshotTest.mode", providers.gradleProperty("snapshotTest.mode").getOrElse("verify"))
    providers.gradleProperty("snapshotTest.goldenRoot").orNull?.let {
        systemProperty("snapshotTest.goldenRoot", it)
    }
}
```

文档同步:`README.md:156` 附近补 `-PincludeNetwork`;`docs/testing/README.md` 补标签约定(默认排除 `sample`/`network`,各自开关)。

## 逐文件改动

| 文件 | 标签 | org.junit 转换 |
|---|---|---|
| `core/tools/SimpleLimitedNetworkImageCacheTest` | 类级 `@Tag("network")` | `assertThrows` → `assertFailsWith` |
| `core/tools/SimpleNoLimitedNetworkImageCacheTest` | 类级 | `assertThrows` → `assertFailsWith` |
| `core/tools/LimitedImageInputStreamTest` | 方法级:仅 `image_format_test` | `assertThrows` → `assertFailsWith` |
| `core/tools/LimitedInputStreamTest` | 无(纯本地) | `assertThrows` → `assertFailsWith` |
| `core/widget/CachedNetworkImageTest` | 类级 | `org.junit.jupiter.api.Test` → `kotlin.test.Test` |
| `core/widget/text/TextTest` | 方法级:仅 `image_emoji_test` | 无 |
| `parser/.../widget/ImageParserTest` | 类级 | 无 |
| `parser/.../widget/TextParserTest` | 方法级:仅 `rich_text_test` | 无 |

### `CachedNetworkImageTest` 的断言(冒烟迁移)

两个用例(普通 / `noCache = true`)结构相同:两个 `Column` 各含两张 400×400 的 `CachedNetworkImage`,并排成 `Row`。

- 布局:`rootLayout { … }.assertSize(800f, 800f)`;
- 像素:`snapshotPixels(background = Color.TRANSPARENT) { … }` 后 `expectRegionOpaque(pixmap, Rect.makeXYWH(0f, 0f, 800f, 800f))`——图片未加载则区域透明,断言失败。

## 验证标准

- 默认 `./gradlew test --offline` 全绿,且**网络用例不出现在执行清单**:核对 `build/test-results/test/*.xml`——`Simple*NetworkImageCacheTest`、`CachedNetworkImageTest` 无结果文件;`TextTest` 只 4 个用例、`TextParserTest` 少 1 个、`LimitedImageInputStreamTest` 只 1 个;
- `./gradlew :core:test -PincludeNetwork --offline` 只跑网络用例;`-PincludeSamples` 行为不变;
- `git status` 干净;
- grep 核对:本批 8 个文件不再出现 `org.junit.jupiter.api.assertThrows` / `org.junit.jupiter.api.Test`。

## 风险与处理

1. **`-PincludeNetwork` 实跑可能因外网不可达而失败**(本机到 `samples-files.com` / `zhimg.com` 不保证)。**如实记录,不把失败说成绿**;标签正确性由"默认套件不含这些用例"这一可离线验证的事实来保证。
2. **`CachedNetworkImageTest` 新断言依赖图片真实加载**,只在网络开关下执行,默认套件不受影响。
3. 方法级标签依赖 JUnit 5,已由 `useJUnitPlatform()` 保证。

## 非目标

- `parser` 8 个非网络文件的 `org.junit` 统一(单独批次);
- 把联网测试改造成离线(需要本地图片服务/打桩,本批不做);
- 其它旧冒烟迁移(字体类、`render/flex/RenderFlexTest`、`paint/gradient/*`)。

## 交付方式

分支 `test/network-tag-isolation`,base = `main`(`66d74b7`)。提交顺序:docs 先行(本 spec → 实现计划),再按"构建配置 / 逐文件标签"分组提交。PR 文案备好。

## 参考

- 先例:`@Tag("sample")` 与 `-PincludeSamples`(`README.md:156`、`core/build.gradle.kts`、`parser/build.gradle.kts`);
- 手册:`docs/testing/README.md`;
- 前序批次:`docs/superpowers/specs/2026-09-08-migrate-widget-smoke-1a-design.md`、`…-1b-design.md`、`2026-09-09-migrate-widget-smoke-1c-design.md`。

# 测试/CI 收尾卫生设计(样例再生成器隔离 + 干净守卫)

日期:2026-09-08
状态:设计(做法已与用户确认)

## 背景

- `core/src/test/.../Sample.kt` 与 `parser/src/test/.../ParserSample.kt` 是 **README 配图再生成器**:每次默认 `./gradlew test` 会把 `sample_container/sample_layout/sample_image_and_text/sample_parse_dom_like` 等 PNG 写到**仓库根**。README 以相对/raw 链接引用这些已跟踪图片。
- 副作用:① 默认测试改写已跟踪文件 → 工作树被写脏(字节随字体/网络变化);② `sample_image_and_text` 与 `ParserSample` 依赖外网图片 → 默认 suite 联网、易抖、离线必挂。
- CI(test.yaml/jar.yaml)已跑 `./gradlew test`/`jar`;golden 默认即 verify,无需额外。CI 缺"默认 suite 是否改写已跟踪文件"的守卫。

## 目标与完成标准

1. 把两个样例再生成器**移出默认 suite**(不随默认 `test` 跑,不再默认联网/写脏仓库根)。
2. 提供**显式再生成**入口(`-PincludeSamples`),README 配图仍可按需重生成并提交。
3. CI 加**工作树干净守卫**:默认 test 若改写任何已跟踪文件即失败。
4. 不改写仓库根已提交样例 PNG 的内容(保持 README 引用有效)。

验收:
- [ ] 默认 `./gradlew test`(core+parser)后 `git status` 干净、不再命中外网样例图;
- [ ] `./gradlew :core:test -PincludeSamples` 与 `./gradlew :parser:test -PincludeSamples` 能分别再生成两模块样例 PNG;
- [ ] `test.yaml` 增干净守卫步骤;
- [ ] README 增一句再生成说明;
- [ ] `./gradlew test` 与 `./gradlew jar` 仍 BUILD SUCCESSFUL。

## 做法决策(逐条)

1. 用 JUnit `@Tag("sample")` 标注两个 Sample 类(`org.junit.jupiter.api.Tag`);core 与 parser 的 `tasks.test` 默认 `excludeTags("sample")`,当 Gradle 属性 `-PincludeSamples` 存在时改为 `includeTags("sample")`。
2. 样例文件仍写仓库根(README 位置需要),仅把"何时跑"改为显式。
3. test.yaml 在 `./gradlew test` 之后加 `git diff --exit-code` 守卫步骤(默认 suite 写脏即红,输出 `git status` 便于定位)。
4. README 在样例图附近补一句:这两类是样例再生成器、已从默认 suite 排除,并给出手动再生成命令。

## 涉及文件

- Modify:`core/src/test/kotlin/com/muedsa/snapshot/Sample.kt`
- Modify:`parser/src/test/kotlin/com/muedsa/snapshot/ParserSample.kt`
- Modify:`core/build.gradle.kts`、`parser/build.gradle.kts`(tasks.test 标签过滤)
- Modify:`.github/workflows/test.yaml`(干净守卫步骤)
- Modify:`README.md`(再生成说明一句)

## 非目标

- 不删除/迁移这两个再生成器;不改其输出路径(仍写仓库根,供 README)。
- 不动其它依赖网络的 widget 测试(网络隔离属后续工程卫生批次)。
- 不加覆盖率工具;不改 jar.yaml。
- 不迁移旧冒烟渲染测试。

## 参考

- 现 CI:`.github/workflows/{test,jar}.yaml`(ubuntu/JDK17,已跑 test/jar)。
- 现样例:Sample.kt、ParserSample.kt(均用 `rootDirection.resolve(...).writeBytes(...)`)。

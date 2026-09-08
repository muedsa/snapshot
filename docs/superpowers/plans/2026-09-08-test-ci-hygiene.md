# 测试/CI 收尾卫生实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans.

**Goal:** 把两个"README 样例再生成器"测试移出默认 suite(打 `@Tag("sample")` + 默认排除、`-PincludeSamples` 可选),并在 CI 加工作树干净守卫。

**Architecture:** JUnit Tag 过滤在 core/parser 各自 `tasks.test` 配 `excludeTags`/`includeTags`(由 Gradle 属性 `includeSamples` 决定);样例仍写仓库根,仅"何时跑"变显式;test.yaml 加 `git diff --exit-code` 守卫;README 补一句命令。

**Tech Stack:** Kotlin/JVM、Gradle、JUnit Platform(经 `kotlin-test-junit5` + `junit-jupiter-engine`)、GitHub Actions。

**Spec:** `docs/superpowers/specs/2026-09-08-test-ci-hygiene-design.md`

---

## 执行前必读(环境约定)

- 仓库根 `D:\mine\workspace\snapshot`(git-bash 用 `./gradlew`);分支 `test/ci-sample-hygiene`(base=main),工作区干净。
- 每个完成点 GPG 提交(`git commit -S -m "…"`;报 `No passphrase given` 原样重试一次;`git log -1 --pretty='%h %G? %s'` 应 G)。
- gradle 缓存报 AccessDenied → `--no-build-cache`。

## 涉及文件

- Modify:`core/src/test/kotlin/com/muedsa/snapshot/Sample.kt`
- Modify:`parser/src/test/kotlin/com/muedsa/snapshot/ParserSample.kt`
- Modify:`core/build.gradle.kts`、`parser/build.gradle.kts`
- Modify:`.github/workflows/test.yaml`
- Modify:`README.md`

---

### Task 1:样例类打 Tag 并默认排除

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/Sample.kt`
- Modify: `parser/src/test/kotlin/com/muedsa/snapshot/ParserSample.kt`
- Modify: `core/build.gradle.kts`
- Modify: `parser/build.gradle.kts`

- [ ] **Step 1: 两个 Sample 类加 `@Tag("sample")`**

`Sample.kt`:顶部加 `import org.junit.jupiter.api.Tag`,类上（`class Sample {` 前）加 `@Tag("sample")`。
`ParserSample.kt`：同样加 import 与 `@Tag("sample")`（`class ParserSample {` 前）。

- [ ] **Step 2: core 与 parser 的 tasks.test 加标签过滤**

`core/build.gradle.kts` 与 `parser/build.gradle.kts` 各自的 `tasks.test { useJUnitPlatform() ... }` 块内、`useJUnitPlatform()` 后追加：

```kotlin
    val includeSamples = providers.gradleProperty("includeSamples").orNull
    if (includeSamples != null) {
        useJUnitPlatform { includeTags("sample") }
    } else {
        useJUnitPlatform { excludeTags("sample") }
    }
```
> 注意:若块内已有 `useJUnitPlatform()`(无参),保留之,再按条件补 `useJUnitPlatform { …tags… }`;Gradle 支持多次 useJUnitPlatform 合并标签配置。以实际语法校验为准做最小调整。

- [ ] **Step 3: 验证默认排除**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.Sample' --console=plain
./gradlew :parser:test --tests 'com.muedsa.snapshot.ParserSample' --console=plain
```
Expected:因默认 excludeTags 无匹配——若 Gradle 报 "No tests found for given includes" 属预期(说明被排除);更稳的验证走 Step 5 的 include。

- [ ] **Step 4: 验证可选再生成**

Run:
```bash
./gradlew :core:test -PincludeSamples --console=plain
./gradlew :parser:test -PincludeSamples --console=plain
```
Expected:BUILD SUCCESSFUL,且 Sample/ParserSample 用例执行、仓库根样例 PNG 被重写。完成后把这些 PNG 的改动**还原**(`git checkout -- sample_*.png`),因为本任务不提交样例内容变更。

- [ ] **Step 5: 提交(只含 4 文件)**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/Sample.kt \
        parser/src/test/kotlin/com/muedsa/snapshot/ParserSample.kt \
        core/build.gradle.kts parser/build.gradle.kts
git commit -S -m "test: 样例再生成器打 @Tag(sample) 并默认排除(-PincludeSamples 可选)"
```
确认 G。

---

### Task 2:CI 干净守卫 + README 说明

**Files:**
- Modify: `.github/workflows/test.yaml`
- Modify: `README.md`

- [ ] **Step 1: test.yaml 加守卫步骤**

在 `- name: Test with Gradle`(`./gradlew test`)之后、Upload artifact 之前插入：

```yaml
      - name: Check working tree is clean after test
        run: |
          if ! git diff --exit-code --quiet; then
            echo "::error::Default 'test' modified tracked files (sample PNG regenerated?)."
            git status --porcelain
            exit 1
          fi
```

- [ ] **Step 2: README 补再生成说明**

在 README 引用样例图的位置（`sample_image_and_text.png` 与 `sample_parse_dom_like.png` 附近，任选其一处）补一句：

```markdown
> 上述样例图由 `Sample` / `ParserSample` 测试生成。它们是样例再生成器，已加 `@Tag("sample")` 并从默认测试排除（避免默认 `./gradlew test` 联网下载图片或改写仓库文件）。手动再生成：`./gradlew :core:test -PincludeSamples` 与 `./gradlew :parser:test -PincludeSamples`。
```

- [ ] **Step 3: 提交**

```bash
git add .github/workflows/test.yaml README.md
git commit -S -m "ci: test 后加工作树干净守卫; README 补样例再生成说明"
```
确认 G。

---

### Task 3:全量验证

**Files:** 无新改动(校验)。

- [ ] **Step 1: 全量 test + jar**

Run:
```bash
./gradlew test --no-build-cache --console=plain
./gradlew jar --no-build-cache --console=plain
```
Expected:BUILD SUCCESSFUL;随后 `git status --short` 为空(默认 suite 不再写脏样例 PNG)。
> 若 core 全量仍偶发 skiko 原生崩溃(GradientGoldenTest)或网络类(SocketException)属既有留意项,重跑通过即可记录、不归因本批。

- [ ] **Step 2: 汇报**

汇总:两提交 hash/签名、默认 test 后工作树干净验证、`-PincludeSamples` 再生成验证(含已还原样例 PNG)、test/jar 结果。

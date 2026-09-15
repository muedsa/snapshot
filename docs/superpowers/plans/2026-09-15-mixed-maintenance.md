# 混合杂项维护实施计划

> 分支：`chore/misc-maintenance`

## 范围

本次集中完成以下五项低耦合维护工作：

1. 移除 `Matrix44CMO.transform(x, y, z)` 未实现且无调用方的公开桩。
2. 完善 `CharacterReader` 构造参数的前置校验。
3. 将测试中的 Kotlin/JVM 原生 `assert` 改为 `kotlin.test` 断言。
4. 将选定公共 API 的英文 KDoc 与紧邻源码说明翻译为中文。
5. 为 GitHub Actions 增加 Windows 全量测试冒烟任务。

明确不修改 `paintImage` 的 `centerSlice + scale`、`Offset.compareTo`，不开展原生内存观测，也不扩展网络安全或 Kotlin 多平台迁移范围。

## 实施步骤

### 任务一：移除无效矩阵变换桩

**文件：** `core/src/main/kotlin/com/muedsa/geometry/Matrix44CMO.kt`

1. 通过调用搜索与提交历史确认方法没有使用者，且没有可恢复的既定语义。
2. 删除返回 `Unit`、只会抛出 `NotImplementedError` 的 `transform(x, y, z)`。
3. 运行 `:core:test`，确认源码和现有 API 使用者均可编译。

### 任务二：完善解析器构造参数校验

**文件：**

- `parser/src/main/kotlin/com/muedsa/snapshot/parser/CharacterReader.kt`
- `parser/src/test/kotlin/com/muedsa/snapshot/parser/CharacterReaderTest.kt`（新建）

1. 先增加测试，断言零值、负数缓冲区和不支持 `mark/reset` 的 `Reader` 均抛出 `IllegalArgumentException`。
2. 单独运行新测试，确认测试在现有实现下失败。
3. 在分配字符数组之前校验 `bufferSize > 0`，并将 `markSupported()` 的原生 `assert` 改为始终生效的 `require`。
4. 再次运行新测试及 `:parser:test`，确认通过。

### 任务三：清理测试中的原生断言

**文件：**

- `core/src/test/kotlin/com/muedsa/snapshot/render/ContainerParserLayerTest.kt`
- `core/src/test/kotlin/com/muedsa/snapshot/render/PictureLayerTest.kt`

1. 使用 `assertContentEquals` 表达字节数组相等关系。
2. 保留已有 `assertFalse` 对不相等关系的验证。
3. 搜索测试源码，确认不再残留 Kotlin/JVM 原生 `assert(...)`。
4. 运行两个测试类及 `:core:test`。

### 任务四：中文化公共说明

**重点文件：**

- `core/src/main/kotlin/com/muedsa/geometry/Matrix44CMO.kt`
- `core/src/main/kotlin/com/muedsa/snapshot/rendering/stack/StackParentData.kt`
- `core/src/main/kotlin/com/muedsa/snapshot/paint/text/ParagraphBuilder.kt`
- `core/src/main/kotlin/com/muedsa/snapshot/tools/ImageFormatValidator.kt`
- `parser/src/main/kotlin/com/muedsa/snapshot/parser/CharacterReader.kt`
- `parser/src/main/kotlin/com/muedsa/snapshot/parser/token/Tokenizer.kt`
- `parser/src/main/kotlin/com/muedsa/snapshot/parser/token/TokenizerState.kt`

1. 翻译公共类、属性和方法的英文 KDoc，以及与这些 API 紧邻、对维护有价值的英文说明。
2. 保留类型名、协议关键字、规范链接和代码标识符原文，避免改变程序行为。
3. 复查上述文件中的英文说明残留；只保留无法或不应翻译的专有名词。
4. 运行 `:core:compileKotlin` 与 `:parser:compileKotlin`。

### 任务五：增加 Windows CI 冒烟测试

**文件：** `.github/workflows/test.yaml`

1. 将测试作业改为 Ubuntu 与 Windows 的系统矩阵。
2. 仅在非 Windows 环境授予 `gradlew` 执行权限。
3. 分别使用 `./gradlew test` 和 `.\gradlew.bat test`。
4. 将测试后工作区检查改为两端都可运行的 `git diff --exit-code`。
5. 为不同系统生成独立的测试结果制品名，避免矩阵任务同名冲突。

## 最终验证

1. 运行 `./gradlew.bat test --rerun-tasks --no-build-cache`。
2. 运行源码搜索，确认无目标原生断言、无 `TODO("transform")`。
3. 检查 `git diff --check`、`git diff --stat` 与 `git status --short`。
4. 对完整差异进行一次代码审查，修复确认的问题后再次运行相关验证。

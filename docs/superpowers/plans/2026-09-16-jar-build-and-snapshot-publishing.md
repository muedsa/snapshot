# JAR 构建与 GitHub 快照发布实现计划

**目标：** 精简 JAR 构建产物，并在 `main` 分支构建、测试成功后将 core/parser 的 `0.0.0-SNAPSHOT` 发布到 GitHub Packages Maven 仓库。

**架构：** 根构建脚本统一模块坐标、归档命名、Manifest 与 Maven Publication；只发布 `snapshot-core` 和 `snapshot-parser` 两个薄 JAR 及其源码包，testkit 继续只服务仓库内部测试。GitHub Actions 对 PR 执行测试与产物构建，对 `main` 推送在测试成功后使用自动提供的 `GITHUB_TOKEN` 发布时间戳快照。

**技术栈：** Gradle 9.7.1 Kotlin DSL、Kotlin JVM 2.4.20、`maven-publish`、GitHub Actions、GitHub Packages Maven 仓库。

## 全局约束

- 发布版本固定为 `0.0.0-SNAPSHOT`。
- Maven 坐标固定为 `com.muedsa.snapshot:snapshot-core:0.0.0-SNAPSHOT` 与 `com.muedsa.snapshot:snapshot-parser:0.0.0-SNAPSHOT`。
- 保持薄 JAR：不得把 Skiko、平台原生库或其他依赖打入项目 JAR。
- `:testkit` 不发布、不上传为 CI 的交付产物。
- PR 不得发布包；仅 `refs/heads/main` 的推送可执行发布。
- 发布必须位于完整 `test` 成功之后，使用 GitHub Actions 自动提供的 `GITHUB_TOKEN`，不得新增长期发布密钥。
- Java/Kotlin 目标版本继续保持 Java 11，不因 CI 使用 JDK 17 而提高字节码版本。
- 文档尽可能使用中文。

---

### 任务 1：统一 JAR 构建约定并移除无效根 JAR

**文件：**
- 修改：`build.gradle.kts`
- 修改：`core/build.gradle.kts`
- 修改：`parser/build.gradle.kts`
- 修改：`testkit/build.gradle.kts`

**接口：**
- 输入：Kotlin JVM 子项目现有 `jar`、`kotlinSourcesJar` 任务。
- 输出：根任务 `releaseJars`；产物 `snapshot-core-0.0.0-SNAPSHOT.jar`、`snapshot-core-0.0.0-SNAPSHOT-sources.jar`、`snapshot-parser-0.0.0-SNAPSHOT.jar`、`snapshot-parser-0.0.0-SNAPSHOT-sources.jar`。

- [x] **步骤 1：验证当前问题**

执行：

```powershell
.\gradlew.bat jar --rerun-tasks --no-build-cache
Get-ChildItem build/libs,core/build/libs,parser/build/libs,testkit/build/libs -Filter *.jar
```

预期：根目录生成约 261 字节的空 `snapshot.jar`，同时生成不应交付的 `snapshot-testkit-0.0.0-SNAPSHOT.jar`。

- [x] **步骤 2：在根构建脚本集中归档约定**

在 `build.gradle.kts` 顶部引入 `BasePluginExtension` 与 `Jar`，移除根项目的 `` `java-library` `` 插件，并在 `subprojects` 中统一：

```kotlin
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar

plugins {
    base
    alias(libs.plugins.jvm) apply false
}

subprojects {
    group = "com.muedsa.snapshot"
    version = "0.0.0-SNAPSHOT"

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(11))
        }
        extensions.configure<BasePluginExtension> {
            archivesName.set("${rootProject.name}-${project.name}")
        }
        tasks.withType<Jar>().configureEach {
            manifest {
                attributes(
                    "Implementation-Title" to "${rootProject.name}-${project.name}",
                    "Implementation-Version" to project.version,
                )
                if (project.name == "core" || project.name == "parser") {
                    attributes("Automatic-Module-Name" to "${project.group}.${project.name}")
                }
            }
        }
    }
}

tasks.register("releaseJars") {
    group = "build"
    description = "构建可交付的 core/parser 二进制与源码 JAR"
    dependsOn(
        ":core:jar",
        ":core:kotlinSourcesJar",
        ":parser:jar",
        ":parser:kotlinSourcesJar",
    )
}
```

根项目只应用轻量 `base` 插件以保留 `clean/assemble` 生命周期，不再创建空 JAR。Java 11 toolchain 改由 `JavaPluginExtension` 显式配置，避免依赖根项目的 `java-library` 类型安全访问器。

- [x] **步骤 3：删除三个子项目的重复配置**

从 `core/build.gradle.kts`、`parser/build.gradle.kts`、`testkit/build.gradle.kts` 删除各自的：

```kotlin
group = "com.muedsa.snapshot"
version = "0.0.0-SNAPSHOT"
val jarBaseName = ...
val manifestAttributes = ...
tasks.jar { ... }
tasks.kotlinSourcesJar { ... }
```

不得改动测试标签、平台运行时依赖或 Java 11 toolchain。

- [x] **步骤 4：验证交付产物**

执行：

```powershell
.\gradlew.bat clean releaseJars --no-build-cache
Get-ChildItem core/build/libs,parser/build/libs -Filter *.jar | Select-Object Name,Length
Test-Path build/libs/snapshot.jar
Test-Path testkit/build/libs/snapshot-testkit-0.0.0-SNAPSHOT.jar
```

预期：core/parser 各有二进制与 `-sources` JAR；后两个 `Test-Path` 均为 `False`。

---

### 任务 2：配置 core/parser Maven Publication

**文件：**
- 修改：`build.gradle.kts`
- 修改：`parser/build.gradle.kts`

**接口：**
- 输入：任务 1 的统一 `group`、`version`、JAR 名称与 `kotlinSourcesJar`。
- 输出：两个名为 `githubPackages` 的 `MavenPublication`，以及 GitHub Packages 发布任务。

- [x] **步骤 1：验证发布任务尚不存在**

执行：

```powershell
.\gradlew.bat tasks --all --console=plain | Select-String "generatePomFileForGithubPackagesPublication"
```

预期：无匹配结果。

- [x] **步骤 2：修正 parser 的公开依赖范围**

`parser` 的公开签名直接暴露 core 的 `Widget` 等类型，因此将：

```kotlin
implementation(project(":core"))
```

改为：

```kotlin
api(project(":core"))
```

这样生成的 POM 会把 `snapshot-core` 放入消费者的编译依赖。

- [x] **步骤 3：在根构建脚本集中创建 Publication**

在 `build.gradle.kts` 引入 `PublishingExtension`、`MavenPublication`，对 `:core` 与 `:parser` 应用并配置 `maven-publish`：

```kotlin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

configure(listOf(project(":core"), project(":parser"))) {
    pluginManager.apply("maven-publish")
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        val moduleName = project.name
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("githubPackages") {
                    from(components["java"])
                    artifact(tasks.named("kotlinSourcesJar"))
                    artifactId = "${rootProject.name}-$moduleName"
                    pom {
                        name.set("Snapshot $moduleName")
                        description.set(
                            if (moduleName == "core")
                                "Kotlin/Skia structured image rendering core"
                            else
                                "DOM-like text parser for Snapshot",
                        )
                        url.set("https://github.com/muedsa/snapshot")
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        scm {
                            connection.set("scm:git:https://github.com/muedsa/snapshot.git")
                            url.set("https://github.com/muedsa/snapshot")
                        }
                    }
                }
            }
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/muedsa/snapshot")
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orNull
                        password = providers.environmentVariable("GITHUB_TOKEN").orNull
                    }
                }
            }
        }
    }
}
```

- [x] **步骤 4：生成并核对 POM**

执行：

```powershell
.\gradlew.bat :core:generatePomFileForGithubPackagesPublication :parser:generatePomFileForGithubPackagesPublication --no-build-cache
Get-Content core/build/publications/githubPackages/pom-default.xml
Get-Content parser/build/publications/githubPackages/pom-default.xml
```

预期：

- 两个 POM 的版本均为 `0.0.0-SNAPSHOT`；
- core POM 包含 `org.jetbrains.skiko:skiko-awt`；
- parser POM 包含 compile scope 的 `com.muedsa.snapshot:snapshot-core:0.0.0-SNAPSHOT`；
- POM 不包含 testkit、JUnit 或平台原生测试依赖。

---

### 任务 3：CI 测试成功后发布 GitHub 快照

**文件：**
- 修改：`.github/workflows/jar.yaml`

**接口：**
- 输入：任务 1 的 `releaseJars` 与任务 2 的 `publish` 任务。
- 输出：PR 构建校验；`main` 推送自动发布 GitHub Packages 快照。

- [x] **步骤 1：收紧工作流权限和产物范围**

在工作流级别只保留只读权限：

```yaml
permissions:
  contents: read
```

将原 `./gradlew jar` 改为两个顺序步骤：

```yaml
- name: Test with Gradle
  run: ./gradlew test --no-daemon
- name: Build release jars
  run: ./gradlew releaseJars --no-daemon
```

上传路径改为：

```yaml
path: |
  core/build/libs/snapshot-core-*.jar
  parser/build/libs/snapshot-parser-*.jar
```

- [x] **步骤 2：增加仅在 main 分支运行且依赖测试成功的发布任务**

```yaml
publish:
  if: github.event_name == 'push' && github.ref == 'refs/heads/main'
  needs: build
  runs-on: ubuntu-latest
  permissions:
    contents: read
    packages: write
  steps:
    - uses: actions/checkout@v7
    - name: set up JDK 17
      uses: actions/setup-java@v6
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    - name: Publish snapshot to GitHub Packages
      run: ./gradlew :core:publish :parser:publish --no-daemon
      env:
        GITHUB_ACTOR: ${{ github.actor }}
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

`publish` 任务通过 `needs: build` 等待测试、`releaseJars` 与产物上传全部成功；只有该任务获得 `packages: write`，PR 的构建任务始终只有只读权限。

- [x] **步骤 3：静态核对工作流门禁**

执行：

```powershell
rg -n "permissions:|packages: write|gradlew test|releaseJars|needs: build|github.event_name|refs/heads/main|GITHUB_TOKEN" .github/workflows/jar.yaml
```

预期：发布任务同时受推送事件和 main 引用限制、依赖 build 成功；只有发布任务具有 `packages: write`，凭据来自 `secrets.GITHUB_TOKEN`。

---

### 任务 4：更新使用文档并完成验证

**文件：**
- 修改：`docs/usage/README.md`

**接口：**
- 输入：任务 2 的 Maven 坐标、仓库地址与鉴权约束。
- 输出：GitHub Packages 快照使用说明和新的 JAR 构建命令。

- [x] **步骤 1：更新构建命令**

将交付 JAR 命令说明改为：

```bash
./gradlew releaseJars          # 只构建 core/parser 的二进制与源码 JAR
```

保留 `./gradlew jar` 的说明时，应明确它是所有子模块的开发构建，不是发布入口。

- [x] **步骤 2：添加 GitHub Packages 使用示例**

用中文说明 GitHub Packages Maven 下载需要 GitHub 用户名和具备 `read:packages` 的令牌，并给出：

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/muedsa/snapshot")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull
            password = providers.gradleProperty("gpr.key").orNull
        }
    }
}

dependencies {
    implementation("com.muedsa.snapshot:snapshot-core:0.0.0-SNAPSHOT")
    implementation("com.muedsa.snapshot:snapshot-parser:0.0.0-SNAPSHOT")
}
```

并说明 parser 已传递依赖 core，通常只需按功能选择一个入口。

- [x] **步骤 3：运行完整验证**

执行：

```powershell
.\gradlew.bat clean test releaseJars :core:generatePomFileForGithubPackagesPublication :parser:generatePomFileForGithubPackagesPublication --no-build-cache
git diff --check
git status --short
```

预期：`BUILD SUCCESSFUL`；`releaseJars` 的交付集合只有 core/parser 的二进制与源码 JAR（完整测试可按内部依赖临时构建 testkit JAR，但 CI 不上传它）；POM 内容符合任务 2；差异检查无错误。

- [x] **步骤 4：验证可复现构建**

连续两次执行 `releaseJars --rerun-tasks --no-build-cache` 并比较四个交付 JAR 的 SHA-256。

预期：两次哈希完全一致。

- [x] **步骤 5：提交**

```bash
git add build.gradle.kts core/build.gradle.kts parser/build.gradle.kts testkit/build.gradle.kts .github/workflows/jar.yaml docs/usage/README.md docs/superpowers/plans/2026-09-16-jar-build-and-snapshot-publishing.md
git commit -m "build: 优化 JAR 构建并发布 GitHub 快照"
```

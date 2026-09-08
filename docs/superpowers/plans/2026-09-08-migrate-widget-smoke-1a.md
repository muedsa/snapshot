# 旧渲染冒烟迁移 Batch-1a 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans.

**Goal:** 把 `widget` 包 4 个简单确定性旧冒烟测试重写替换为 testkit 断言/golden(Opacity、Positioned、Clip)并清理 Flexible,确立迁移模板。

**Architecture:** 复用 testkit `golden/snapshotPixels/rootLayout/LayoutNode`;逐文件重写为 数值/采样+golden;场景移除文字/网络、只留确定性几何;golden 基准同提交。

**Spec:** `docs/superpowers/specs/2026-09-08-migrate-widget-smoke-1a-design.md`

---

## 执行前必读(环境约定)

- 仓库根 `D:\mine\workspace\snapshot`;分支 `test/migrate-widget-smoke-1a`(base=main,testkit 已合入)。工作区干净。
- 每个完成点 **GPG 提交**(报 `No passphrase given` 原样重试;`git log -1 --pretty='%h %G? %s'` 应 G)。gradle 缓存 AccessDenied → `--no-build-cache`。
- 迁移模板:文件先读,保留场景意图;删除 `println` 与 debug 文本;用 kotlin.test;golden id 沿用;record→verify。

---

### Task M1:重写 OpacityTest

**Files:** Modify `core/src/test/kotlin/com/muedsa/snapshot/widget/OpacityTest.kt`

- [ ] 去掉 println/drawWidget;保留三档场景(0/0.5/1),各做:
  - 采样:`snapshotPixels` 中心 (150,150):opacity=1==GREEN、0==YELLOW、0.5∈(非 GREEN 非 YELLOW);四角 (1,1)==YELLOW(全档);
  - golden:`golden("widget/opacity/opacity_0")`、`…_0_5`、`…_1`。
- [ ] record 三张 → verify:`./gradlew :core:test -PsnapshotTest.mode=record --tests '...OpacityTest'` 后默认 verify。
- [ ] 全绿后提交(kt+3 png)消息 `test(core): OpacityTest 重写为采样+golden 断言`。

### Task M2:重写 PositionedTest + FlexibleTest 清理

**Files:** Modify `widget/PositionedTest.kt`、`widget/FlexibleTest.kt`

- [ ] PositionedTest:
  - `applyParentData_test` 仅去 println;
  - `left_top_test` 重写为 `rootLayout`(Stack + Positioned left10/top10 + RED 100×100)数值 `assertGlobalRect(10,10,100,100)` + golden `widget/positioned/left_top`;采样坐标以实际为准。
- [ ] FlexibleTest:
  - 去 println;
  - 补 flex 布局数值用例(如 Row 内两 SizedBox,flex 1:2 分配剩余空间,先跑实测再按输出定期望并注释推导)。
- [ ] 提交:消息 `test(core): PositionedTest/FlexibleTest 重写与清理`。

### Task M3:重写 ClipTest

**Files:** Modify `widget/ClipTest.kt`

- [ ] 5 个场景(clipRect/clipRRect×2/clipOval/clipPath 星)去 println/debug 文本;每场景 `golden("widget/clip/<scene>")`(沿用原 id 风格),可加 1–2 采样;任何含 text 的场景替换为纯几何。
- [ ] record → verify 全绿。
- [ ] 提交:消息 `test(core): ClipTest 重写为确定性 golden`。

### Task M4:全量验证

- [ ] `./gradlew test --no-build-cache` 与 `./gradlew jar --no-build-cache` BUILD SUCCESSFUL;默认 suite 后 `git status` 干净;`main..HEAD` 全 G;无 org.junit 混入。
- [ ] 汇报修复/待议(如有)与提交 hash 清单。

# 旧渲染冒烟迁移 Batch-1b 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans.

**Goal:** 把 `DecoratedBoxTest`/`ImageFilteredTest`/`BackdropFilterTest` 重写为采样/结构断言(文字换几何、剔除外网、blur/阴影不上 golden)。

**Architecture:** 复用 testkit `snapshotPixels`/`regionStats`/`expectColorAt`/`rootLayout` 与 golden(仅锐边);blur/阴影用"同平台互比"采样;场景抽 builder 供孪生(有/无滤镜)复用。

**Spec:** `docs/superpowers/specs/2026-09-08-migrate-widget-smoke-1b-design.md`

---

## 执行前必读

- 仓库根 `D:\mine\workspace\snapshot`;分支 `test/migrate-widget-smoke-1b`(base=main)。GPG 提交(报 `No passphrase given` 原样重试;`%G?` 应 G);gradle AccessDenied → `--no-build-cache`。
- 规则:去 println/drawWidget/debug 文本;文字/外网→确定性几何;场景抽 builder;坐标/色值以**实际渲染实测**校准并注释;清晰 bug 最小修复并记录,模糊注释"待议"。
- 采样辅助建议在各文件内私有实现(如 `fun Pixmap.luminanceAt(x,y): Int` 用 `getColor` 取通道均值)。

---

### Task N1:`DecoratedBoxTest` 重写

**Files:** Modify `core/src/test/kotlin/com/muedsa/snapshot/widget/DecoratedBoxTest.kt`

- [ ] `border_radius_golden`:原 borderRadius 场景去 debug/println → `golden("widget/decorated_box/borderRadius_w200_r100")`;record→verify。
- [ ] `elevation_shadow_sampling`:代表档位 `[1,4,12,24]`,同场景 builder;断言:盒下固定采样点亮度随 elevation **非增**、远处点==WHITE、盒内==WHITE。坐标先实测(可临时打印)再定。
- [ ] `elevation_zero_no_shadow`:档位 0 → 盒下紧邻点==WHITE。
- [ ] 全绿后提交:`test(core): DecoratedBoxTest 重写(圆角 golden + 阴影采样断言)`。
- [ ] 实施期发现并修复 RenderDecoratedBox 子盒重复绘制(3154d5c),补 BACKGROUND/FOREGROUND 两条回归断言。

### Task N2:`ImageFilteredTest` 重写

**Files:** Modify `core/src/test/kotlin/com/muedsa/snapshot/widget/ImageFilteredTest.kt`

- [ ] 文字→几何(横向色带/小方块网格);`blur_clip` 用更长/更多色带保持"溢出被裁剪"意图。
- [ ] 每场景:`withFilter` 与 `withoutFilter` 孪生 builder;断言:模糊带内某点两者不同、边缘外一点为混合色、远处一点两者相同。
- [ ] 全绿后提交:`test(core): ImageFilteredTest 重写为孪生采样断言`。

### Task N3:`BackdropFilterTest` 重写

**Files:** Modify `core/src/test/kotlin/com/muedsa/snapshot/widget/BackdropFilterTest.kt`

- [ ] `blur_test`/`blur_2_test`:文字→几何;`withFilter`/`withoutFilter` 孪生;断言区域内点不同、区域外点相同。
- [ ] `blur_3_test`:外网 `DecorationImage` → 本地几何(色带网格),保留 ClipPath(45°弧)+BackdropFilter;孪生比对。
- [ ] 确认文件不再 import 网络缓存类、本文件不再依赖网络(其它网络用例仍在,留待后续批)。
- [ ] 全绿后提交:`test(core): BackdropFilterTest 重写(剔除外网,孪生采样断言)`。

### Task N4:全量验证

- [ ] `./gradlew test --no-build-cache` 与 `./gradlew jar --no-build-cache` BUILD SUCCESSFUL;`git status` 干净(本批三文件不联网、不改写仓库根);
- [ ] grep 确认三文件无 println/drawWidget/org.junit、无网络 import;`git log main..HEAD` 全 G;
- [ ] 汇报修复/待议(如有)与提交清单。

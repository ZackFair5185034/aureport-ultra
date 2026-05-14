# Handsontable 版本调研

> 调研时间：2026-05-14
> 背景：Aureport Ultra 项目使用 handsontable 6.2.2，研究升级到新版（12.x/14.x/17.x）的可行性和影响。

## 版本历史

| 版本 | 发布日期 | 架构 | License | 维护状态 |
|------|---------|------|---------|---------|
| 6.2.2 | 2018-12 | jQuery + Class API | MIT | ❌ 停止维护 |
| 7.4.x | 2021-02 | 移除 jQuery | MIT | ❌ 停止维护 |
| 8.0.0 | 2021-02 | 新渲染引擎 | MIT | ❌ 停止维护 |
| 12.0.0 | 2023-01 | TypeScript 重写 | **商业** | ✅ 维护中 |
| 14.0.0 | 2024-01 | Web Components | **商业** | ✅ 维护中 |
| 17.0.1 | 2026-03 | 现代架构 | **商业** | ✅ 维护中 |

**关键节点**：
- `6.x → 7.x`：移除 jQuery，API 仍兼容
- `8.x → 12.x`：**License 从 MIT 变为商业**，TypeScript 重写
- `12.x → 17.x`：持续商业化，架构现代化

## Aureport 实际使用方式

项目仅使用 **Handsontable 核心 API**（`edit-table/index.vue`）：

```typescript
import Handsontable from 'handsontable'

// 初始化
hot.value = new Handsontable(el, {
  startCols: 1, startRows: 1,
  fillHandle: { autoInsertRow: false },
  colHeaders: true, rowHeaders: true,
  manualColumnResize: true, manualRowResize: true,
  maxColsNumber: 700,
  width: '100%', height: '100%',
  contextMenu: buildMenuConfigure(),
})

// Hooks（核心事件监听）
Handsontable.hooks.add('afterSelectionEnd', callback, hot.value)
hot.value.addHook('afterRenderer', afterRenderer)
hot.value.addHook('afterRowResize', rowResizeHandler)
hot.value.addHook('afterColumnResize', colResizeHandler)

// 数据操作
hot.value.loadData(dataArray)
hot.value.updateSettings({ mergeCells, colWidths, rowHeights })
hot.value.render()
hot.value.destroy()
```

**非 Vue Wrapper 集成**：
- 不是用 `@handsontable/vue-handsontable-official`
- 直接 `import Handsontable from 'handsontable'` + `new Handsontable(el, config)`
- 这意味着升级到 12.x+ 需要同时替换集成方式

## 升级路径分析

### 路径 A：6.2.2 → 7.4.x（最后 MIT 版本）

```
工作量：★☆☆☆☆（极低）
License：MIT ✅
兼容性：6.x 代码 90% 直接可用

优点：
  - API 向下兼容，迁移成本极低
  - 移除 jQuery 依赖
  - 免费商用

缺点：
  - 7.x 本身也已停止维护
  - 仍是旧架构（无 TypeScript）
  - 无官方 Vue 3 wrapper
```

### 路径 B：12.x+（现代商业版）

```
工作量：★★★☆☆（中高）
License：商业 ❌（需购买）

优点：
  - TypeScript 原生支持
  - 官方 Vue 3 Wrapper（@handsontable/vue-handsontable-official）
  - 内置公式引擎（400+ 公式）
  - 现代化 UI / 主题系统
  - 持续安全更新

缺点：
  - 需购买 Commercial License（价格不公开，联系销售）
  - API Breaking Changes（Class API → Hooks API）
  - 集成方式从直接实例化 → Vue 3 Component
  - CSS 样式完全重写
```

### 路径 C：保持 6.2.2（现状）

```
工作量：0 ✅
License：MIT ✅
风险：低（有 jQuery 依赖的安全隐患）

结论：Aureport Ultra **推荐保持 6.2.2**
理由：
  1. 功能完全满足需求（无公式引擎、合并单元格、行列Resize等）
  2. 升级收益 < 迁移风险
  3. 无需购买商业 License
```

## 官方 Vue Wrapper 版本对应

| Handsontable 版本 | Vue Wrapper 包 | Vue 版本 |
|-----------------|--------------|---------|
| 12.x | @handsontable/vue-handsontable-official@4.x | Vue 3 |
| 13.x | @handsontable/vue-handsontable-official@4.x | Vue 3 |
| 14.x | @handsontable/vue-handsontable-official@5.x | Vue 3 |
| 17.x | @handsontable/vue-handsontable-official@5.1.0 | Vue 3 |

Vue Wrapper 当前最新：`5.1.0`（GitHub stars: 738）

## 关键发现

1. **handsontable 12.x+ 是商业 License**，不是免费开源
2. **Aureport 仅用核心功能**，但集成方式是直接实例化非 Vue 组件
3. **6.2.2 对本项目完全够用**，升级收益不明显
4. **npm view 超时问题**：npmjs.org 在当前环境网络较慢，建议用 `curl` 直接调 GitHub API

## 未来升级建议

如果未来确定需要升级：
1. 先联系 Handsontable 销售获取商业 License 报价
2. 阅读官方 Migration Guide（文档已更新）
3. 使用 `@handsontable/vue-handsontable-official` 重构 `edit-table/index.vue`
4. 测试所有 hooks 兼容性
5. 重写 CSS 样式（6.x jQuery 样式系统 vs 12.x+ CSS 变量系统）

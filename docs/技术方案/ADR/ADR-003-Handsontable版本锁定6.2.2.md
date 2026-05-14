# ADR-003: Handsontable 版本锁定 6.2.2

## 状态

已接受

## 背景

Aureport Ultra 报表设计器的表格编辑功能基于 Handsontable。当前项目使用 6.2.2 版本（2018年发布，MIT License），需要评估是否升级到新版（12.x/14.x/17.x）。

## 决策

**保持 Handsontable 6.2.2，不升级。**

## 理由

### License 因素
- **6.2.2**: MIT License ✅ 可免费商用
- **12.x+**: 商业 License ❌ 需购买

### 功能因素
- Aureport 仅使用 Handsontable **核心 API**（`loadData`, `updateSettings`, `render`, `hooks`）
- 6.2.2 功能完全满足需求（无公式引擎依赖）
- 12.x+ 的新增功能（本项目不需要）：
  - 内置公式引擎（400+ 公式）
  - Web Components 架构
  - 官方 Vue 3 Wrapper

### 迁移成本
- 升级到 12.x+ 需要：
  - 重写 `edit-table/index.vue`（直接实例化 → Vue 组件）
  - 重写 CSS 样式系统（jQuery 样式 → CSS 变量）
  - 测试所有 hooks 兼容性
- 迁移工作量：★★★☆☆（中高）

### 风险
- 6.2.2 已停止维护，存在安全隐患（jQuery 依赖）
- 但 Handsontable 核心代码稳定，漏洞利用概率低

## 后果

**正面**：
- 无需购买商业 License
- 迁移成本为 0
- 现有代码稳定运行

**负面**：
- 无法使用新版功能（如公式引擎）
- 继续依赖停止维护的版本
- 存在潜在安全风险（低概率）

## 未来升级路径

如果未来确定需要升级：
1. 联系 Handsontable 销售获取商业 License 报价
2. 评估功能需求是否值得购买
3. 阅读官方 Migration Guide
4. 使用 `@handsontable/vue-handsontable-official` 重构

---

# ADR-002: 报表文件存储方案

## 状态

已接受

## 背景

需要确定报表定义的存储方式。UReport2 原版使用数据库存储，存在：
- 数据库 schema 变更管理复杂
- 无法使用 Git 管理报表版本
- 部署时需要额外数据库迁移

## 决策

**采用文件系统存储（.ureport.xml 文件），数据库仅用作数据源查询。**

## 理由

### 文件存储优势
- **版本管理**：可以使用 Git 管理报表 XML 文件
- **简化部署**：无需数据库迁移步骤
- **独立性强**：不依赖特定数据库
- **热更新**：`file:` 前缀的报表保存即生效

### 数据库仅用作数据源
- 报表的数据集通过 SQL 查询从数据库获取
- 这是报表引擎的标准做法（FineReport、润乾同理）
- 保持数据库连接灵活性（MySQL/Oracle/SQLServer/达梦）

## 报表路径前缀

| 前缀 | 存储位置 | 变更生效 |
|------|---------|---------|
| `classpath:` | JAR 内 classpath | 需重新 `mvn package` |
| `file:` | `fileStoreDir` 目录 | 保存即生效 |

## 后果

**正面**：
- 报表可以 Git 版本化管理
- 部署简单（复制文件）
- 热更新支持

**负面**：
- 报表文件分散在服务器文件系统
- 多节点部署需要共享存储（如 NFS）
- 无法用 SQL 查询报表列表（需要扫描目录）

# 开发进度目录

本目录用于记录版本发布、已完成功能、里程碑等。

## 文件索引

|| 文档 | 内容 | 状态 |
||------|------|------|
|| [CHANGELOG.md](./CHANGELOG.md) | 版本变更 + 开发日志（详细） | ✅ 进行中 |

## 版本规范

采用语义化版本 `主版本.次版本.修订号`：
- **主版本**：不兼容的 API 变更
- **次版本**：向后兼容的新功能
- **修订号**：向后兼容的问题修复

## 提交规范

提交信息格式：`type(scope): description`

```
feat(core): 新增 IterateAggregate 嵌套迭代聚合
fix(ui): 修复搜索表单设计器白屏问题
chore(server): 升级 Spring Boot 到 3.2.5
docs: 更新 README
```

类型：`feat`/`fix`/`docs`/`style`/`refactor`/`test`/`chore`

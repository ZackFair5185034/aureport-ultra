# Aureport Ultra

基于 Spring Boot 3 + Vue 的高性能 Java 报表引擎。

## 项目简介

Aureport Ultra 是一款基于开源项目 UReport2 重构的 Java 高性能报表引擎，通过迭代单元格可以实现任意复杂的中国式报表。相较于 UReport2，Aureport Ultra 在技术架构上进行了全新升级，后端基于 SpringBoot 框架开发、前端采用 Vue 框架构建，技术选型贴合当下主流项目开发标准，可精准适配各类实际开发需求。

## 系统要求

- JDK >= 17
- MySQL >= 5.7
- Node.js >= 14.0

## 快速开始

```bash
# 编译整个项目
cd aureport-ultra-server && mvn clean package -DskipTests

# 运行
cd aureport-ultra-server/aureport-ultra-pub && mvn spring-boot:run
```

## 技术栈

- **后端**: Spring Boot 3.2.5, ANTLR4, Apache POI, iText
- **前端**: Vue 3, Element Plus
- **数据库**: MySQL / Oracle / SQL Server / 达梦

## License

Apache-2.0

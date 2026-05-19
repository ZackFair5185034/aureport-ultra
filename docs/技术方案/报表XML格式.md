# 报表 XML 格式规范

> 本文档源自技能文档 `references/ureport-xml-format.md`，为项目技术方案的一部分。

## 完整报表文件结构

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ureport xmlns="http://www.example.org/ureport2"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.example.org/ureport2 http://www.example.org/ureport2">

  <!-- 第1行：报表标题（跨7列，居中大字） -->
  <cell expand="None" name="A1" row="1" col="1" col-span="7">
    <simple-value><![CDATA[销售订单汇总表]]></simple-value>
    <cell-style font-size="16" font-weight="bold" align="center" valign="middle"
                forecolor="#1a56db"></cell-style>
  </cell>

  <!-- 第2行：表头（7个单元格） -->
  <cell expand="None" name="A2" row="2" col="1">
    <simple-value><![CDATA[订单号]]></simple-value>
    <cell-style font-size="10" font-weight="bold" align="center" valign="middle"
                background-color="#e0e7ff"></cell-style>
  </cell>
  <!-- ... B2~G2 同理 ... -->

  <!-- 第3行：数据行（expand="Down" 表示向下扩展） -->
  <cell expand="Down" name="A3" row="3" col="1">
    <dataset-value aggregate="select" property="order_no" dataset-name="orders"></dataset-value>
    <cell-style font-size="10" align="center" valign="middle"></cell-style>
  </cell>
  <!-- B3, C3, D3, E3, F3, G3 同理 -->

  <!-- 第4行：汇总行 -->
  <cell expand="None" name="A4" row="4" col="1" col-span="5">
    <simple-value><![CDATA[总计]]></simple-value>
    <cell-style font-size="10" font-weight="bold" align="right" valign="middle"
                background-color="#fef3c7"></cell-style>
  </cell>
  <cell expand="None" name="F4" row="4" col="6">
    <expression-value><![CDATA[sum(F3)]]></expression-value>
    <cell-style font-size="11" font-weight="bold" align="right" valign="middle"
                background-color="#fef3c7" forecolor="#dc2626"></cell-style>
  </cell>

  <!-- 行高定义 -->
  <row row-number="1" height="40"/>
  <row row-number="2" height="28"/>
  <row row-number="3" height="24"/>
  <row row-number="4" height="30"/>

  <!-- 列宽定义 -->
  <column col-number="1" width="120"/>
  <column col-number="2" width="100"/>
  <!-- ... -->

  <!-- 页面设置 -->
  <paper type="A4" orientation="landscape" paging-mode="fitpage"></paper>

  <!-- 数据源定义 -->
  <datasource name="mysql" type="jdbc"
              driver="com.mysql.cj.jdbc.Driver"
              username="urp" password="T7nZ7weNG4FBFi75"
              url="jdbc:mysql://192.168.101.188:3306/urp?serverTimezone=Asia/Shanghai&amp;useUnicode=true&amp;characterEncoding=utf-8">
    <dataset name="orders" type="sql">
      <sql><![CDATA[
        SELECT o.order_no, c.name AS customer_name, p.name AS product_name,
               p.price AS unit_price, o.quantity, o.total_amount, o.status
        FROM orders o
        LEFT JOIN customers c ON o.customer_id = c.id
        LEFT JOIN products p ON o.product_id = p.id
        ORDER BY o.order_date DESC, o.order_no DESC
      ]]></sql>
      <field name="order_no"/>
      <field name="customer_name"/>
      <field name="product_name"/>
      <field name="unit_price"/>
      <field name="quantity"/>
      <field name="total_amount"/>
      <field name="status"/>
    </dataset>
  </datasource>
</ureport>
```

## 核心元素说明

### cell 标签属性

| 属性 | 值 | 说明 |
|------|-----|------|
| `expand` | `None` / `Down` / `Right` | 扩展方向（None=固定，Down=向下扩展行，Right=向右扩展列） |
| `name` | `A1`, `B2` 等 | Excel风格单元格名 |
| `row` | 数字 | 行号（从1开始） |
| `col` | 数字 | 列号（从1开始） |
| `col-span` | 数字 | 跨列数 |
| `left-cell` | `A2` 等 | 左父格名称（**XML 用 hyphenated 格式，不是 camelCase**） |
| `top-cell` | `A1` 等 | 上父格名称（用于行列双维度交叉报表） |

> ⚠️ **XML 属性名区分大小写且必须匹配引擎解析器**：引擎 `CellParser.java` 用 `element.attributeValue("left-cell")` 和 `element.attributeValue("top-cell")`，XML 必须写 `left-cell` / `top-cell`，**不是** `leftParentCellName` / `topParentCellName`（那是 Java 实体类的 getter 方法名）。

### cell-style 属性

| 属性 | 说明 |
|------|------|
| `font-size` | 字号（如 10, 16） |
| `font-weight` | 粗体（bold） |
| `align` | 水平对齐（left, center, right） |
| `valign` | 垂直对齐（top, middle, bottom） |
| `forecolor` | 前景色（如 #dc2626 红色） |
| `background-color` | 背景色（如 #e0e7ff 淡蓝） |

### dataset-value（数据集单元格）

| 属性 | 说明 |
|------|------|
| `aggregate` | `select`=普通字段, `group`=分组字段, `sum`/`avg`/`count`/`max`/`min`=聚合, `iterate`=嵌套迭代 |
| `property` | 字段名（对应 dataset 中的 field） |
| `dataset-name` | 数据集名称 |
| `order` | 排序（asc, desc） |
| `nest-property` | 嵌套属性路径（用于 `iterate` 聚合类型，如 `familyMembers`） |

### expression-value（表达式单元格）

支持 UReport2 表达式语法，如 `sum(F3)`、`100>151 ? "小计" : "总计"`

## 存储位置

```
aureport-ultra-server/aureport-ultra-web/src/main/resources/template/
├── template.ureport.xml      # 空白模板
└── sales_report.ureport.xml  # 销售订单报表（测试用）

reports/                       # 运行时报表存储（file: 前缀）
└── employee_family.ureport.xml  # 员工家庭成员报表（嵌套迭代示例）
```

## ⚠️ 重要：classpath XML 必须重新编译，file: XML 无需编译

**`classpath:` 前缀**（存放在 JAR 的 classpath 内）：新增/修改后必须 `mvn clean package` 重新打包。

**`file:` 前缀**（存放在 `reports/` 目录）：保存后**立即生效**，无需重启后端。

```bash
cd /home/coding/aureport-ultra/aureport-ultra-server
mvn clean package -DskipTests
# 然后重启后端
```

## 预览接口

```bash
# 正确的预览接口（后端直接）
curl "http://localhost:8050/report/html/loadHtml?reportPath=file:sales_report.ureport.xml"

# 通过前端代理
http://localhost:3000/report/preview?reportPath=file:sales_report.ureport.xml
```

返回格式：
```json
{
  "content": "<table>...</table>",
  "style": "._A1{...}",
  "totalPage": 1,
  "searchForm": null,
  "chartDatas": []
}
```

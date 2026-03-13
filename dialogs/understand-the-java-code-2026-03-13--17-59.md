# 理解 Apache Commons CSV 项目

> 生成时间：2026-03-13 17:59  
> 项目版本：1.14.2-SNAPSHOT（最新稳定发布：1.14.1）  
> 作者：GitHub Copilot（Claude Sonnet 4.6）

---

## 目录

1. [主要功能、特点、优势、劣势和适用场景](#1-主要功能特点优势劣势和适用场景)
2. [如何在 Windows 11 PowerShell 7 中编译并运行演示应用](#2-如何在-windows-11-powershell-7-中编译并运行演示应用)
3. [如何运行自动化测试](#3-如何运行自动化测试)

---

## 1. 主要功能、特点、优势、劣势和适用场景

### 1.1 主要功能

Apache Commons CSV 是 Apache Commons 家族中专门处理 CSV（逗号分隔值）文件的库，提供两大核心能力：

| 能力 | 核心类 | 说明 |
|------|--------|------|
| **读（解析）** | `CSVParser` | 从文件/字符串/URL/InputStream 解析 CSV，支持 Stream/Iterator/Iterable 三种访问方式 |
| **写（格式化输出）** | `CSVPrinter` | 将 Java 对象或 SQL `ResultSet` 输出为 CSV 文本，支持逐字段、逐行、批量写入 |
| **格式配置** | `CSVFormat` | 12 种预定义方言格式，也可自定义分隔符、引号字符、注释符等参数 |
| **记录访问** | `CSVRecord` | 代表 CSV 中的一行，支持按列索引或列名访问字段值 |

### 1.2 支持的预定义格式方言

| 格式名 | 说明 |
|--------|------|
| `DEFAULT` | 标准 CSV（RFC 4180 的宽松变体） |
| `RFC4180` | 严格符合 RFC 4180 标准 |
| `EXCEL` | 与 Microsoft Excel 兼容 |
| `MYSQL` | MySQL 的 `LOAD DATA INFILE` 格式 |
| `POSTGRESQL_CSV` | PostgreSQL `COPY ... CSV` 格式 |
| `POSTGRESQL_TEXT` | PostgreSQL `COPY ... TEXT` 格式 |
| `ORACLE` | Oracle SQL*Loader 格式 |
| `MONGODB_CSV` | MongoDB mongoexport CSV 格式 |
| `MONGODB_TSV` | MongoDB mongoexport TSV 格式 |
| `INFORMIX_UNLOAD` | Informix UNLOAD 格式 |
| `INFORMIX_UNLOAD_CSV` | Informix UNLOAD CSV 格式 |
| `TDF` | Tab 分隔格式（Tab-Delimited Format） |

### 1.3 特点

- **轻量零侵入**：只依赖 `commons-io` 和 `commons-codec`，无框架绑定
- **多方言支持**：12 种预定义格式，覆盖主流数据库导入导出需求
- **SQL 直接集成**：`CSVPrinter.printRecords(ResultSet)` 可将数据库结果集一行代码导出为 CSV
- **BOM/Unicode 支持**：正确处理 UTF-8 BOM、日文、Emoji 等特殊字符
- **流式处理**：`CSVParser.stream()` 返回 `Stream<CSVRecord>`，可用 Java 8 Stream API 过滤/映射
- **高度可定制**：可自定义分隔符、引号、转义符、注释符、行终止符、空值处理等

### 1.4 优势

1. **API 极简**：3 行代码即可完成文件解析，学习曲线极低
2. **文档完善**：Javadoc 覆盖率接近 100%，官方用户指南详尽
3. **Apache 质量保证**：JaCoCo 代码覆盖率要求 99%（指令/行），SpotBugs/PMD/Checkstyle 全量静态检查
4. **Java 8+ 兼容**：无需升级 JDK，与现有企业项目无缝集成
5. **活跃维护**：持续更新，安全漏洞响应及时

### 1.5 劣势

1. **不支持 Excel `.xlsx/.xls` 格式**：如需处理 Excel 二进制格式，需改用 Apache POI
2. **无对象自动映射**：不内置 CSV ↔ Java Bean 的自动映射（需自行编写，或使用 OpenCSV/Jackson Dataformat CSV）
3. **无数据验证**：不提供字段类型验证、必填检查等数据质量功能
4. **大文件性能一般**：对于 GB 级超大文件，需自行管理内存（逐行处理，避免 `getRecords()` 全量加载）

### 1.6 适用场景

| 场景 | 说明 |
|------|------|
| **数据导入导出** | 系统间数据交换，如从 ERP 导出订单到 CSV |
| **数据库数据备份** | 将 SQL 查询结果直接导出为 CSV |
| **日志/报表生成** | 生成结构化的运营报表 |
| **ETL 数据管道** | 在数据仓库流程中作为数据提取/加载工具 |
| **测试数据管理** | 用 CSV 文件驱动参数化测试 |
| **微服务数据交换** | 轻量级的服务间批量数据传递 |

---

## 2. 如何在 Windows 11 PowerShell 7 中编译并运行演示应用

### 2.1 前提条件

在 PowerShell 7 中执行以下命令，确认环境已就绪：

```powershell
# 检查 Java 版本（需要 JDK 8 或更高版本）
java -version

# 检查 Maven 版本（需要 Maven 3.6 或更高版本）
mvn -version
```

**预期输出示例：**

```
openjdk version "17.0.x" ...
Apache Maven 3.9.x ...
```

> 如果 `java` 或 `mvn` 命令不可用，请先安装 [JDK](https://adoptium.net/) 和 [Apache Maven](https://maven.apache.org/download.cgi)，并将其 `bin` 目录添加到系统 `PATH` 环境变量。

### 2.2 演示应用说明

演示应用位于 `demo\` 目录，模拟**员工花名册管理**场景，包含三个阶段：

| 阶段 | 操作 | 展示能力 |
|------|------|----------|
| 阶段1 | 将5名员工数据写入 `employees.csv` | `CSVPrinter` 写出 CSV |
| 阶段2 | 解析 `employees.csv`，按列名打印所有记录 | `CSVParser` 读入 + 按列名访问 |
| 阶段3 | 用 Java Stream 过滤月薪 > 15000 的员工 | `CSVParser.stream()` 流式处理 |

### 2.3 编译步骤

```powershell
# 步骤1：切换到 demo 目录
cd C:\Users\wubin\OOR\katas\commons-csv\demo

# 步骤2：编译（Maven 会自动从 Maven Central 下载 commons-csv 1.14.1 依赖）
mvn compile
```

**预期输出（关键行）：**

```
[INFO] Downloading from central: https://repo1.maven.org/maven2/...
[INFO] BUILD SUCCESS
```

> 首次运行需要联网下载依赖，约需 10-30 秒。依赖下载后会缓存在本机 Maven 仓库（`~\.m2\repository\`），后续运行无需重新下载。

### 2.4 运行演示应用

```powershell
# 步骤3：运行演示程序（需在 demo\ 目录下执行）
mvn exec:java
```

### 2.5 预期控制台输出

```
=== [阶段1] CSV 写出完成 ===
文件已保存到：C:\Users\wubin\OOR\katas\commons-csv\demo\employees.csv

=== [阶段2] 解析 CSV 并读取所有员工 ===
工号   姓名     部门         月薪(元)
--------------------------------------
E001   张伟     研发部       22000
E002   李娜     产品部       18000
E003   王芳     市场部       12000
E004   刘洋     研发部       25000
E005   陈静     人力资源部   10000

=== [阶段3] 流式筛选：月薪 > 15000 的员工 ===
  E001  张伟  22000 元
  E002  李娜  18000 元
  E004  刘洋  25000 元

演示完毕！请用文本编辑器打开 employees.csv 查看原始 CSV 内容。
```

### 2.6 查看生成的 CSV 文件

运行后，`employees.csv` 文件会生成在 `demo\` 目录下（即 PowerShell 当前工作目录）：

```powershell
# 在 PowerShell 中直接查看 CSV 内容
Get-Content .\employees.csv
```

**预期 CSV 文件内容：**

```
工号,姓名,部门,月薪(元)
E001,张伟,研发部,22000
E002,李娜,产品部,18000
E003,王芳,市场部,12000
E004,刘洋,研发部,25000
E005,陈静,人力资源部,10000
```

> 也可以直接用记事本或 VS Code 打开 `demo\employees.csv` 文件查看。

---

## 3. 如何运行自动化测试

### 3.1 切换到项目根目录

```powershell
cd C:\Users\wubin\OOR\katas\commons-csv
```

### 3.2 仅运行测试（推荐初次使用）

以下命令只执行单元测试和集成测试，**跳过** Checkstyle/SpotBugs/PMD 等静态代码分析：

```powershell
mvn clean test
```

**预期输出（关键行）：**

```
[INFO] Running org.apache.commons.csv.CSVParserTest
[INFO] Tests run: 182, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.apache.commons.csv.CSVPrinterTest
[INFO] Tests run: 116, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] BUILD SUCCESS
```

### 3.3 测试框架和测试规模说明

| 测试类 | 主要测试内容 |
|--------|------------|
| `CSVParserTest` | CSV 解析正确性，包含边界情况（空值、引号嵌套、多行字段等） |
| `CSVPrinterTest` | CSV 写出格式正确性，含 SQL ResultSet 导出测试 |
| `CSVFormatTest` | 12 种预定义格式参数验证 |
| `CSVFormatPredefinedTest` | 每种格式的序列化/反序列化一致性 |
| `CSVRecordTest` | 记录字段访问、列名映射正确性 |
| `LexerTest` | 词法分析器底层分词逻辑 |
| `ExtendedBufferedReaderTest` | 带行号追踪的缓冲读取器 |
| `CSVDuplicateHeaderTest` | 重复表头的各种处理策略 |
| `JiraCsv196Test` / `JiraCsv318Test` | 特定 JIRA 缺陷的回归测试 |

- **测试框架**：JUnit 5（`org.junit.jupiter`）
- **测试风格**：含普通单元测试、参数化测试（`@ParameterizedTest`）、集成测试

### 3.4 完整检查（可选，包含静态分析和覆盖率）

```powershell
# 运行所有检查：测试 + Checkstyle + SpotBugs + PMD + JaCoCo 覆盖率
mvn
```

> **注意**：此命令执行时间较长（约 2-5 分钟），会运行 Checkstyle 代码风格、SpotBugs 潜在 Bug 扫描、PMD 代码质量等全量检查。项目要求代码覆盖率不低于 99%（指令/行）。

### 3.5 测试报告位置

测试完成后，报告文件位于：

```
target\surefire-reports\           ← 各测试类的 XML 和 TXT 格式报告
target\site\jacoco\index.html      ← JaCoCo 代码覆盖率 HTML 报告（需运行 mvn site）
```

```powershell
# 查看文本格式的测试报告摘要（示例）
Get-Content target\surefire-reports\org.apache.commons.csv.CSVParserTest.txt | Select-Object -First 10
```

---

*本文档由 GitHub Copilot 根据项目 README.md 及源码自动生成，供快速上手参考。*

package demo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 演示场景：员工花名册管理
 *
 * 本程序演示 Apache Commons CSV 的三项核心能力：
 *   1. 写出：将员工数据写入 CSV 文件
 *   2. 读入：从 CSV 文件解析并按列名访问数据
 *   3. 流式筛选：用 Java Stream 过滤高薪员工
 */
public class CsvDemo {

    private static final String CSV_FILE = "employees.csv";

    // 表头：工号, 姓名, 部门, 月薪(元)
    private static final String[] HEADERS = {"工号", "姓名", "部门", "月薪(元)"};

    // 员工数据
    private static final List<Object[]> EMPLOYEES = Arrays.asList(
        new Object[]{"E001", "张伟",   "研发部", 22000},
        new Object[]{"E002", "李娜",   "产品部", 18000},
        new Object[]{"E003", "王芳",   "市场部", 12000},
        new Object[]{"E004", "刘洋",   "研发部", 25000},
        new Object[]{"E005", "陈静",   "人力资源部", 10000}
    );

    public static void main(String[] args) throws Exception {
        writeCsv();
        readCsv();
        filterHighSalary();
    }

    // ── 阶段1：写出 ──────────────────────────────────────────────────────────
    private static void writeCsv() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        try (Writer writer = new FileWriter(CSV_FILE, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Object[] row : EMPLOYEES) {
                printer.printRecord(row);
            }
        }

        System.out.println("=== [阶段1] CSV 写出完成 ===");
        System.out.println("文件已保存到：" + new java.io.File(CSV_FILE).getAbsolutePath());
        System.out.println();
    }

    // ── 阶段2：读入并打印 ────────────────────────────────────────────────────
    private static void readCsv() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .build();

        System.out.println("=== [阶段2] 解析 CSV 并读取所有员工 ===");
        System.out.printf("%-6s %-8s %-12s %-10s%n", "工号", "姓名", "部门", "月薪(元)");
        System.out.println("--------------------------------------");

        try (Reader reader = new FileReader(CSV_FILE, StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, format)) {
            for (CSVRecord record : parser) {
                System.out.printf("%-6s %-8s %-12s %-10s%n",
                        record.get("工号"),
                        record.get("姓名"),
                        record.get("部门"),
                        record.get("月薪(元)"));
            }
        }
        System.out.println();
    }

    // ── 阶段3：流式筛选高薪员工（月薪 > 15000）────────────────────────────
    private static void filterHighSalary() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .build();

        System.out.println("=== [阶段3] 流式筛选：月薪 > 15000 的员工 ===");

        try (Reader reader = new FileReader(CSV_FILE, StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, format)) {
            parser.stream()
                  .filter(r -> Integer.parseInt(r.get("月薪(元)").trim()) > 15000)
                  .forEach(r -> System.out.printf("  %s  %s  %s 元%n",
                          r.get("工号"), r.get("姓名"), r.get("月薪(元)")));
        }
        System.out.println();
        System.out.println("演示完毕！请用文本编辑器打开 employees.csv 查看原始 CSV 内容。");
    }
}

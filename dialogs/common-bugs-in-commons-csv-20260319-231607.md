# commons-csv 生产代码常见 Bug 清单

> 基于对 `src/main/java/org/apache/commons/csv/` 下全部生产代码的分析整理。

---

## 1. 差一错误（Off-by-One Error）

**简述：** 在边界判断中将严格小于（`<`）误写为小于等于（`<=`），或反之。典型位置包括 `CSVRecord.isSet(int index)` 的范围检查（`index < values.length`），以及 `Lexer.trimTrailingSpaces` 的循环条件（`length > 0`）。一旦出错，会使调用方误以为某个越界索引合法，随后在 `get(int)` 中引发 `ArrayIndexOutOfBoundsException`。

---

## 2. 行号重复计数（Line Number Double-Count）

**简述：** `ExtendedBufferedReader.read()` 通过判断 `CR`、`LF`、`\r\n` 来递增 `lineNumber`。若逻辑条件（如 `lastChar != CR`）被反转或遗漏，`\r\n` 序列会被计作两次换行，导致 `getCurrentLineNumber()` 返回的行号偏大，错误信息和字节位置追踪均失准。

---

## 3. EOF 检测逻辑错误（Wrong EOF Detection）

**简述：** `Lexer.nextToken()` 中有一处复合条件：`!isLastTokenDelimiter && isEndOfFile(c)`，用于在分隔符后遇到 EOF 时正确结束解析。若将 `&&` 误写为 `||`，任何分隔符或任何 EOF 都会触发 EOF 令牌，导致记录被提前截断，尾部字段丢失。

---

## 4. 尾部空白修剪越界（Trim Trailing Spaces Index Bug）

**简述：** `Lexer.trimTrailingSpaces` 使用 `buffer.charAt(length - 1)` 检查末尾字符。若误写为 `buffer.charAt(length)`，当字段末尾存在空白字符时，`length` 会等于已减小后的有效长度，但仍在有效范围外，导致 `StringIndexOutOfBoundsException`，使所有开启了 `ignoreSurroundingSpaces` 的解析崩溃。

---

## 5. 转义字符处理错误（Escape Sequence Handling Bug）

**简述：** `Lexer.readEscape()` 通过 `switch` 语句将 `\r`、`\n`、`\t` 等转义序列映射为对应控制字符。若某个 `case` 分支的返回值被错误替换（例如把 `return Constants.CR` 改为 `return Constants.LF`），则转义后的字符类型改变，CSV 中的转义换行符会被解析为错误的控制字符，破坏多行字段的正确性。

---

## 6. 引号双写检测缺失（Quote Doubling Detection Bug）

**简述：** `Lexer.parseEncapsulatedToken()` 通过 `isQuoteChar(reader.peek())` 判断是否为连续两个引号（即转义引号）。若将 `peek()` 的比较逻辑反转（如改为 `!isQuoteChar`），则真正的转义引号会被当成字段结束标志，导致引号后的内容被截断或引发解析异常。

---

## 7. 多字符分隔符读取偏移错误（Multi-char Delimiter Buffer Index Bug）

**简述：** `Lexer.isDelimiter(int ch)` 对多字符分隔符使用 `delimiterBuf` 进行 peek 比较：`delimiterBuf[i] != delimiter[i + 1]`。若索引偏移写错（如 `delimiter[i]` 而非 `delimiter[i + 1]`），分隔符的首字符会被重复比较，导致合法分隔符被误判为普通字符，记录无法正确切分。

---

## 8. 字符位置计数包含 EOF（Character Position Counts EOF）

**简述：** `ExtendedBufferedReader.read()` 在每次读取后无条件执行 `position++`，即使返回的是 `EOF`（-1）。这会使 `getPosition()` 在流末尾多计一位，导致依赖字符偏移量的断点续传或错误定位逻辑出现系统性偏差。

---

## 9. 空指针解引用（Null Pointer Dereference）

**简述：** `CSVRecord.get(String name)` 先通过 `getHeaderMapRaw()` 获取 header map，若 `parser` 为 null（反序列化后的 `CSVRecord`），则返回 null，随后会抛出 `IllegalStateException`。但 `isSet(String)` 和 `putIn(Map)` 在调用 `getHeaderMapRaw()` 后各有一处判断，若某处判断被移除，则会导致空指针异常而非预期的优雅降级。

---

## 10. 注释行错误附加到记录（Comment Attached to Wrong Record）

**简述：** CSV 注释在 `CSVParser` 中被"附加到下一条记录"。若记录收集逻辑在累计注释后未正确清空 `headerComment` 缓冲，注释内容可能被重复附加到多条连续记录，或在仅有注释而无数据行时造成注释内容意外丢失。

---

## 11. 并发访问时 newRecord 标志竞态（newRecord Flag Race Condition）

**简述：** `CSVPrinter` 使用 `ReentrantLock` 保护 `print` 和 `println`，但 `printRecord(Stream<?>)` 对并行流调用 `printRaw`（绕过锁），而对串行流调用 `print`（加锁）。若错误地对并行流也走加锁路径，则 `newRecord` 标志仍可能在多线程场景下被竞态修改，导致分隔符丢失或重复输出。

---

## 12. 记录号起始值错误（Record Number Off-by-One at Start）

**简述：** `CSVParser.Builder` 默认将 `recordNumber` 初始化为 `1`，并在构造器中将其作为"下一条记录号"传递。若将初始值误设为 `0`，第一条解析到的记录号将为 `0` 而非 `1`，破坏了文档约定（记录号从 1 开始），导致依赖记录号的跳过逻辑或日志出现错位。

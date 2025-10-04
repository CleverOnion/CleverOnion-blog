# Redis 缓存序列化问题解决方案

**问题发现时间**: 2025-10-04  
**问题类型**: Jackson JSON 反序列化错误  
**影响范围**: 所有缓存的领域对象  
**解决方案**: 为值对象添加 Jackson 注解

---

## 1. 问题描述

### 1.1 错误信息

```
org.springframework.data.redis.serializer.SerializationException:
Could not read JSON: Cannot construct instance of
`com.cleveronion.blog.domain.article.valueobject.ArticleId`
(although at least one Creator exists): cannot deserialize from Object value
(no delegate- or property-based Creator)
```

### 1.2 问题原因

**值对象的典型设计**（DDD 模式）：

```java
public class ArticleId {
    private final String value;  // final 字段，不可变

    public ArticleId(String value) {  // 只有带参构造函数
        this.value = value;
    }

    public String getValue() {  // 只有 getter
        return value;
    }
}
```

**Jackson 反序列化的要求**：

- ❌ 需要**无参构造函数** OR
- ❌ 需要**setter 方法** OR
- ✅ 需要 `@JsonCreator` + `@JsonProperty` 注解

**序列化流程分析**：

```
写入Redis（序列化）:
ArticleId { value: "123" } → {"value": "123"} ✅ 成功（有 getValue()）

读取Redis（反序列化）:
{"value": "123"} → ArticleId ❌ 失败（Jackson不知道如何构造）
```

---

## 2. 解决方案：添加 Jackson 注解

### 2.1 单字段值对象修改模板

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArticleId {
    private final String value;

    @JsonCreator  // 告诉 Jackson 使用这个构造函数
    public ArticleId(@JsonProperty("value") String value) {  // 参数名映射到JSON字段
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // equals, hashCode, toString 方法不变...
}
```

### 2.2 多字段值对象修改模板

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArticleContent {
    private final String title;
    private final String content;
    private final String summary;

    @JsonCreator
    public ArticleContent(
        @JsonProperty("title") String title,
        @JsonProperty("content") String content,
        @JsonProperty("summary") String summary
    ) {
        // 验证逻辑
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        this.title = title;
        this.content = content;
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }

    // 其他方法不变...
}
```

---

## 3. 需要修改的文件清单

### 3.1 文章模块值对象（6 个）

#### ✅ 核心 ID 类（4 个）

| 序号 | 文件路径                                     | 类名       | 字段          | 改动                              |
| ---- | -------------------------------------------- | ---------- | ------------- | --------------------------------- |
| 1    | `domain/article/valueobject/ArticleId.java`  | ArticleId  | value: String | 添加 @JsonCreator + @JsonProperty |
| 2    | `domain/article/valueobject/CategoryId.java` | CategoryId | value: Long   | 添加 @JsonCreator + @JsonProperty |
| 3    | `domain/article/valueobject/TagId.java`      | TagId      | value: Long   | 添加 @JsonCreator + @JsonProperty |
| 4    | `domain/article/valueobject/AuthorId.java`   | AuthorId   | value: Long   | 添加 @JsonCreator + @JsonProperty |

#### ✅ 复杂值对象（1 个）

| 序号 | 文件路径                                         | 类名           | 字段                    | 改动                |
| ---- | ------------------------------------------------ | -------------- | ----------------------- | ------------------- |
| 5    | `domain/article/valueobject/ArticleContent.java` | ArticleContent | title, content, summary | 添加注解到 3 个参数 |

#### 📝 枚举类（不需要修改）

| 序号 | 文件路径                                        | 类名          | 说明                          |
| ---- | ----------------------------------------------- | ------------- | ----------------------------- |
| 6    | `domain/article/valueobject/ArticleStatus.java` | ArticleStatus | 枚举类型，Jackson 自动处理 ✅ |

### 3.2 用户模块值对象（2 个）

| 序号 | 文件路径                                | 类名     | 字段        | 改动                              |
| ---- | --------------------------------------- | -------- | ----------- | --------------------------------- |
| 7    | `domain/user/valueobject/UserId.java`   | UserId   | value: Long | 添加 @JsonCreator + @JsonProperty |
| 8    | `domain/user/valueobject/GitHubId.java` | GitHubId | value: Long | 添加 @JsonCreator + @JsonProperty |

### 3.3 评论模块值对象（1 个）

| 序号 | 文件路径                                    | 类名      | 字段        | 改动                              |
| ---- | ------------------------------------------- | --------- | ----------- | --------------------------------- |
| 9    | `domain/comment/valueobject/CommentId.java` | CommentId | value: Long | 添加 @JsonCreator + @JsonProperty |

### 3.4 总计

- ✅ **必须修改**: 8 个值对象类
- 📝 **不需要修改**: 1 个枚举类
- ⏰ **预计时间**: 20-30 分钟

---

## 4. 详细修改说明

### 4.1 ArticleId 修改

**文件路径**: `src/main/java/com/cleveronion/blog/domain/article/valueobject/ArticleId.java`

**修改前**:

```java
public class ArticleId {
    private final String value;

    public ArticleId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
```

**修改后**:

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArticleId {
    private final String value;

    @JsonCreator
    public ArticleId(@JsonProperty("value") String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
```

**改动说明**:

1. 添加导入：`import com.fasterxml.jackson.annotation.JsonCreator;`
2. 添加导入：`import com.fasterxml.jackson.annotation.JsonProperty;`
3. 构造函数前添加：`@JsonCreator`
4. 构造函数参数前添加：`@JsonProperty("value")`

---

### 4.2 CategoryId 修改

**文件路径**: `src/main/java/com/cleveronion/blog/domain/article/valueobject/CategoryId.java`

**字段类型**: `Long`

**修改方式**: 同 ArticleId，注意字段类型是 Long

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryId {
    private final Long value;

    @JsonCreator
    public CategoryId(@JsonProperty("value") Long value) {
        if (value == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
```

---

### 4.3 TagId 修改

**文件路径**: `src/main/java/com/cleveronion/blog/domain/article/valueobject/TagId.java`

**字段类型**: `Long`

**修改方式**: 同 CategoryId

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TagId {
    private final Long value;

    @JsonCreator
    public TagId(@JsonProperty("value") Long value) {
        if (value == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
```

---

### 4.4 AuthorId 修改

**文件路径**: `src/main/java/com/cleveronion/blog/domain/article/valueobject/AuthorId.java`

**字段类型**: `Long`

**修改方式**: 同 CategoryId

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorId {
    private final Long value;

    @JsonCreator
    public AuthorId(@JsonProperty("value") Long value) {
        if (value == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
```

---

### 4.5 UserId 修改

**文件路径**: `src/main/java/com/cleveronion/blog/domain/user/valueobject/UserId.java`

**字段类型**: `Long`

**修改方式**: 同 CategoryId

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserId {
    private final Long value;

    @JsonCreator
    public UserId(@JsonProperty("value") Long value) {
        if (value == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
```

---

### 4.6 GitHubId 修改

**文件路径**: `src/main/java/com/cleveronion/blog/domain/user/valueobject/GitHubId.java`

**字段类型**: `Long`

**修改方式**: 同 UserId

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubId {
    private final Long value;

    @JsonCreator
    public GitHubId(@JsonProperty("value") Long value) {
        if (value == null) {
            throw new IllegalArgumentException("GitHub ID不能为空");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
```

---

### 4.7 CommentId 修改

**文件路径**: `src/main/java/com/cleveronion/blog/domain/comment/valueobject/CommentId.java`

**字段类型**: `Long`

**修改方式**: 同 CategoryId

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommentId {
    private final Long value;

    @JsonCreator
    public CommentId(@JsonProperty("value") Long value) {
        if (value == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
```

---

### 4.8 ArticleContent 修改（多字段值对象）

**文件路径**: `src/main/java/com/cleveronion/blog/domain/article/valueobject/ArticleContent.java`

**字段**: title, content, summary

**修改方式**:

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArticleContent {
    private final String title;
    private final String content;
    private final String summary;

    @JsonCreator
    public ArticleContent(
        @JsonProperty("title") String title,
        @JsonProperty("content") String content,
        @JsonProperty("summary") String summary
    ) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("内容不能为空");
        }

        this.title = title;
        this.content = content;
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }

    // 其他方法保持不变...
}
```

---

## 5. 实施步骤

### 步骤 1：添加依赖（✅ 已完成）

Jackson 注解已包含在 `jackson-databind` 中，无需额外依赖。

### 步骤 2：修改值对象类

**按优先级顺序修改**：

#### 🔴 第一批（核心 ID，最高优先级）

1. ArticleId
2. CategoryId
3. TagId
4. AuthorId

#### 🔴 第二批（用户和评论）

5. UserId
6. GitHubId
7. CommentId

#### 🔴 第三批（复杂值对象）

8. ArticleContent

### 步骤 3：编译验证

每修改一批后执行：

```bash
mvn compile -DskipTests
```

### 步骤 4：测试验证

启动应用，测试缓存功能：

```bash
# 1. 启动应用
mvn spring-boot:run

# 2. 调用查询接口
curl http://localhost:8080/api/articles

# 3. 查看日志，确认缓存工作
# 4. 再次调用，验证从缓存读取
```

### 步骤 5：清理测试

删除测试用的缓存：

```bash
redis-cli -h localhost -p 16379 FLUSHDB
```

---

## 6. 修改注意事项

### 6.1 导入语句

**需要添加的导入**:

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
```

**位置**: 放在 package 语句之后，类定义之前

### 6.2 注解位置

```java
// ✅ 正确：注解在构造函数上
@JsonCreator
public ArticleId(@JsonProperty("value") String value) { }

// ❌ 错误：注解在类上
@JsonCreator  // 错误！
public class ArticleId { }
```

### 6.3 参数名称

```java
// ✅ 正确：参数名与字段名一致
@JsonProperty("value")  // JSON字段名
String value            // 参数名

// ⚠️ 注意：参数名必须匹配JSON中的字段名
```

### 6.4 多构造函数的情况

如果值对象有多个构造函数：

```java
// 只在用于反序列化的构造函数上添加 @JsonCreator
@JsonCreator
public ArticleId(@JsonProperty("value") String value) {
    this.value = value;
}

// 其他构造函数不需要注解
public ArticleId(Long numericValue) {
    this.value = String.valueOf(numericValue);
}
```

**规则**: 只能有一个构造函数标注 `@JsonCreator`

---

## 7. 验证方法

### 7.1 编译验证

```bash
cd CleverOnion-backend
mvn compile -DskipTests
```

**期望结果**: BUILD SUCCESS

### 7.2 启动验证

```bash
mvn spring-boot:run
```

**检查日志**:

```
✅ 应用启动成功
✅ 无序列化错误
✅ Redis连接成功
✅ 缓存管理器初始化成功
```

### 7.3 功能验证

**测试步骤**:

1. 调用查询接口（首次）
2. 检查 Redis 中有缓存数据
3. 调用查询接口（第二次）
4. 查看日志，确认从缓存读取
5. 调用更新接口
6. 检查 Redis 中缓存已清除

**验证命令**:

```bash
# 查看所有缓存key
redis-cli -h localhost -p 16379 KEYS "cleveronion:cache:*"

# 查看特定缓存
redis-cli -h localhost -p 16379 GET "cleveronion:cache:article:detail::123"

# 检查TTL
redis-cli -h localhost -p 16379 TTL "cleveronion:cache:article:detail::123"
```

---

## 8. 可能的额外修改

### 8.1 检查静态工厂方法

如果值对象使用静态工厂方法：

```java
public class UserId {
    private final Long value;

    // 私有构造函数
    @JsonCreator
    private UserId(@JsonProperty("value") Long value) {
        this.value = value;
    }

    // 静态工厂方法（不需要注解）
    public static UserId of(Long value) {
        return new UserId(value);
    }
}
```

**注意**: `@JsonCreator` 可以用在私有构造函数上

### 8.2 处理继承关系

如果值对象有继承关系：

```java
// 基类
public abstract class BaseId {
    private final Long value;

    @JsonCreator
    protected BaseId(@JsonProperty("value") Long value) {
        this.value = value;
    }
}

// 子类（继承基类的序列化配置）
public class ArticleId extends BaseId {
    public ArticleId(Long value) {
        super(value);  // 调用基类构造函数
    }
}
```

---

## 9. 常见问题

### 9.1 为什么序列化没问题，反序列化有问题？

**序列化**（对象 → JSON）：

- Jackson 通过 **getter 方法** 获取字段值
- 不需要构造函数
- ✅ 所以没问题

**反序列化**（JSON → 对象）：

- Jackson 需要 **构造对象实例**
- 需要知道使用哪个构造函数
- 需要知道 JSON 字段如何映射到构造函数参数
- ❌ 没有注解就不知道

### 9.2 @JsonProperty 的作用

```java
@JsonProperty("value")  // JSON中的字段名
String value            // 参数名

// JSON: {"value": "123"}
//         ↓
// 映射到参数: value = "123"
```

**如果参数名和 JSON 字段名一致**，在某些情况下可以省略 `@JsonProperty`，但：

- ⚠️ 需要编译时保留参数名（`-parameters` flag）
- ⚠️ 不是所有环境都支持
- ✅ **建议明确添加 @JsonProperty，更安全**

### 9.3 会不会影响 DDD 的纯净性？

**影响分析**：

- 技术依赖：值对象依赖 Jackson 注解 ⚠️
- 业务逻辑：完全不受影响 ✅
- 领域规则：完全不受影响 ✅

**结论**：

- Jackson 是序列化的**基础设施关注点**
- 注解是**元数据**，不是业务代码
- 类似于 JPA 的 `@Entity` 注解
- **可以接受的技术耦合** ✅

---

## 10. 实施检查清单

### 修改前检查

- [ ] 备份当前代码（git commit）
- [ ] 确认 Redis 服务运行正常
- [ ] 确认所有值对象文件的路径

### 修改过程

- [ ] 修改 ArticleId
- [ ] 修改 CategoryId
- [ ] 修改 TagId
- [ ] 修改 AuthorId
- [ ] 修改 UserId
- [ ] 修改 GitHubId
- [ ] 修改 CommentId
- [ ] 修改 ArticleContent

### 修改后验证

- [ ] 编译通过 (`mvn compile`)
- [ ] 测试编译通过 (`mvn test-compile`)
- [ ] 应用启动成功
- [ ] 查询接口正常工作
- [ ] Redis 中可以看到缓存数据
- [ ] 缓存读取正常（无序列化错误）
- [ ] 缓存清除正常工作

---

## 11. 验证脚本

创建一个验证脚本来测试缓存功能：

**测试步骤**:

```bash
# 1. 清空Redis缓存
redis-cli -h localhost -p 16379 FLUSHDB

# 2. 调用查询接口（首次，应该查数据库）
curl http://localhost:8080/api/articles/1

# 3. 查看Redis中的缓存
redis-cli -h localhost -p 16379 KEYS "cleveronion:cache:*"

# 4. 再次调用（应该从缓存读取，日志中无SQL）
curl http://localhost:8080/api/articles/1

# 5. 更新文章（应该清除缓存）
curl -X PUT http://localhost:8080/api/articles/1 -H "Content-Type: application/json" -d '{...}'

# 6. 验证缓存已清除
redis-cli -h localhost -p 16379 GET "cleveronion:cache:article:detail::1"
# 应该返回 (nil)
```

---

## 12. 回滚方案

如果修改后出现问题，回滚方案：

### 方案 A：Git 回滚

```bash
git checkout -- src/main/java/com/cleveronion/blog/domain/
```

### 方案 B：临时禁用缓存

在 `application-dev.yml` 中：

```yaml
spring:
  cache:
    type: none # 禁用缓存
```

---

## 13. 预期效果

修改完成后：

### 13.1 序列化（写入 Redis）

```
ArticleAggregate {
  id: ArticleId { value: "123" },
  content: ArticleContent { title: "测试", ... }
}
    ↓
{
  "id": {"value": "123"},
  "content": {"title": "测试", ...}
}
```

### 13.2 反序列化（读取 Redis）

```
{
  "id": {"value": "123"},
  "content": {"title": "测试", ...}
}
    ↓
ArticleAggregate {
  id: ArticleId { value: "123" },    // ✅ 成功构造
  content: ArticleContent { title: "测试", ... }  // ✅ 成功构造
}
```

---

## 14. 总结

### 14.1 问题根源

- DDD 值对象的不可变设计与 Jackson 默认反序列化机制不兼容

### 14.2 解决方案

- 为所有值对象添加 `@JsonCreator` 和 `@JsonProperty` 注解

### 14.3 工作量

- 需要修改：8 个值对象类
- 预计时间：20-30 分钟
- 风险等级：低（改动小，可快速回滚）

### 14.4 实施建议

1. 先修改 1-2 个值对象测试
2. 验证通过后批量修改其余值对象
3. 每修改一批就编译验证一次
4. 最后进行完整的功能测试

---

**下一步**: 开始修改值对象，建议从 `ArticleId` 开始

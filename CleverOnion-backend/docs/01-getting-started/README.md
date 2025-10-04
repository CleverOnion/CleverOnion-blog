# 快速开始

## 📖 文档列表

- [**development-guidelines.md**](./development-guidelines.md) - 开发指南和最佳实践

---

## 🚀 快速上手

### 1. 环境要求

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 2. 启动步骤

```bash
# 1. 克隆项目
git clone https://github.com/CleverOnion/blog.git

# 2. 进入后端目录
cd CleverOnion-backend

# 3. 配置数据库
# 修改 src/main/resources/application-dev.yml

# 4. 启动服务
mvn spring-boot:run
```

### 3. 验证

访问：http://localhost:8080/swagger-ui.html

---

## 📚 下一步

- 阅读 [开发指南](./development-guidelines.md)
- 了解 [架构设计](../02-architecture/)
- 查看 [功能模块](../03-features/)

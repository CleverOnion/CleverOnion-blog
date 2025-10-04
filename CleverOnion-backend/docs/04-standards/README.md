# 开发规范

## 📖 文档列表

- [**backend-development-standards.md**](./backend-development-standards.md) - 后端开发规范
- [**issues-and-improvements.md**](./issues-and-improvements.md) - 问题和改进建议

---

## 📏 规范概述

### 代码规范

1. **命名规范**

   - 类名：大驼峰（UserAggregate）
   - 方法名：小驼峰（findById）
   - 常量：全大写下划线（MAX_SIZE）

2. **包结构规范**

   - 按模块组织（user、article、comment）
   - 分层清晰（domain、application、infrastructure）

3. **注释规范**
   - 所有 public 方法必须有 Javadoc
   - 复杂逻辑必须有行注释
   - 接口必须有 Swagger 注解

### Git 规范

1. **提交信息格式**

   ```
   <type>(<scope>): <subject>

   <body>
   ```

2. **分支管理**
   - main：主分支
   - develop：开发分支
   - feature/\*：功能分支

---

## 📚 详细规范

查看 [后端开发规范](./backend-development-standards.md) 获取完整的规范文档。

---

**最后更新**：2025-10-04

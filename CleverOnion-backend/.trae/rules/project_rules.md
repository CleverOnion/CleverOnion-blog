1. 项目架构与分包规范
采用 DDD 分层与限界上下文相结合的架构模式，以确保高内聚、低耦合。

├── com.cleveronion.blog          // 根包
│   ├── presentation             // 表现层
│   │   ├── api                  // API 接口
│   │   │   ├── controller       // 控制器
│   │   │   └── dto              // 数据传输对象 (DTO)
│   │   └── view                 // 服务端渲染视图
│   │
│   ├── application              // 应用层
│   │   ├── <bounded_context_1>  // 限界上下文1，如：order
│   │   │   ├── command          // 命令对象
│   │   │   ├── eventhandler     // 事件处理器
│   │   │   └── service          // 应用服务
│   │   └── <bounded_context_2>  // 限界上下文2，如：user
│   │       └── ...
│   │
│   ├── domain                   // 领域层
│   │   ├── <bounded_context_1>
│   │   │   ├── aggregate        // 聚合根
│   │   │   ├── entity           // 实体
│   │   │   ├── valueobject      // 值对象
│   │   │   ├── event            // 领域事件
│   │   │   └── repository       // 仓储接口
│   │   └── <bounded_context_2>
│   │       └── ...
│   │
│   ├── infrastructure           // 基础设施层
│   │   ├── <bounded_context_1>
│   │   │   ├── persistence      // 数据持久化
│   │   │   │   ├── po           // 持久化对象 (PO)
│   │   │   │   └── repository   // 仓储实现
│   │   │   └── client           // 外部服务客户端
│   │   └── <bounded_context_2>
│   │       └── ...
│   │
│   └── common                   // 通用/共享模块
│       ├── exception            // 统一异常
│       ├── security             // 安全配置
│       └── util                 // 通用工具类
分层依赖原则： 代码只能依赖下方层级。领域层必须保持纯净，不依赖任何外部框架或技术。

2. 代码风格与命名规范
命名：使用英文命名，避免拼音。类名、接口名使用驼峰式，方法名、变量名使用小驼峰。常量使用全大写加下划线。

注释：所有类、接口和复杂方法必须有 Javadoc 注释，清晰说明其功能、参数、返回值和异常。

日志：使用 Slf4j 接口，避免直接使用 Log4j 或 Logback。日志级别应合理使用（info 用于关键业务流程，debug 用于调试，warn 用于可恢复的错误，error 用于致命错误）。

3. API 接口设计规范 (RESTful)
URL 路径：使用名词复数，表示资源集合。例如 /orders。

HTTP 方法：

GET：用于查询资源。

POST：用于创建资源。

PUT：用于完整更新资源。

PATCH：用于部分更新资源。

DELETE：用于删除资源。

状态码：使用标准的 HTTP 状态码。例如：200 (成功), 201 (创建成功), 400 (请求错误), 404 (资源未找到), 500 (服务器错误)。

数据格式：请求和响应统一使用 JSON 格式。

4. 数据库与持久化规范
POJO：持久化对象（PO）作为数据在数据库和基础设施层之间的桥梁，不应包含任何业务逻辑。

仓储：

领域层：只定义仓储接口（例如 OrderRepository），使用领域模型作为参数和返回值。

基础设施层：实现仓储接口（例如 OrderRepositoryImpl），负责将领域模型和 PO 进行转换，并与数据库交互。

事务：事务管理应在应用服务层，使用 @Transactional 注解。

5. 异常处理规范
统一处理：使用 Spring 的 @ControllerAdvice 和 @ExceptionHandler 进行全局异常处理。

自定义异常：定义业务相关的自定义异常，例如 OrderNotFoundException，并使用统一异常处理器将其转换为合适的 HTTP 状态码和错误信息。

避免捕获通用异常：除非有必要，否则不要捕获 Exception。应精确地捕获并处理特定的异常。

6. 测试规范
单元测试：对领域层的核心逻辑进行单元测试，不依赖外部服务（如数据库）。

集成测试：对应用服务和仓储实现进行集成测试，确保整个业务流程正确。

API 测试：使用 MockMvc 或 TestRestTemplate 对 RESTful API 进行端到端测试。

7. DDD 核心规范
领域模型纯粹：领域层的代码中，严禁出现 Spring、JPA、HTTP 等技术框架的引用。

聚合根：每个聚合都有一个聚合根作为唯一入口，所有对聚合内部实体和值对象的操作都必须通过聚合根。

值对象：值对象必须是不可变的。其相等性判断基于所有属性值。

领域事件：用于解耦业务流程。应用服务发布事件，事件处理器监听并处理事件。

8. 分层依赖原则
   这是最核心的规则。严格遵循 DDD 的分层原则，代码只能依赖下方的层级。

presentation 层：只依赖 application 层。它不能直接调用 domain 或 infrastructure 层的代码。

application 层：依赖 domain 和 infrastructure 层。它负责协调业务流程，但不包含核心业务逻辑。它不能被 domain 层依赖。

domain 层：不依赖任何外部层。这是领域模型纯粹性的关键。领域层的代码只包含业务规则和模型，不能有任何关于数据库、HTTP、Spring 等技术实现的引用。

infrastructure 层：依赖 domain 层。它实现 domain 层定义的仓储接口、领域事件发布器等。

9. 代码职责与命名规范
   presentation

controller：只处理 HTTP 请求和响应。它接收 DTO，调用 application 层的服务，并将结果封装成 DTO 返回。不包含任何业务逻辑。

dto：命名应清晰地表达其用途，例如 PlaceOrderRequest、OrderResponse。DTO 和 领域模型（实体、值对象）必须严格分离，避免在 API 接口中直接暴露领域模型。

application

service：命名应体现其业务流程，例如 OrderApplicationService。方法名应使用动词，如 placeOrder(PlaceOrderCommand command)。

command 与 query：命名应体现意图，例如 PlaceOrderCommand、GetOrderQuery。它们是不可变的，用于封装请求参数。

eventhandler：命名应体现其监听的事件和执行的动作，例如 OrderPlacedEventHandler。方法应使用 @EventListener 注解。

domain

aggregate：只包含聚合根实体。聚合根是整个聚合的唯一入口，其方法应反映业务行为，例如 order.pay()。

entity：命名应体现其业务概念，例如 OrderItem。实体类拥有唯一的 ID，其方法应包含自身状态的业务逻辑。

valueobject：命名应体现其值属性，例如 Address、Money。值对象必须是不可变的。其相等性（equals() 和 hashCode()）基于所有属性。

repository：接口命名应体现其存取聚合的能力，例如 OrderRepository。方法名应使用领域语言，例如 findById()、save()。

infrastructure

repository 实现：命名应以 Impl 结尾，例如 OrderRepositoryImpl。它负责将 domain 层的模型映射到数据库的 PO（Persistence Object），反之亦然。

po：命名应与数据库表对应，例如 OrderPo。PO 是纯粹的数据结构，不应包含任何业务逻辑。

client：命名应体现其外部依赖，例如 InventoryServiceClient。

10. 跨上下文通信规范
   禁止直接调用：application 层禁止直接调用另一个限界上下文的 application 层或 domain 层。

使用领域事件：跨上下文的协作应通过领域事件来实现。一个上下文发布事件，另一个上下文监听并处理。这确保了最大的解耦。

使用防腐层：在需要与外部遗留系统或第三方服务同步集成时，应在 infrastructure 层实现一个 防腐层（Anti-Corruption Layer），将外部复杂模型转换为本领域模型可以理解的格式。

11. 依赖注入与事务管理
   依赖注入：只在 application 层和 infrastructure 层使用 Spring 的依赖注入。domain 层应保持纯净，不使用任何框架注解（如 @Service、@Component）。

事务管理：事务应在 application 层的服务方法上进行管理，例如使用 @Transactional 注解。领域事件通常在事务提交后发布，以确保最终一致性。

12. 安全规范
认证与授权：
使用 Sa-token 进行统一的认证和授权管理。
API 接口应明确声明所需的权限，例如使用 @SaCheckPermission 注解。
密码存储必须使用强哈希算法（如 BCrypt）。

输入验证：
所有来自外部的输入（如 API 请求的 DTO）都必须在 application 层进行严格的合法性校验。
使用 Bean Validation (JSR 380) 注解（如 @NotNull, @Size）进行声明式校验。

防止常见漏洞：
SQL 注入：使用 JPA 或 Mybatis 等 ORM 框架，避免拼接 SQL。
跨站脚本（XSS）：对输出到视图的數據进行转义。
跨站请求伪造（CSRF）：Sa-token 默认提供 CSRF 防护，请确保已开启并正确配置。

13. 配置管理规范
配置分离：
将配置与代码分离。使用 application.yml 或 application.properties 文件进行配置。
敏感信息（如数据库密码、API 密钥）不应硬编码在代码或配置文件。

多环境配置：
使用 Spring Profiles（如 application-dev.yml, application-prod.yml）来管理不同环境（开发、测试、生产）的配置。
通过激活不同的 Profile 来加载相应的配置。

14. 环境管理规范
开发环境 (dev): 用于日常开发，配置可以灵活调整。
测试环境 (test): 用于部署测试版本，配置应接近生产环境。
生产环境 (prod): 最终部署环境，配置必须严格管理和审核。

15. 文档与版本控制
接口文档：
使用 OpenAPI 3 (Swagger) 自动生成和维护 API 文档。
确保文档与代码实现同步更新。

版本控制 (Git)：
分支模型：遵循 Git Flow 或类似的稳定分支模型。
  - `main`/`master`: 生产环境分支，保持稳定。
  - `develop`: 开发主分支，集成新功能。
  - `feature/*`: 功能开发分支，从 `develop` 创建。
  - `release/*`: 发布分支，用于准备发布版本。
  - `hotfix/*`: 紧急修复分支，从 `main`/`master` 创建。
提交信息：提交信息应清晰、有意义，遵循一定的格式（如 Conventional Commits）。
Code Review：所有代码变更必须经过 Code Review 才能合并到 `develop` 或 `main` 分支。
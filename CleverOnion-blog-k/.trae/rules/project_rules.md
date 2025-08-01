src/
│
├── 1-domain/                       # 1️⃣ 领域层：纯业务、零依赖
│   ├── aggregate/
│   ├── entity/
│   ├── value-object/
│   ├── domain-service/
│   ├── repository/                 # 端口：仓储接口
│   └── event/
│
├── 2-application/                  # 2️⃣ 应用层：用例编排
│   ├── use-case/
│   ├── command/
│   ├── query/
│   ├── port/                       # 应用层端口（in/out）
│   └── service/                    # 应用服务
│
├── 3-infrastructure/               # 3️⃣ 基础设施层：我依赖别人
│   ├── persistence/                #   如 DB、Redis、搜索引擎
│   ├── messaging/                  #   如 Kafka、RabbitMQ
│   ├── identity/                   #   如 OAuth、JWT
│   └── third-party/                #   如支付网关、地图 API
│
├── 4-trigger/                      # 4️⃣ 触发器层：别人依赖我
│   ├── http/                       #   REST / GraphQL / gRPC
│   ├── cli/
│   ├── event-subscriber/           #   消费 MQ
│   ├── scheduler/                  #   定时任务
│   └── webhook/
│
├── 5-config/                       # 5️⃣ 配置层：集中管理
│   ├── database.config.ts
│   ├── logger.config.ts
│   ├── security.config.ts
│   └── ioc.config.ts               # 依赖注入配置
│
└── 6-bootstrap/                    # 6️⃣ 启动层：跑起来
    ├── server.ts                   #   HTTP Server、CLI Entry、Worker Entry
    ├── worker.ts
    └── main.ts
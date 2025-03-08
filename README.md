# 后端数据同步系统

## **项目概述**
本项目是一个基于 Spring Boot 和 Spark 的后端数据同步系统，用于处理用户活动数据，并将数据存储到 MySQL 和 Redis 中。系统通过 Kafka 实现异步消息处理，支持用户活动的记录、查询、更新和删除操作。

## **项目结构**
backend-data-sync/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/example/demo/
│   │   │   │   ├── controller/          # 控制器层
│   │   │   │   ├── model/              # 数据模型
│   │   │   │   ├── repository/        # 数据访问层
│   │   │   │   ├── service/            # 业务逻辑层
│   │   │   │   └── config/             # 配置类
│   │   ├── resources/
│   │   │   ├── application.properties  # 配置文件
│   │   │   └── static/                 # 静态资源
│   ├── test/
│   │   ├── java/
│   │   │   ├── com/example/demo/
│   │   │   │   └── controller/         # 控制器测试
│   │   └── resources/
├── target/                             # 构建输出
├── pom.xml                             # Maven 配置文件
└── README.md                           # 项目说明

## **接口文档**

### 用户活动数据接口

#### 1. 获取用户活动数据
- **URL**: `/api/user-activities`
- **Method**: GET
- **参数**: 
  - `openid` (可选): 用户唯一标识
  - `subscribe` (可选): 订阅状态
- **响应**: 
  ```json
  {
    "id": 1,
    "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M",
    "language": "zh_CN",
    "subscribe": true,
    "subscribeTime": "2023-01-01T00:00:00Z",
    "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL2M",
    "processed": false,
    "createTime": "2023-01-01T00:00:00",
    "updateTime": "2023-01-01T00:00:00"
  }
  ```

#### 2. 创建用户活动记录
- **URL**: `/api/user-activities`
- **Method**: POST
- **请求体**:
  ```json
  {
    "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M",
    "language": "zh_CN",
    "subscribe": true,
    "subscribeTime": "1672531200",
    "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL2M",
    "processed": false,
    "pday": "20230101"
  }
  ```
- **响应**: 201 Created

#### 3. 更新用户活动记录
- **URL**: `/api/user-activities/{id}`
- **Method**: PUT
- **请求体**:
  ```json
  {
    "subscribe": false,
    "processed": true
  }
  ```
- **响应**: 200 OK

#### 4. 删除用户活动记录
- **URL**: `/api/user-activities/{id}`
- **Method**: DELETE
- **响应**: 204 No Content

### 数据同步接口

#### 1. 触发数据同步
- **URL**: `/api/sync`
- **Method**: POST
- **响应**: 
  ```json
  {
    "status": "success",
    "message": "Data sync started"
  }
  ```

#### 2. 获取同步状态
- **URL**: `/api/sync/status`
- **Method**: GET
- **响应**:
  ```json
  {
    "status": "running",
    "progress": 75,
    "lastSyncTime": "2023-01-01T12:00:00Z"
  }
  ```

  ## 系统架构图

  ```mermaid
  graph TD
    A[微信用户] -->|订阅/取消订阅| B(微信服务器)
    B -->|推送消息| C[消息队列]
    C --> D[数据同步服务]
    D --> E{数据校验}
    E -->|有效数据| F[MySQL数据库]
    E -->|无效数据| G[错误日志]
    F --> H[ETL处理]
    H --> I[数据仓库]
    I --> J[数据分析]
    J --> K[BI报表]
  ```

  ### 组件说明
  1. **微信用户**: 系统终端用户，通过微信公众号进行交互
  2. **微信服务器**: 接收用户操作并推送消息
  3. **消息队列**: 异步处理用户操作消息
  4. **数据同步服务**: 核心业务逻辑，处理数据同步
  5. **MySQL数据库**: 存储用户活动记录
  6. **ETL处理**: 数据清洗转换
  7. **数据仓库**: 存储历史数据


测试文档： https://mv21kbvltn.feishu.cn/docx/YbNXdQaMaobPxYxkAUmcjTUInoh?from=from_copylink
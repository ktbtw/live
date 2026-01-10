# 直播辩论系统

一个完整的直播辩论平台，支持实时投票、评委管理、辩论流程控制等功能。前端（uni-app）+ 网关 + 后端（Spring Boot）均可本地运行，支持模拟业务数据。

## 📌 基本信息

- **项目名称**: 直播辩论系统（Live Debate）

## 🚀 演示地址

- **前端访问地址**: `http://193.112.85.94:8080/`
- **访问管理后台**: `http://193.112.85.94:8080/admin`


## 🧱 技术栈说明

### 后端框架
- **Java 17** + **Spring Boot 3.2.5**
- Spring WebSocket（实时通信）
- SpringDoc OpenAPI（API 文档）
- Lombok（简化代码）

### Mock 数据方案
- 使用 `ConcurrentHashMap` 内存存储
- 系统启动时自动初始化测试数据（直播流、用户、评委、辩论流程、AI内容）
- 无需数据库依赖，开箱即用

### 网关层
- Node.js + Express
- WebSocket 透传
- API 路由反向代理
- 静态页面服务（/admin、/static）

### 前端
- uni-app（Vue）
- 支持小程序和 H5

### 部署方式
- 云服务器直接部署（或任意云平台）
- PM2 进程管理（Node.js 服务）
- Java -jar 直接运行（Spring Boot）



## 🔗 项目结构与接口说明

```
├── Live/                   # 前端项目（uni-app）
│   ├── pages/             # 页面
│   ├── components/        # 组件
│   ├── admin/             # 后台管理页面
│   └── server.js          # 原 Node.js 服务（已被后端替代）
│
├── live-gateway/          # 网关服务
│   ├── gateway.js         # 网关主文件
│   └── config/            # 配置文件
│
├── live-backend/          # Spring Boot 后端服务
│   ├── src/main/java/com/live/
│   │   ├── controller/    # 控制器（API 接口）
│   │   ├── service/       # 服务层（Mock 数据）
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   ├── config/        # 配置（CORS、WebSocket）
│   │   └── websocket/     # WebSocket 处理
│   └── pom.xml            # Maven 配置
│
└── README.md              # 本文件
```

## 📡 主要接口说明

| 功能 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 健康检查 | GET | `/health` | 服务健康状态 |
| 获取直播流列表 | GET | `/api/admin/streams` | 返回所有直播流 |
| 获取票数 | GET | `/api/admin/votes` | 获取当前投票数据 |
| 更新票数 | POST | `/api/admin/live/update-votes` | 管理员更新票数 |
| 用户投票 | POST | `/api/user-vote` | 用户提交投票 |
| 获取仪表盘 | GET | `/api/admin/dashboard` | 获取统计数据 |
| 获取直播状态 | GET | `/api/admin/live/status` | 获取直播与AI状态 |
| 控制直播 | POST | `/api/live/control` | 开始/停止直播 |
| 获取AI内容 | GET | `/api/v1/ai-content` | 小程序AI内容 |
| AI评论 | POST | `/api/comment` | 添加评论 |
| AI点赞 | POST | `/api/like` | 点赞内容/评论 |
| 获取辩题 | GET | `/api/v1/debate-topic` | 获取辩题信息 |
| 获取AI列表 | GET | `/api/v1/admin/ai-content/list` | 后台AI内容列表 |
| 获取推流地址 | GET | `/api/admin/rtmp/urls` | 返回推流/播放地址 |
| 获取评委配置 | GET | `/api/admin/judges` | 获取评委列表 |
| 保存评委配置 | POST | `/api/admin/judges` | 保存评委设置 |
| 获取辩论流程 | GET | `/api/admin/debate-flow` | 获取流程配置 |
| 保存辩论流程 | POST | `/api/admin/debate-flow` | 保存流程配置 |
| 流程控制 | POST | `/api/admin/debate-flow/control` | 发送控制命令（start/pause/next等） |
| 获取用户列表 | GET | `/api/admin/users` | 获取用户列表 |

### WebSocket

- **连接地址**: `ws://[服务器地址]:8000/ws`
- **事件类型**:
  - `liveStatus` - 直播状态更新
  - `votes-updated` - 票数更新
  - `judges-updated` - 评委配置更新
  - `debate-flow-updated` - 流程配置更新
  - `debate-flow-control` - 流程控制命令
  - `aiStatus` - AI 状态更新
  - `newAIContent` - 新增 AI 内容
  - `ai-content-updated` - AI 内容更新
  - `aiContentDeleted` - AI 内容删除
  - `viewers-updated` - 观看人数更新

### 响应格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1704700800000
}
```

> 说明：当前后端以 `success/message/data` 为统一结构，已满足前端使用。如需与示例 `code/message/data` 对齐，可在后端统一追加 `code` 字段。

## 🧠 项目开发过程笔记

### 实现思路

1. **分析前端需求**：通过阅读前端代码和网关配置，梳理出所需的 API 接口
2. **设计数据模型**：定义直播流、投票、评委、辩论流程等核心实体
3. **Mock 数据服务**：使用内存存储模拟数据库，系统启动时初始化测试数据
4. **WebSocket 实时通信**：实现数据变更的实时推送，确保多端数据同步
5. **统一响应格式**：封装 ApiResponse，保证接口返回格式一致

### 遇到的问题与解决方案

1. **前端请求格式兼容**
   - 问题：小程序发送的请求体被包装在 `{request: {...}}` 中
   - 解决：在接口中兼容处理，同时支持直接格式和包装格式

2. **跨域问题**
   - 问题：前端访问后端 API 报 CORS 错误
   - 解决：配置 CorsConfig，允许所有来源访问

3. **WebSocket 连接管理**
   - 问题：客户端断开后连接池未清理
   - 解决：在 onClose 和 onError 事件中移除失效连接

4. **多直播流数据隔离**
   - 问题：不同直播间的数据需要独立管理
   - 解决：所有数据以 streamId 为 key 存储在 Map 中

### 本地运行

1. **启动后端**
   ```bash
   cd live-backend
   mvn spring-boot:run
   ```
2. **启动网关**
   ```bash
   cd live-gateway
   npm install
   npm start
   ```
3. **访问管理后台**
   - `http://localhost:8080/admin`
4. **前端 H5（临时）**
   - 用 HBuilderX 运行到 H5 或构建后用静态服务器启动

### 本地联调经验

1. 先启动后端服务（端口 8000）
2. 修改网关配置，将 API 请求代理到后端
3. 启动网关服务（端口 8080）
4. 前端连接网关地址进行测试
5. 使用 Swagger UI（`/swagger-ui.html`）测试接口

### 部署步骤

1. **后端部署**
   ```bash
   cd live-backend
   mvn clean package -DskipTests
   java -jar target/live-debate-backend-1.0.0.jar
   ```

2. **网关部署**
   ```bash
   cd live-gateway
   npm install
   npm start
   ```

3. **前端部署**
   - H5 版本：构建后部署静态文件
   - 小程序：通过微信开发者工具上传

### 踩坑记录

- Spring Boot 3.x 需要 Java 17+，注意 JDK 版本
- WebSocket 配置需要同时配置 `WebSocketConfig` 和 `WebConfig`
- 前端 uni-app 的 WebSocket 连接需要完整的 ws:// 地址

## 🔮 扩展性思考

1. **数据库层**：将 MockDataService 替换为 JPA Repository，接入 MySQL/PostgreSQL
2. **缓存层**：使用 Redis 缓存热点数据（如实时票数）
3. **消息队列**：高并发投票场景可引入 RabbitMQ/Kafka
4. **认证授权**：集成 Spring Security + JWT
5. **分布式部署**：WebSocket 需要引入 Redis Pub/Sub 实现多节点消息同步

## 🧍 个人介绍

- **技术背景**: 本科在读（2022.09-2026.06），主修数据结构、计算机网络、操作系统、数据库等
- **主要语言**: Java、JavaScript、SQL、Python、C++
- **擅长方向**: Spring/Spring Boot 后端开发、前后端分离项目落地、微服务与缓存/限流实践
- **学习目标**: 深入分布式与性能优化，提升工程化与全栈协作能力

---

## 📄 相关链接

- [前端项目仓库](https://github.com/xuelinc91-creator/Live)
- [网关项目仓库](https://github.com/xuelinc91-creator/live-gateway)

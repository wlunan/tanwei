# 探味 · TanWei

> 本地生活服务平台 —— 基于 Spring Boot + Redis 的高并发本地生活服务系统

## 项目简介

**探味（TanWei）** 是一款面向本地生活服务场景的后端项目，聚焦「吃、逛、探店」三大核心诉求，提供商户浏览与附近搜索、优惠券发放与秒杀、探店笔记与好友关注、点赞、连续签到等完整能力。项目以 **Redis 深度应用**为主线，在高并发秒杀、缓存一致性、分布式锁、Feed 流推送等典型互联网场景中落地了可复用的工程化解决方案。

项目定位为一个具备生产级稳定性考量的 **RESTful API 后端服务**，技术选型与实现均面向高并发、高性能目标。

## 技术栈

| 分类 | 技术 | 说明 |
| --- | --- | --- |
| 基础框架 | Spring Boot 2.3 | 应用框架 |
| ORM | MyBatis-Plus 3.4 | 数据访问层 |
| 缓存 / 存储 | Redis 6 + Spring Data Redis | 缓存、分布式锁、消息队列等 |
| Redis 客户端 | Lettuce、Redisson | 连接池与分布式锁 |
| 数据库 | MySQL 5.7 | 业务数据存储 |
| 工具 | Hutool、Lombok | 工具类与样板代码消除 |
| 构建 | Maven | 依赖与构建管理 |
| 语言 | Java 8 | 开发语言 |

## 功能特性

- **用户模块**：手机号验证码登录、密码登录、登录态续期、基于 BitMap 的连续签到统计
- **商户模块**：商户分页查询、类型筛选、基于 GEO 的附近商户搜索与距离排序
- **优惠券模块**：普通优惠券、秒杀优惠券的发放与核销
- **秒杀模块**：Lua 脚本原子化扣减库存、Stream 消息队列异步下单、一人一单防超卖
- **博客（探店笔记）模块**：笔记发布、点赞、评论，基于 Sorted Set 的推模式 Feed 流
- **社交模块**：关注 / 取关、共同关注、粉丝列表
- **系统能力**：全局唯一 ID（雪花算法）、图片上传、统一异常处理与统一返回体

## 核心亮点

项目围绕高并发与缓存场景，沉淀了以下可复用的技术方案：

### 1. 缓存三大问题的系统化解决
- **缓存穿透**：空值缓存 + 短 TTL 兜底
- **缓存击穿**：互斥锁重建 + 逻辑过期两种方案（`CacheClient` 统一封装）
- **缓存雪崩**：随机 TTL 分散过期时间

### 2. 秒杀场景的极致优化
- **Lua 脚本原子操作**：库存判断、一人一单校验、扣减在 Redis 侧一次性完成，减少网络往返
- **异步下单**：秒杀成功后将下单请求写入 Redis Stream，由消费者异步落库，削峰填谷
- **全局唯一 ID**：基于时间戳 + 序列号的雪花算法 ID 生成器（`RedisIdWorker`）

### 3. 分布式锁
- 基于 Redis `SET NX EX` 的简易分布式锁 + Lua 脚本保证释放原子性（`ILock` / `SimpleRedisLock`）
- 引入 Redisson 实现可重入锁、看门狗续期等高级能力

### 4. Feed 流推送
- 关注笔记采用「推模式」，通过 Sorted Set 时间线向粉丝推送，滚动分页查询

### 5. 基于 GEO / BitMap / HyperLogLog 的场景落地
- 附近商户：Redis GEO 数据结构实现 LBS 距离检索
- 连续签到：BitMap 位运算统计
- 活跃用户 UV：HyperLogLog 近似去重统计（百万级数据测试）

## 项目结构

```
src/main/java/com/tanwei/
├── TanWeiApplication.java      # 启动入口
├── controller/                 # 控制层
├── service/                    # 业务接口
│   └── impl/                   # 业务实现
├── mapper/                     # 数据访问层
├── entity/                     # 数据库实体
├── dto/                        # 数据传输对象
├── config/                     # 配置类（Redis、MyBatis、MVC、异常）
└── utils/                      # 工具类（缓存客户端、分布式锁、ID 生成器等）

src/main/resources/
├── application.yaml            # 应用配置
├── mapper/                     # MyBatis XML 映射
├── db/tanwei.sql               # 数据库初始化脚本
├── seckill.lua                 # 秒杀 Lua 脚本
└── unlock.lua                  # 分布式锁释放 Lua 脚本
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis 6.x（需支持 GEO、Stream 等数据结构）

### 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE tanwei DEFAULT CHARACTER SET utf8mb4;
```

2. 导入初始化脚本：

```bash
mysql -u root -p tanwei < src/main/resources/db/tanwei.sql
```

### 配置

修改 `src/main/resources/application.yaml` 中的数据库与 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/tanwei?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
  redis:
    host: 127.0.0.1
    port: 6379
    password: your_password
```

### 运行

```bash
mvn spring-boot:run
```

服务默认启动于 `8081` 端口，可通过 `http://localhost:8081` 访问接口。

## 说明

- 本项目为后端 API 服务，接口返回统一的 JSON 结构（`Result` 封装）。
- 测试代码位于 `src/test/java/com/tanwei/`，包含缓存、分布式锁、GEO 加载、HyperLogLog 等单元测试。

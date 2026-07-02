# 食光机 - 餐饮外卖全栈系统

## 项目简介

基于 Spring Boot 的餐饮外卖平台，包含 C 端用户点餐下单和 B 端商家管理后台，支持菜品管理、套餐管理、购物车、订单全流程、支付宝沙箱支付、实时 WebSocket 推送、数据统计报表等功能。

## 技术栈

| 层面 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.3 + Spring MVC |
| 持久层 | MyBatis + MySQL + Druid 连接池 |
| 缓存 | Redis + Redisson（延迟队列） |
| 实时通信 | WebSocket |
| 支付 | 支付宝沙箱（RSA2 签名） |
| 安全 | JWT 双端鉴权 + BCrypt 密码加密 |
| 接口文档 | Knife4j (Swagger) |
| 前端 | 微信小程序 + Vue 管理后台 |
| 文件存储 | 阿里云 OSS |

## 核心技术亮点

### 1. Redisson 延迟队列实现订单超时自动取消
- 用户下单后投递到 RDelayedQueue，15 分钟后自动消费检查
- 相比定时任务轮询，秒级精确超时，减少数据库无效扫描
- 守护线程 + AtomicBoolean 实现优雅停机
- 保留定时任务作为兜底方案

### 2. 菜品库存管理
- 下单事务内扣减库存，库存不足回滚事务
- 库存归零自动停售，形成业务闭环
- 兼容 stock=NULL 表示不限库存

### 3. 支付宝沙箱支付
- AlipayTradePagePayRequest 表单生成 + 异步回调验签
- RSA2 签名验证确保数据安全
- 回调幂等性处理

### 4. 安全加固
- 密码存储从 MD5 升级为 BCrypt
- JWT 拦截器日志脱敏
- 修复 SetmealMapper UPDATE 缺失 WHERE 条件导致全表更新的灾难级 Bug
- 修复报表统计模块忽略 status 参数导致完成率恒为 100% 的逻辑 Bug

## 项目结构

```
foodtime/
├── foodtime-common/          # 公共模块（常量、工具类、异常）
├── foodtime-pojo/            # 实体类、DTO、VO
├── foodtime-server/          # 主服务模块
│   ├── src/main/java/com/foodtime/
│   │   ├── config/           # 配置类（Redisson、WebMvc、Springfox 修复）
│   │   ├── controller/       # 控制器（admin + user）
│   │   ├── queue/            # 延迟队列（OrderDelayQueue）
│   │   ├── service/          # 业务逻辑
│   │   ├── mapper/           # MyBatis Mapper
│   │   ├── interceptor/      # JWT 拦截器
│   │   ├── task/             # 定时任务
│   │   └── websocket/        # WebSocket 服务
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml.template  # 配置模板（脱敏）
│       └── mapper/            # MyBatis XML
└── pom.xml
```

## 快速开始

### 前置条件
- JDK 8+
- MySQL 8+
- Redis
- Maven 3.6+

### 配置

1. 创建数据库并导入表结构
2. 复制 `application-dev.yml.template` 为 `application-dev.yml`，填写真实配置
3. 编译运行

```bash
mvn clean package -DskipTests
java -jar foodtime-server/target/foodtime-server-1.0-SNAPSHOT.jar
```

4. 小程序端修改 `common/vendor.js` 中的 `baseUrl` 为后端地址

## 接口文档

启动后访问：http://localhost:8080/doc.html

# AdFlux-Backend

AdFlux 是一个轻量级、智能化的广告投放与数据分析后台系统。本项目基于 Spring Boot 架构，核心亮点在于其具备**实时反馈闭环的搜索/推荐算法**以及**全链路结构化调试日志系统**。

## 🚀 核心功能

### 1. 智能广告推荐引擎
系统通过多维度过滤与加权算法，为用户匹配最合适的广告：
- **多级过滤**：支持网站接入验证、广告时效性检测（近30天）、审核状态及启用状态校验。
- **动态预算控制**：实时计算广告商周预算消耗（支持展示计费 CPM 与点击计费 CPC 混合模型），确保投放不超支。
- **个性化加权算法**：
    - **浏览时长分析**：基于用户在不同内容分类下的停留时间计算基础兴趣。
    - **反馈闭环（CTR修正）**：结合历史点击率差异进行权重补偿。
    - **时间衰减模型**：利用指数衰减函数，确保近期行为对推荐结果有更高权重。
- **归一化随机选择**：采用加权随机算法平衡“个性化精准度”与“内容探索性”。

### 2. 全链路 Debug 日志系统 (AdDebug)
为了解决推荐算法“黑盒”问题，系统内置了透明化调试机制：
- **低侵入记录**：利用 AOP 切面技术，在不改动核心逻辑的前提下截获计算中间过程。
- **结构化数据**：不同于传统文本日志，系统记录完整的逻辑快照（JSON 格式）。
- **实时推送**：通过 WebSocket 将推荐逻辑实时推送到前端，方便运营与开发人员进行算法回溯。

### 3. 安全与权限管理
- **基于 JWT 的无状态鉴权**：保护接口安全，支持 Token 自动解析与上下文绑定。
- **细粒度角色控制**：通过自定义注解 `@RequireRole` 实现“管理员/广告商/网站主”的三级权限隔离。

## 🛠 技术栈

- **核心框架**：Spring Boot 2.7.6
- **ORM 框架**：MyBatis-Plus (支持高效的 Lambda 链式查询)
- **数据库**：MySQL8.0
- **实时通信**：Spring WebSocket
- **切面工具**：Spring AOP + AspectJ
- **工具类库**：Hutool, Jackson, Lombok, JWT

## 📂 项目结构

```text
src/main/java/com/usst/adfluxbackend/
├── annotation/   # 自定义权限与业务注解
├── aop/          # AdDebug 切面逻辑
├── common/       # 公共响应封装、计费计算器、Debug 上下文
├── config/       # 配置类 (跨域、拦截器、MyBatis-Plus)
├── controller/   # API 入口 (广告追踪、用户管理、广告管理)
├── interceptor/  # JWT 鉴权拦截器
├── mapper/       # 数据库映射接口
├── model/        # 实体类 (Entity, DTO, VO)
└── service/      # 核心业务逻辑实现
```

## ⚙️ 快速开始

### 1. 环境准备
- JDK 17+ (推荐)
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置
数据库在docs仓库中
1. 执行 SQL 文件初始化表结构（详见 [Database Design](https://github.com/USST-JavaWeb-251-AdFlux/docs/blob/main/Database-Design.md)）。
2. 在 `src/main/resources/application-local.yml` (或 `application-dev.yml`) 中修改数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/adflux?useUnicode=true&characterEncoding=utf-8
       username: root
       password: yourpassword
   ```

### 3. 启动项目
```bash
mvn clean install
mvn spring-boot:run
```


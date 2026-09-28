# jira-backend

Jira 项目的后端服务。由原 `json-server` Mock 重构而来，使用 **Java + Spring Boot + MyBatis + MySQL**。

> 前端项目在仓库的 `frontend/` 目录，数据库脚本在 `/backend/db/` 目录。

---

## 一、技术栈与版本

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | **21** | 编译与运行 |
| Spring Boot | **3.2.5** | 基础框架 |
| Spring Framework | 6.1.6 | 由 Spring Boot 托管 |
| 内嵌 Tomcat | 10.1.20 | 由 Spring Boot 托管 |
| MyBatis Spring Boot Starter | **3.0.3** | 数据访问框架（手工指定版本） |
| MySQL Connector/J | 8.3.0 | 由 Spring Boot 托管 |
| HikariCP | 5.0.1 | 连接池，由 Spring Boot 托管 |
| Lombok | 1.18.32 | 由 Spring Boot 托管 |
| spring-security-crypto | 6.2.4 | 只用 BCrypt 密码哈希，未引入整套 Spring Security |
| JJWT | **0.12.6** | JWT 签发与校验（手工指定版本） |
| MySQL Server | 8.0 | 数据库，字符集 `utf8mb4` |

> 「由 Spring Boot 托管」= 版本由 `spring-boot-starter-parent` 的 BOM 统一决定，`pom.xml` 里不写 `<version>`，避免版本冲突。
> 实际解析结果可用 `mvn dependency:tree` 或 IDEA 的 Maven 面板查看。

### 构建工具

**Maven**。项目未附带 `mvnw`，两种使用方式：

- **用 IDEA 打开项目**：IDEA 自带 Maven，无需单独安装（推荐）
- **用命令行**：需要先自行安装 Maven 并配置 `MAVEN_HOME` / `PATH`

---

## 二、环境要求

| 项 | 要求 |
| --- | --- |
| JDK | 21（本机路径示例：`D:\develop\Java\jdk-21`） |
| MySQL | 8.0，服务需已启动，监听 `3306` |
| 数据库 | 库名 `jira`，字符集 `utf8mb4` |
| IDE | IntelliJ IDEA（Community 版即可；建议装 `MyBatisX` 插件） |

---

## 三、快速开始

### 第 1 步：初始化数据库

数据库脚本在仓库根目录的 `db/` 下：

| 文件 | 作用 |
| --- | --- |
| `db/schema.sql` | 建库 `jira` + 7 张表（含外键与级联删除） |
| `db/data.sql` | 种子数据：用户、项目、项目成员、看板、任务组、任务类型、任务 |

**执行顺序必须是：先 `schema.sql`，后 `data.sql`**（`schema.sql` 开头有 `DROP TABLE`，顺序颠倒会清空数据）。

执行方式（二选一）：

**方式 A：MySQL Workbench**

1. 连接 `localhost:3306`
2. `File → Open SQL Script...` 选 `db/schema.sql` → 点 ⚡ 执行
3. 同样方式打开 `db/data.sql` → 点 ⚡ 执行

> 注意：两个文件要分别在**各自的标签页**里执行，不要在同一个标签页里换文件。
>
> 如果 `data.sql` 报 `Error 1175 (safe update mode)`：Workbench 默认开启了 Safe Updates。关闭方式：`Edit → Preferences → SQL Editor → 取消勾选 "Safe Updates"`，然后重新连接。

**方式 B：命令行**

```bash
mysql -u root -p --default-character-set=utf8mb4
```

在 `mysql>` 提示符下依次执行：

```sql
source <仓库路径>/db/schema.sql
source <仓库路径>/db/data.sql
```

**验证：**

```sql
SELECT COUNT(*) FROM jira.task;   -- 期望 6
```

### 第 2 步：配置数据库密码

`application.yml` 里读的是环境变量，**仓库中不含任何明文密码**：

```yaml
username: ${DB_USERNAME:root}
password: ${DB_PASSWORD:}
```

**IDEA 里配置（推荐）：**

`Run → Edit Configurations...` → 选中 `JiraApplication` → `Environment variables` 一栏填：

```
DB_PASSWORD=你的MySQL root密码
```

**或在 `application.yml` 里直接填写：**

```yaml
password: 你的密码
```

> 直接写进配置文件的话，改完注意不要提交（`.gitignore` 已排除 `application-local.yml`，更推荐用这个文件覆盖）。

### 第 3 步：启动

**方式一：IDEA（推荐）**

打开 `JiraApplication.java`，点类名左侧的绿色三角，或按 `Shift + F10`。

**方式二：命令行（需已安装 Maven）**

```bash
# 开发模式运行
mvn spring-boot:run

# 或先打包再运行
mvn clean package -DskipTests
java -jar target/jira-backend-0.0.1-SNAPSHOT.jar
```

启动成功的标志（控制台最后几行）：

```
Tomcat started on port 8080 (http)
Started JiraApplication in x.xxx seconds
```

### 第 4 步：验证

浏览器访问 **http://localhost:8080/health**

```json
{"code":0,"message":"成功","data":{"status":"UP","userCount":4}}
```

`userCount: 4` 表示 **Web 层 + MyBatis + MySQL 三段链路全部打通**。

---

## 四、配置说明

`src/main/resources/application.yml`：

| 配置项 | 值 | 说明 |
| --- | --- | --- |
| `server.port` | `8080` | 服务端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/jira?...` | 含 `serverTimezone=Asia/Shanghai`、`allowPublicKeyRetrieval=true` |
| `spring.datasource.username` | `${DB_USERNAME:root}` | 环境变量优先，默认 `root` |
| `spring.datasource.password` | `${DB_PASSWORD:}` | **必须通过环境变量提供** |
| `mybatis.mapper-locations` | `classpath:mapper/*.xml` | MyBatis XML 位置 |
| `mybatis.configuration.map-underscore-to-camel-case` | `true` | `person_id` → `personId` 自动映射 |
| `jira.jwt.secret` | `${JWT_SECRET:...}` | 生产环境必须替换为随机长字符串（≥32 字符） |
| `jira.jwt.expire-hours` | `24` | token 有效期（小时） |

> ⚠️ **不要提交任何明文密码、Token、密钥。** 本地开发用环境变量或 `application-local.yml`（已在 `.gitignore` 中）。

---

## 五、接口约定

| 约定 | 内容 |
| --- | --- |
| 路径前缀 | **无**。接口路径与原 Mock 保持一致（`/login`、`/me`、`/projects`、`/tasks`…），未加 `/api` 前缀 |
| 成功响应 | `{ "code": 0, "message": "成功", "data": ... }` |
| 错误响应 | `{ "code": 4xx/5xx, "message": "面向用户的提示" }`，前端直接展示 `message` |
| HTTP 状态码 | 与错误语义一致（400 参数错误 / 401 未登录 / 403 无权限 / 404 不存在 / 409 冲突 / 500 服务器错误），便于前端识别 401 并跳转登录 |
| 分页 | 仅**项目列表**分页，返回 `data: { list: [...], total: N }`；看板 / 任务 / 任务组列表**不分页**，直接返回数组 |
| ID 与时间 | ID 为整型；时间字段为**毫秒时间戳**（如 `created`、`start_time`），与前端 `dayjs(ms)` 契约一致 |

错误码定义见 `common/ErrorCode.java`。

---

## 六、目录结构

```
jira-backend/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/jira/
    │   ├── JiraApplication.java       # 启动类
    │   ├── common/                    # 统一响应 Result、分页 PageResult、
    │   │                              # 错误码 ErrorCode、BusinessException、
    │   │                              # 全局异常处理 GlobalExceptionHandler
    │   ├── config/                    # CorsConfig（跨域）
    │   ├── system/                    # HealthController / HealthMapper（自检用）
    │   ├── security/                  # 登录拦截、JWT、项目成员权限校验
    │   ├── user/                      # 认证与用户
    │   ├── project/                   # 项目 + 项目成员
    │   ├── tasktype/                  # 任务类型
    │   ├── kanban/                    # 看板列
    │   ├── task/                      # 任务
    │   └── epic/                      # 任务组
    └── resources/
        ├── application.yml
        └── mapper/                    # MyBatis XML
```

**模块分工**（详见仓库根目录《后端重构调研文档》第八章）：

| 成员 | 负责 |
| --- | --- |
| A | `common/`、`config/`、`security/`、`user/`、`project/` |
| B | `tasktype/`、`kanban/`、`task/`、`epic/` |

每个模块内部统一分：`controller / service / mapper / entity / dto`。

> Mapper 接口需要标注 `@Mapper`（项目未使用 `@MapperScan`，避免误扫 Service 接口）。

---

## 七、当前进度

| 阶段 | 状态 |
| --- | --- |
| 数据库脚本 `db/schema.sql`、`db/data.sql` | ✅ 已完成 |
| 工程骨架（能启动、能连库、`/health` 通过） | ✅ 已完成 |
| 接口文档 | ⬜ 待产出 |
| 业务模块（user / project / kanban / task / epic / tasktype） | ⬜ 待开发 |

---

## 八、常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 启动报 `Access denied for user 'root'@'localhost'` 或 `HikariPool` 相关错误 | 数据库密码没配。设置环境变量 `DB_PASSWORD`（见第三章第 2 步） |
| 启动报 `Communications link failure` | MySQL 服务没启动。`services.msc` 里启动 `MySQL80`，或确认端口 `3306` 未被占用 |
| 访问 `http://localhost:8080/` 报 `NoResourceFoundException` | **正常现象**。后端服务没有首页，请访问具体接口如 `/health` |
| 日志出现 `No static resource @vite/client` | 有前端工具（Vite）在请求本服务，与本后端无关，可忽略 |
| 端口 8080 被占用 | 改 `application.yml` 的 `server.port`，并同步修改前端 `jira/.env.development` 的 `REACT_APP_API_URL` |
| `data.sql` 报 `Error 1175 (safe update mode)` | Workbench 的 Safe Updates 拦截了不带 `WHERE` 的 `DELETE`。在 `data.sql` 顶部已有 `SET SQL_SAFE_UPDATES = 0;`；也可在 Workbench 偏好设置里永久关闭 |

---

## 九、前端联调

前端在仓库的 `jira/` 目录，启动：

```bash
cd frontend
npm install
npm start          # 开发服务器默认 3000 端口
```

前端的接口地址在 `frontend/.env.development`：

```
REACT_APP_API_URL=http://localhost:8080
```

> 联调时请**停掉原来的 json-server**（`npm run server`，占用 3001 端口），避免请求仍打到 Mock。

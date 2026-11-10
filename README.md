# Spring Boot Security Hello World

基于 Spring Boot 3.2.0、Spring Security 和 Java 17。演示账号为 `test` / `123456`。用户保存在内存中，密码由 BCrypt 编码。

## 运行

确保 `JAVA_HOME` 指向 JDK 17。在项目根目录运行：

```powershell
.\mvnw.cmd spring-boot:run
```

在 macOS 或 Linux 上使用 `./mvnw spring-boot:run`。应用默认监听端口 8080。

## 接口

| 方法和路径 | 请求 | 响应 |
| --- | --- | --- |
| `POST /login` | JSON，包含 `username` 和 `password` | 成功返回 `204` 和 `JSESSIONID` Cookie。凭据错误返回 `401`，缺少字段返回 `400`。 |
| `GET /hello` | 携带登录时收到的 Cookie | 已认证返回 `200` 和纯文本 `Hello World`，未认证返回 `401`。 |

### 验证（PowerShell）

```powershell
# 未登录，返回 401
curl.exe -i http://localhost:8080/hello

# 登录，返回 204，并保存会话 Cookie
$body = @{ username = 'test'; password = '123456' } | ConvertTo-Json
Invoke-WebRequest -Uri 'http://localhost:8080/login' -Method Post `
    -ContentType 'application/json' -Body $body -SessionVariable loginSession |
    Select-Object -ExpandProperty StatusCode

# 携带 Cookie，输出 Hello World
(Invoke-WebRequest -Uri 'http://localhost:8080/hello' -WebSession $loginSession).Content
```

### 自动化测试

```powershell
.\mvnw.cmd test
```

测试启动嵌入式 Tomcat，并通过 HTTP 验证未认证访问、错误密码、缺少字段、非 JSON 登录和登录后使用会话访问。

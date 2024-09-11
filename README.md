# Spring REST MVC

## 项目简介
该项目是基于Spring 开发的RESTful API 服务，提供了用户认证、数据库操作、缓存管理、日志监控等功能，旨在构建一个健壮、可扩展的后端服务。


## 技术栈
- **后端框架**: Spring Boot, Spring MVC
- **ORM框架**: Hibernate, JPA
- **数据库**: MySQL, H2（用于开发和测试环境）
- **安全认证**: Spring Security、OAuth2
- **对象映射**: MapStruct
- **日志**: Logbook, Logstash
- **数据库迁移**: Flyway
- **API文档**: OpenAPI 3, Swagger


## 功能
1. **用户认证与授权**: 集成了OAuth2授权服务器，提供安全的用户登录认证功能。
2. **数据持久化**: 通过JPA与MySQL交互，支持复杂的数据库查询与操作，对客户、啤酒订单和啤酒信息进行管理。
3. **日志监控**: 使用Logbook与Logstash进行结构化日志记录，便于追踪请求响应数据。
4. **API文档**: 使用OpenAPI生成交互式API文档，便于前端开发和系统集成。

## 快速开始

### 前提条件
- Java 21
- Maven 3.x
- MySQL 数据库

### 安装步骤
1. 克隆项目到本地:
   ```bash
   git clone https://github.com/SaulGoodman-jl/spring-6-rest-mvc.git
   ```
2. 配置数据库:
   在`application-localmysql.properties`文件中配置MySQL数据库连接信息：
   ```properties
   spring.datasource.url=your_datasource_url
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. 启动应用:
   ```bash
   .\mvnw spring-boot:run
   ```

4. 访问API文档：
   启动应用后，可以通过以下地址查看API文档:
   ```
   http://localhost:8081/v3/api-docs
   ```
   或者通过[此文件](src/test/resources/oa3.yml)查看OpenAPI文档。

## 测试
您可以使用以下命令运行测试：
```bash
.\mvnw test
```

## License
本项目采用MIT许可证，详见[LICENSE](LICENSE)文件。
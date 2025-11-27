# OkoooEncodingTest测试指南

## 项目配置检查

已确认项目中包含必要的测试依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## 测试类说明

OkoooEncodingTest.java 是一个Spring Boot测试类，用于验证RestTemplate处理GB2312编码数据的能力。测试内容包括：

- 发送HTTP请求到Okooo网站获取足球数据
- 测试不同编码(UTF-8、GB2312)的解码结果
- 验证HTML内容的正确性

## 运行测试的方法

### 方法一：使用Maven命令行

在项目根目录执行以下命令：

```bash
mvn test -Dtest=com.soccer.test.OkoooEncodingTest
```

### 方法二：使用IDE直接运行

#### IntelliJ IDEA
1. 打开项目
2. 在左侧项目结构中找到`src/test/java/com/soccer/test/OkoooEncodingTest.java`
3. 右键点击文件，选择"Run 'OkoooEncodingTest'"
4. 或在代码编辑器中，点击测试方法左侧的绿色运行按钮

#### Eclipse
1. 打开项目
2. 在Package Explorer中找到测试类
3. 右键点击文件，选择"Run As" -> "JUnit Test"

### 方法三：通过Spring Boot Maven插件运行

```bash
mvn spring-boot:test -Dtest=com.soccer.test.OkoooEncodingTest
```

### 方法四：创建测试启动类（推荐）

在`com.soccer.test`包下创建一个测试启动类，便于快速运行测试：

```java
package com.soccer.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class OkoooEncodingTestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(OkoooEncodingTest.class);
        
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        
        System.out.println("测试运行结果: " + (result.wasSuccessful() ? "通过" : "失败"));
    }
}
```

创建完成后，直接运行此类的main方法即可执行测试。

## 测试结果分析

测试运行后，会在控制台输出以下信息：

1. 发送请求的URL
2. 响应的Content-Type
3. 响应字节长度
4. UTF-8解码结果的前100个字符
5. GB2312解码结果的前100个字符
6. HTML内容中是否包含"gb2312"字符串
7. GB2312解码是否成功（是否包含DOCTYPE声明）

正常情况下，应该能看到GB2312解码的内容是正确的中文，没有乱码现象。

## 注意事项

1. 确保网络连接正常，能够访问Okooo网站
2. 测试使用的Cookie可能会过期，如遇到403或其他认证错误，可能需要更新Cookie
3. 如果测试失败，可以检查RestTemplateConfig.java中的编码配置是否正确

## 常见问题处理

- **问题**：测试运行时报"Connection refused"或"Cannot connect to host"
  **解决方法**：检查网络连接，确认Okooo网站是否可以正常访问

- **问题**：测试通过但仍有乱码
  **解决方法**：检查RestTemplateConfig中的编码配置，确认是否正确配置了GB2312支持

- **问题**：测试报"NoSuchBeanDefinitionException: No qualifying bean of type 'RestTemplate'"
  **解决方法**：确认RestTemplateConfig是否正确定义了RestTemplate Bean

## Spring Boot主应用类信息

项目的Spring Boot主应用类是：`com.CrudApplication`，包含`@SpringBootApplication`和`@EnableScheduling`注解。
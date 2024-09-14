

#### 配置文件

```apl
mybatis-plus.check-config-location=true
mybatis-plus.configuration-properties.cache.enable=true
mybatis-plus.config-location=classpath:/mybatis/mybatis-config.xml
mybatis-plus.mapper-locations=classpath:/mybatis/com/icc/account/mapper/*.xml
```



#### pom.xml

```xml
<!--  mybatis依赖-->
<!--新增mybatis-plus 开始-->

    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.1.1</version>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-generator</artifactId>
        <scope>test</scope>
        <version>3.1.1</version>
    </dependency>
    <dependency>
        <groupId>com.github.pagehelper</groupId>
        <artifactId>pagehelper-spring-boot-starter</artifactId>
        <version>1.2.12</version>
    </dependency>
```



#### 新增文件

```java
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class MybatisPlusConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        return paginationInterceptor;
    }

    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }
}
```


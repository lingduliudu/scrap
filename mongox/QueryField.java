package com.example.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface QueryField {
    String value() default ""; // 列名
    String regex() default "";
    String regexConfig() default "";
    QueryType queryType() default QueryType.EQ;
    IgnoreType ignoreType() default IgnoreType.EMPTY;
    QueryLogic queryLogic() default  QueryLogic.AND;  // 未实现
    String owner() default "TOP"; // 未实现
    boolean auto() default true;
}

package com.spider.mybatix;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface QueryField {

    String[] value() default {};
    KV[] match() default {};
    String group() default "TOP";
    String miss() default "FALSE";
    String empty() default "";
    boolean isOr() default false;
    QueryType type() default QueryType.EQ;
    int sortIndex() default 1;
    boolean simpleQueryTrim() default false;
    IgnoreType ignoreType() default  IgnoreType.EMPTY;
    FrontTimeMode frontTimeMode() default FrontTimeMode.NONE;
    Class<? extends IMPXConvert> using() default IMPXConvert.class;

}
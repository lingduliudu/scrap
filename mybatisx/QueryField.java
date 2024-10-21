package com.icc.framework.api.annotation.mybatisx;

import com.icc.framework.api.annotation.KV;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface QueryField {

    String value() default "";
    String[] multiValue() default {};
    KV[] match() default {};
    String miss() default "FALSE";
    String empty() default "";
    QueryType type() default QueryType.EQ;
    int sortIndex() default 1;
    boolean simpleQueryTrim() default false;
    IgnoreType ignoreType() default  IgnoreType.EMPTY;
    Class<? extends IMPXConverter> using() default IMPXConverter.class;
    boolean auto() default true;

}
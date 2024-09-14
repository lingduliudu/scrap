package com.spider.mybatix;

/**
 * @Description: 查询类型
 * @author Hao.Yuan
 * @date 2023/11/8
 */
public enum QueryType {
    EQ, // 相等
    NE, // 不相等
    LIKE, // 模糊查询
    MULTIPLE_LIKE, // 多模糊
    LIKER, // 模糊查询(右)
    LIKEL, //  模糊查询(左)
    IN, //  包含
    NOT_IN, // 不包含
    INNER, //  内嵌
    GROUP, // 分组
    NOT_NULL, // 不为空
    NULL, // 空
    ASC, // 正序
    DESC, // 倒序
    ASC_NOTEMPTY, // 正序-非空生效
    DESC_NOTEMPTY, // 倒序-非空生效
    GT, // 大于
    GE, //大于等于
    LT, // 小于
    LE, // 小于等于
    MATCH, // 自动匹配 占位
    LAST; // 拼接
}
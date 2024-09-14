package com.spider.mybatix;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class WP {

    /**
     * @Description: 直接查询
     * @author Hao.Yuan
     * @date 2023/11/8
     */
    public static QueryWrapper to(Object data){
        try{
            QueryWrapper orgin = new WrapperParse().to(data);
            return orgin;
        }catch(Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }

}
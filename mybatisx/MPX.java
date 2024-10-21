package com.icc.account.mybatisx;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MPX {
    /**
     * @Description: 直接查询
     * @author Hao.Yuan
     * @date 2023/11/8
     */
    public static QueryWrapper to(Object data){
        try{
            return new WrapperParse().to(data);
        }catch(Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
    }

}
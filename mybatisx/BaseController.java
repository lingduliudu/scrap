package com.spider.controller.base;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.spring.util.BeanUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spider.response.PageT;
import com.spider.tools.RequestTool;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BaseController {
    private static final String PAGE_NO="pageNumber";
    private static final String PAGE_SIZE="pageSize";
    public static ThreadLocal<Long> pageNoLocal = new ThreadLocal<>();
    public static ThreadLocal<Long> pageSizeLocal = new ThreadLocal<>();
    /**
     * @Description: 获取request
     * @author Hao.Yuan
     * @date 2024/8/30
     */
    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }
    /**
     * @Description: 获取语言 例如:zh-cn
     * @author Hao.Yuan
     * @date 2024/8/30
     */
    public String getLanguage() {
        String language = "en";
        String headerLanguage = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Content-Language");
        if("zh-cn".equals(headerLanguage)){
            language = "zh-cn";
        }
        return language;
    }
    /**
     * @Description: 获取语言 例如:zh-cn
     * @author Hao.Yuan
     * @date 2024/8/30
     */
    public String getToken() {
        String headerAuth = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
        return headerAuth;
    }
    /**
     * @Description: 获取request
     * @author Hao.Yuan
     * @date 2024/8/30
     */
    public Integer getParameterToInt(String name) {
        return Convert.toInt(getRequest().getParameter(name));
    }

    public Page initPage(){
        Integer pageNo = getParameterToInt(PAGE_NO);
        Integer pageSize = getParameterToInt(PAGE_SIZE);
        if(pageNo==null)pageNo=1;
        if(pageSize==null)pageSize=10;
        return new Page(pageNo,pageSize);
    }
    public <T> PageT<T> toPageT(Page<T> page){
        PageT t = new PageT();
        t.setRows(page.getRecords());
        t.setPageNo(page.getCurrent());
        t.setPageSize(page.getSize());
        t.setTotalPage(page.getPages());
        t.setTotalRow(page.getTotal());
        return t;
    }

    public Object toT(Object obj,Class clazz){
        if(obj == null)return null;
        Object result = JSONObject.parseObject(JSONObject.toJSONString(obj),clazz);
        return result;
    }

    public  PageT toPageT(Page page,Class clazz){
        PageT t = new PageT();
        if(page.getRecords().size()>0){
            t.setRows(copyList(page.getRecords(),clazz));
        }else{
            t.setRows(new ArrayList<>());
        }
        t.setPageNo(page.getCurrent());
        t.setPageSize(page.getSize());
        t.setTotalPage(page.getPages());
        t.setTotalRow(page.getTotal());
        return t;
    }
    public List copyList(List list, Class clazz){
        List result = new ArrayList();
        for(Object obj:list){
            Object obj2 = JSONObject.parseObject(JSONObject.toJSONString(obj),clazz);
            result.add(obj2);
        }
        return result;
    }
    public void initPage2(){
        Long pageNo = 1L;
        Long pageSize = 10L;
        if(pageNoLocal.get() != null){
            pageNo = pageNoLocal.get();
            pageNoLocal.remove();
        }
        if(pageSizeLocal.get() != null){
            pageSize = pageSizeLocal.get();
            pageSizeLocal.remove();
        }
        System.out.println(pageNo);
        System.out.println(pageSize);
    }
}

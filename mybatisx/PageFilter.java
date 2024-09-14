package com.spider.config;

import com.alibaba.fastjson2.JSONObject;
import com.spider.controller.base.BaseController;
import com.spider.interceptor.MultiReadHttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Description: 用来处理page不同
 * @author Hao.Yuan
 * @date 2024/9/12
 */

@Component
@Slf4j
public class PageFilter implements Filter {


    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)throws IOException, ServletException {
        MultiReadHttpServletRequest wrappedRequest = new MultiReadHttpServletRequest((HttpServletRequest) servletRequest);
        String pageNum = wrappedRequest.getHeader("pageNum");
        String pageSize = wrappedRequest.getHeader("pageSize");
        savePageNum(pageNum);
        savePageSize(pageSize);
        pageNum = wrappedRequest.getParameter("pageNum");
        pageSize = wrappedRequest.getParameter("pageSize");
        savePageNum(pageNum);
        savePageSize(pageSize);
        if(pageNum==null || pageSize==null){
            String str = IOUtils.toString(wrappedRequest.getInputStream());
            if(str!=null){
                JSONObject j = JSONObject.parseObject(str);
                if(j!=null){
                    pageNum = j.getString("pageNum");
                    pageSize = j.getString("pageSize");
                    savePageNum(pageNum);
                    savePageSize(pageSize);
                }
            }
        }
        filterChain.doFilter(wrappedRequest, servletResponse);
    }

    private void savePageNum(String pageNum){
        if(pageNum == null)return;
        BaseController.pageNoLocal.set(Long.parseLong(pageNum));
    }
    private void savePageSize(String pageSize){
        if(pageSize == null)return;
        BaseController.pageSizeLocal.set(Long.parseLong(pageSize));
    }



}
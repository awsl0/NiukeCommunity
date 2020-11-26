package com.bilibili.config.Interceptor;



import com.bilibili.utils.MD5Utils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice
@Slf4j
public class ExceptionHandle {
    @ExceptionHandler(Exception.class)
    public void exceptionHandle(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("服务器异常："+e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            log.error(element.toString());
        }
        String header = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(header)){
            response.setContentType("application/plain; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(MD5Utils.getJsonString("500","服务器异常"));
        }else {
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }
}

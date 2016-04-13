package com.tropo.webapp;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class TropoFilter implements Filter {
    
    private boolean isEnabled;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    
        String enabled = filterConfig.getInitParameter("enabled");
        if ("true".equalsIgnoreCase(enabled)) {
            isEnabled = true;
        }
        
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    
        if (isEnabled) {
            printHeaders((HttpServletRequest) request);
        }
        
        chain.doFilter(request, response);
        
    }
    
    @Override
    public void destroy() {
    
    }
    
    private void printHeaders(HttpServletRequest request) {
    
        System.out.println("*****request dumpHeader*****");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            
            String headerName = headerNames.nextElement();
            System.out.println(headerName);
            
            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                System.out.println("\t" + headers.nextElement());
            }
        }
        
    }
    
}

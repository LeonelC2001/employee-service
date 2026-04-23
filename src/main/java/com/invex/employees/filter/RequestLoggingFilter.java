package com.invex.employees.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .filter(h -> !h.equalsIgnoreCase("authorization"))
                .map(h -> h + "=" + request.getHeader(h))
                .collect(Collectors.joining(", "));

        log.info("Incoming request | method={} uri={} correlationId={} requestId={} headers=[{}]",
                method, uri, correlationId, requestId, headers);

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;

        log.info("Completed request | method={} uri={} status={} duration={}ms",
                method, uri, response.getStatus(), duration);
    }
}

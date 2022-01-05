package ru.dmerkushov.mkcache.rest.filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * Filter incoming requests and update them with request ids
 */
@Component
@Log4j2
public class RequestIdFilter implements Filter {

    public static final String requestIdHeaderName = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.debug("+doFilter()");

        HttpServletRequest httpServletRequest;
        try {
            httpServletRequest = (HttpServletRequest) servletRequest;
        } catch (ClassCastException e) {
            log.warn("-doFilter(): Received a request that is not an HTTP request: remote host {}, remote port {}", servletRequest.getRemoteHost(), servletRequest.getRemotePort());
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String requestId = httpServletRequest.getHeader(requestIdHeaderName);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
            log.info("For request from remote host {}, remote port {}, method {}, URI {}: generated requestId {}",
                    servletRequest.getRemoteHost(),
                    servletRequest.getRemotePort(),
                    httpServletRequest.getMethod(),
                    httpServletRequest.getRequestURI(),
                    requestId);
        } else if (log.isInfoEnabled()) {
            log.info("For request from remote host {}, remote port {}, method {}, URI {}: received requestId {}",
                    servletRequest.getRemoteHost(),
                    servletRequest.getRemotePort(),
                    httpServletRequest.getMethod(),
                    httpServletRequest.getRequestURI(),
                    requestId);
        }
        servletRequest.setAttribute("requestId", requestId);

        log.debug("-doFilter(): OK");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}

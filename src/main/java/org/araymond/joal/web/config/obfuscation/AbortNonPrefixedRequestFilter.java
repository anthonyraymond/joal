package org.araymond.joal.web.config.obfuscation;

import org.araymond.joal.web.annotations.ConditionalOnWebUi;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * As a security measure, all request coming from a non-prefixed URI will be closed before an actual response is sent to the client.
 */
@ConditionalOnWebUi
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AbortNonPrefixedRequestFilter implements Filter {
    private static final Logger logger = getLogger(AbortNonPrefixedRequestFilter.class);
    private final String pathPrefix;

    public AbortNonPrefixedRequestFilter(@Value("${joal.ui.path.prefix}")final String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;

        String requestedUri = req.getRequestURI();
        if (requestedUri.startsWith("/")) {
            requestedUri = requestedUri.substring(1);
        }
        if (!requestedUri.startsWith(pathPrefix)) {
            logger.warn("Request was sent to URI '{}' and does not match the path prefix, therefore the request Thread has been shut down.", req.getRequestURI());
            Thread.currentThread().interrupt();
            return;
        }
        chain.doFilter(request, response);
    }
}

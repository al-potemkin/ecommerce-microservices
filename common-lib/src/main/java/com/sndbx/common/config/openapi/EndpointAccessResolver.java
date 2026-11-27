package com.sndbx.common.config.openapi;

import org.springframework.web.method.HandlerMethod;

@FunctionalInterface
public interface EndpointAccessResolver {

    Access resolve(HandlerMethod method);

    enum Access {
        SECURED,         // lock, 401 (and 403 if enabled)
        TOKEN_OPTIONAL,  // works without a token, but the token is sent if you are authorized
        PUBLIC           // no lock
    }
}
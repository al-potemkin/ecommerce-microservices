package com.sndbx.common.exception.data;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String name();

    HttpStatus getStatus();

    String getTitle();

    String getDefaultDetail();
}

package com.sndbx.user.exception.config;

import com.sndbx.common.exception.data.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    ;

    private final HttpStatus status;
    private final String title;
    private final String defaultDetail;
}

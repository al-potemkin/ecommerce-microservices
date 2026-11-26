package com.sndbx.common.kafka;

import java.util.List;

public interface KafkaNotRetryableExceptions {
    List<Class<? extends Exception>> classes();
}

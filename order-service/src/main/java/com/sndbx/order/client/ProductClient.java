package com.sndbx.order.client;

import com.sndbx.order.dto.client.VariantInfo;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;
import java.util.Set;

@HttpExchange("/internal")
public interface ProductClient {

    @GetExchange("/variants")
    List<VariantInfo> getVariants(@RequestParam("skus") Set<String> skus);
}

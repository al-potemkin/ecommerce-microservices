package com.sndbx.order.config.client;

import com.sndbx.order.controller.DevVariantsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;
import java.util.Set;

@HttpExchange("/internal")
public interface ProductClient {
//    Logger log = LoggerFactory.getLogger(ProductClient.class);

    @GetExchange("/variants")
    List<DevVariantsController.VariantInfo> getVariants(@RequestParam("skus") Set<String> skus);
}

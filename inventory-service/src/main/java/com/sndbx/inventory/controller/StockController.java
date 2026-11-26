package com.sndbx.inventory.controller;

import com.sndbx.inventory.controller.openapi.StockApi;
import com.sndbx.inventory.dto.request.ReceiveStockRequest;
import com.sndbx.inventory.dto.request.SetOnHandRequest;
import com.sndbx.inventory.dto.response.PageResponse;
import com.sndbx.inventory.dto.response.StockItemResponse;
import com.sndbx.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/stock")
@RequiredArgsConstructor
public class StockController implements StockApi {

    private final StockService stockService;

    @Override
    public PageResponse<StockItemResponse> list(String productId, int page, int size) {
        return stockService.findAll(productId, page, size);
    }

    @Override
    public StockItemResponse get(String sku) {
        return stockService.get(sku);
    }

    @Override
    public StockItemResponse receive(String sku, ReceiveStockRequest request) {
        return stockService.receive(sku, request.quantity());
    }

    @Override
    public StockItemResponse setOnHand(String sku, SetOnHandRequest request) {
        return stockService.setOnHand(sku, request.onHand());
    }
}
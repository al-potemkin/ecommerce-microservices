package com.sndbx.inventory.controller;

import com.sndbx.inventory.dto.response.PageResponse;
import com.sndbx.inventory.dto.request.ReceiveStockRequest;
import com.sndbx.inventory.dto.request.SetOnHandRequest;
import com.sndbx.inventory.dto.response.StockItemResponse;
import com.sndbx.inventory.service.StockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public PageResponse<StockItemResponse> list(
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return stockService.findAll(productId, page, size);
    }

    @GetMapping("/{sku}")
    public StockItemResponse get(@PathVariable String sku) {
        return stockService.get(sku);
    }

    @PostMapping("/{sku}/receipts")
    public StockItemResponse receive(@PathVariable String sku,
                                     @Valid @RequestBody ReceiveStockRequest request) {
        return stockService.receive(sku, request.quantity());
    }

    @PutMapping("/{sku}/on-hand")
    public StockItemResponse setOnHand(@PathVariable String sku,
                                       @Valid @RequestBody SetOnHandRequest request) {
        return stockService.setOnHand(sku, request.onHand());
    }
}
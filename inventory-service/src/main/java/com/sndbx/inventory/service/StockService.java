package com.sndbx.inventory.service;

import com.sndbx.inventory.dto.response.PageResponse;
import com.sndbx.inventory.dto.response.StockItemResponse;
import com.sndbx.inventory.entity.StockItem;
import com.sndbx.inventory.exception.StockBelowReservedException;
import com.sndbx.inventory.exception.StockItemNotFoundException;
import com.sndbx.inventory.mapper.StockItemMapper;
import com.sndbx.inventory.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockItemRepository repository;
    private final StockAvailability availability;
    private final StockItemMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<StockItemResponse> findAll(String productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sku"));
        Page<StockItem> items = (productId == null)
                ? repository.findAll(pageable)
                : repository.findByProductId(productId, pageable);
        return mapper.toPageResponse(items);
    }

    @Transactional(readOnly = true)
    public StockItemResponse get(String sku) {
        return mapper.toResponse(findOrThrow(sku));
    }

    @Transactional
    public StockItemResponse receive(String sku, int quantity) {
        StockItem item = findOrThrow(sku);
        int availableBefore = availability.of(item);
        item.setOnHand(item.getOnHand() + quantity);
        StockItem saved = repository.saveAndFlush(item);
        availability.recordChange(saved, availableBefore);
        return mapper.toResponse(saved);
    }

    @Transactional
    public StockItemResponse setOnHand(String sku, int onHand) {
        StockItem item = findOrThrow(sku);
        if (onHand < item.getReserved()) {
            throw new StockBelowReservedException(sku, onHand, item.getReserved());
        }
        int availableBefore = availability.of(item);
        item.setOnHand(onHand);
        StockItem saved = repository.saveAndFlush(item);
        availability.recordChange(saved, availableBefore);
        return mapper.toResponse(saved);
    }

    @Transactional
    public int registerSkus(String productId, Collection<String> skus) {
        int created = 0;
        for (String sku : skus) {
            created += repository.insertIfAbsent(sku, productId);
        }
        return created;
    }

    private StockItem findOrThrow(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new StockItemNotFoundException(sku));
    }
}
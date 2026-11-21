package com.sndbx.product.dto;

public enum CatalogSort {
    NEWEST,      // сначала новые (по дате создания)
    PRICE_ASC,   // сначала дешёвые (по минимальной цене)
    PRICE_DESC,  // сначала дорогие (по минимальной цене)
    NAME         // по названию, А–Я
}
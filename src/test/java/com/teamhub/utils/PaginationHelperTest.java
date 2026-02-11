package com.teamhub.utils;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaginationHelperTest {

    @Test
    void calculateSkip_firstPage() {
        assertEquals(0, PaginationHelper.calculateSkip(1, 20));
    }

    @Test
    void calculateSkip_secondPage() {
        assertEquals(20, PaginationHelper.calculateSkip(2, 20));
    }

    @Test
    void calculateSkip_customPageSize() {
        assertEquals(50, PaginationHelper.calculateSkip(3, 25));
    }

    @Test
    void calculateTotalPages_exactDivision() {
        assertEquals(5, PaginationHelper.calculateTotalPages(100, 20));
    }

    @Test
    void calculateTotalPages_withRemainder() {
        assertEquals(6, PaginationHelper.calculateTotalPages(101, 20));
    }

    @Test
    void calculateTotalPages_emptyResults() {
        assertEquals(0, PaginationHelper.calculateTotalPages(0, 20));
    }

    @Test
    void calculateTotalPages_singleItem() {
        assertEquals(1, PaginationHelper.calculateTotalPages(1, 20));
    }

    @Test
    void calculateTotalPages_zeroPageSize() {
        assertEquals(0, PaginationHelper.calculateTotalPages(10, 0));
    }

    @Test
    void buildPaginationMeta_standard() {
        JsonObject meta = PaginationHelper.buildPaginationMeta(1, 20, 50);

        assertEquals(1, meta.getInteger("page"));
        assertEquals(20, meta.getInteger("pageSize"));
        assertEquals(50L, meta.getLong("totalItems"));
        assertEquals(3, meta.getInteger("totalPages"));
    }

    @Test
    void buildPaginationMeta_emptyResults() {
        JsonObject meta = PaginationHelper.buildPaginationMeta(1, 20, 0);

        assertEquals(1, meta.getInteger("page"));
        assertEquals(20, meta.getInteger("pageSize"));
        assertEquals(0L, meta.getLong("totalItems"));
        assertEquals(0, meta.getInteger("totalPages"));
    }

    @Test
    void buildPaginationMeta_lastPage() {
        JsonObject meta = PaginationHelper.buildPaginationMeta(3, 20, 50);

        assertEquals(3, meta.getInteger("page"));
        assertEquals(20, meta.getInteger("pageSize"));
        assertEquals(50L, meta.getLong("totalItems"));
        assertEquals(3, meta.getInteger("totalPages"));
    }
}

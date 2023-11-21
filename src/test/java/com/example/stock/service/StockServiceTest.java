package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(Stock.builder().productId(1L).quantity(100L).build());
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @DisplayName("재고 감소")
    @Test
    void 재고감소() {
        // given
        stockService.decrease(1L, 1L);

        // when
        Stock stock = stockRepository.findById(1L).orElseThrow();

        // then
        assertThat(stock.getQuantity()).isEqualTo(99);
    }

    @DisplayName("동시에 100개의 요청")
    @Test
    void 동시에_100개의_요청() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        // then
        assertThat(stock.getQuantity()).isEqualTo(0);
    }
}
package com.supshop.sup_shop_backend.integration;

import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.service.WishlistService;
import com.supshop.sup_shop_backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// проверяю сам SQL INSERT ... ON CONFLICT DO NOTHING, ради которого переписывал избранное
class WishlistIntegrationTest extends AbstractIntegrationTest {

    @Autowired private WishlistService wishlistService;

    @Test
    void mergeSkipsDuplicatesAndUnknownProducts() {
        User user = newUser();
        Product board = newProduct(1, "100");
        Product paddle = newProduct(1, "100");
        long missingId = 999_999_999L;

        List<Long> ids = wishlistService.merge(user.getUsername(), List.of(board.getId(), board.getId(), paddle.getId(), missingId)).productIds();
        assertThat(ids).containsExactlyInAnyOrder(board.getId(), paddle.getId());

        // второй перенос того же самого не падает на UNIQUE и ничего не дублирует
        ids = wishlistService.merge(user.getUsername(), List.of(board.getId())).productIds();
        assertThat(ids).hasSize(2);
    }

    // та самая гонка из-за StrictMode: раньше второй запрос падал с 500 на UNIQUE (user_id, product_id)
    @Test
    void parallelAddsOfSameProductDoNotFail() throws Exception {
        User user = newUser();
        Product board = newProduct(1, "100");

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    wishlistService.add(user.getUsername(), board.getId());
                    return null;
                }));
            }
            start.countDown();
            // get() перебросит исключение из потока, если хоть один add упал
            for (Future<?> result : results) {
                result.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(wishlistService.get(user.getUsername()).productIds()).containsExactly(board.getId());
    }
}

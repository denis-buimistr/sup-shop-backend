package com.supshop.sup_shop_backend.integration;

import com.supshop.sup_shop_backend.dto.order.CheckoutRequest;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.model.DeliveryMethod;
import com.supshop.sup_shop_backend.model.PaymentMethod;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.OrderRepository;
import com.supshop.sup_shop_backend.service.OrderService;
import com.supshop.sup_shop_backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// то, что моки проверить не могут: атомарный UPDATE остатка и откат @Transactional на настоящем Postgres
class CheckoutIntegrationTest extends AbstractIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    // 5 человек одновременно покупают товар, которого осталось 2 штуки: заказов должно быть ровно 2, остаток 0, не минус
    @Test
    void parallelCheckoutsNeverSellMoreThanStock() throws Exception {
        Product lastBoards = newProduct(2, "9990.00");
        List<User> buyers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            User buyer = newUser();
            putInCart(buyer, lastBoards, 1);
            buyers.add(buyer);
        }

        AtomicInteger sold = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(buyers.size());
        try {
            List<Future<?>> results = new ArrayList<>();
            for (User buyer : buyers) {
                results.add(pool.submit(() -> {
                    // все потоки ждут общего старта, чтобы запросы реально пересеклись
                    start.await();
                    try {
                        orderService.checkout(buyer.getUsername(), pickup());
                        sold.incrementAndGet();
                    } catch (BadRequestException e) {
                        rejected.incrementAndGet();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> result : results) {
                result.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(sold.get()).isEqualTo(2);
        assertThat(rejected.get()).isEqualTo(3);
        assertThat(stockOf(lastBoards)).isZero();
    }

    // первую позицию сервис уже списал, а на второй упал. @Transactional должен вернуть первую на склад
    @Test
    void failedCheckoutRollsBackStockOfEarlierItems() {
        User buyer = newUser();
        Product paddle = newProduct(5, "4290.00");
        Product board = newProduct(1, "15990.00");
        // позиции корзины читаются в порядке вставки, поэтому весло обработается первым
        putInCart(buyer, paddle, 2);
        putInCart(buyer, board, 1);
        // пока человек был на странице оформления, последнюю доску купил кто-то другой
        jdbcTemplate.update("UPDATE products SET quantity = 0 WHERE id = ?", board.getId());

        assertThatThrownBy(() -> orderService.checkout(buyer.getUsername(), pickup()))
                .isInstanceOf(BadRequestException.class);

        assertThat(stockOf(paddle)).isEqualTo(5);
        assertThat(orderRepository.findByUserIdOrderByCreatedAtDesc(buyer.getId())).isEmpty();
        // корзина остаётся: человек может убрать доску и оформить весло
        assertThat(cartItemRepository.findByCartId(cartRepository.findByUserId(buyer.getId()).orElseThrow().getId())).hasSize(2);
    }

    @Test
    void successfulCheckoutStoresOrderAndEmptiesCart() {
        User buyer = newUser();
        Product leash = newProduct(3, "490.00");
        putInCart(buyer, leash, 2);

        orderService.checkout(buyer.getUsername(), pickup());

        assertThat(stockOf(leash)).isEqualTo(1);
        assertThat(orderRepository.findByUserIdOrderByCreatedAtDesc(buyer.getId())).hasSize(1);
        assertThat(cartItemRepository.findByCartId(cartRepository.findByUserId(buyer.getId()).orElseThrow().getId())).isEmpty();
    }

    private static CheckoutRequest pickup() {
        return new CheckoutRequest("Ion", "Munteanu", "+37369000000", "ion@mail.md",
                DeliveryMethod.PICKUP, null, null, PaymentMethod.CASH_ON_DELIVERY, null);
    }
}

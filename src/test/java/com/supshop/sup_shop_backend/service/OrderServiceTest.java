package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.order.CheckoutRequest;
import com.supshop.sup_shop_backend.dto.order.OrderResponse;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.model.Cart;
import com.supshop.sup_shop_backend.model.CartItem;
import com.supshop.sup_shop_backend.model.DeliveryMethod;
import com.supshop.sup_shop_backend.model.Order;
import com.supshop.sup_shop_backend.model.OrderItem;
import com.supshop.sup_shop_backend.model.PaymentMethod;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.CartItemRepository;
import com.supshop.sup_shop_backend.repository.CartRepository;
import com.supshop.sup_shop_backend.repository.OrderRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import com.supshop.sup_shop_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.supshop.sup_shop_backend.support.AppExceptionAssert.assertKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Spring тут не поднимаю: все репозитории — моки, проверяю только логику сервиса
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final BigDecimal FREE_DELIVERY_FROM = new BigDecimal("2000");

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    private OrderService orderService;
    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, cartRepository, cartItemRepository,
                productRepository, userRepository, FREE_DELIVERY_FROM);

        user = new User();
        user.setId(1L);
        user.setUsername("ivan");
        user.setEmail("ivan@mail.md");

        cart = new Cart();
        cart.setId(10L);
    }

    @Nested
    @DisplayName("checkout")
    class Checkout {

        @Test
        void createsOrderWithPriceSnapshotAndClearsCart() {
            Product board = product(1L, "Red Paddle Ride", "15990.00", 5);
            Product leash = product(2L, "Leash", "490.00", 3);
            givenCart(item(board, 1), item(leash, 2));
            givenStockAndSaveSucceed();

            OrderResponse order = orderService.checkout("ivan", request(DeliveryMethod.COURIER_CHISINAU));

            assertThat(order.status()).isEqualTo("NEW");
            assertThat(order.items()).hasSize(2);
            // цена в заказе — снимок на момент покупки, а не ссылка на текущую цену товара
            assertThat(order.items().get(1).priceAtPurchase()).isEqualByComparingTo("490.00");
            assertThat(order.totalPrice()).isEqualByComparingTo("16970.00");
            verify(productRepository).decreaseStock(1L, 1);
            verify(productRepository).decreaseStock(2L, 2);
            verify(cartItemRepository).deleteByCartId(10L);
        }

        @Test
        void chargesDeliveryBelowFreeThreshold() {
            givenCart(item(product(1L, "Leash", "490.00", 3), 1));
            givenStockAndSaveSucceed();

            OrderResponse order = orderService.checkout("ivan", request(DeliveryMethod.PARCEL_LOCKER));

            assertThat(order.deliveryPrice()).isEqualByComparingTo("45.00");
            assertThat(order.totalPrice()).isEqualByComparingTo("535.00");
        }

        // ровно на пороге доставка уже бесплатная: в коде >=, хочу это зафиксировать
        @Test
        void deliveryIsFreeFromThresholdInclusive() {
            givenCart(item(product(1L, "Pump", "1000.00", 5), 2));
            givenStockAndSaveSucceed();

            OrderResponse order = orderService.checkout("ivan", request(DeliveryMethod.PARCEL_LOCKER));

            assertThat(order.deliveryPrice()).isEqualByComparingTo("0");
            assertThat(order.totalPrice()).isEqualByComparingTo("2000.00");
        }

        @Test
        void pickupDoesNotStoreAddress() {
            givenCart(item(product(1L, "Leash", "490.00", 3), 1));
            givenStockAndSaveSucceed();

            OrderResponse order = orderService.checkout("ivan", request(DeliveryMethod.PICKUP));

            assertThat(order.city()).isNull();
            assertThat(order.shippingAddress()).isNull();
        }

        @Test
        void emptyCartIsRejected() {
            givenUserAndCart();
            when(cartItemRepository.findByCartId(10L)).thenReturn(List.of());

            assertKey(() -> orderService.checkout("ivan", request(DeliveryMethod.PICKUP)),
                    BadRequestException.class, "error.cart.empty");
            verify(orderRepository, never()).save(any());
        }

        @Test
        void discontinuedProductIsRejected() {
            Product old = product(1L, "Old board", "9990.00", 5);
            old.setStatus("DISCONTINUED");
            givenCart(item(old, 1));

            assertKey(() -> orderService.checkout("ivan", request(DeliveryMethod.PICKUP)),
                    BadRequestException.class, "error.product.discontinued");
            verify(productRepository, never()).decreaseStock(any(), any(Integer.class));
            verify(orderRepository, never()).save(any());
        }

        @Test
        void notEnoughStockIsRejectedBeforeTouchingStock() {
            givenCart(item(product(1L, "All Star", "81890.00", 1), 2));

            assertKey(() -> orderService.checkout("ivan", request(DeliveryMethod.PICKUP)),
                    BadRequestException.class, "error.stock.notEnoughOf");
            verify(productRepository, never()).decreaseStock(any(), any(Integer.class));
        }

        // проверка прошла, но между ней и UPDATE кто-то купил последний товар: UPDATE изменил 0 строк.
        // откат уже списанного делает @Transactional, это проверю в интеграционном тесте
        @Test
        void lostRaceForLastItemIsRejectedAndCartStays() {
            givenCart(item(product(1L, "All Star", "81890.00", 1), 1));
            when(productRepository.decreaseStock(1L, 1)).thenReturn(0);

            assertKey(() -> orderService.checkout("ivan", request(DeliveryMethod.PICKUP)),
                    BadRequestException.class, "error.stock.notEnoughShort");
            verify(orderRepository, never()).save(any());
            verify(cartItemRepository, never()).deleteByCartId(any());
        }

        @Test
        void unknownUserIsNotFound() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertKey(() -> orderService.checkout("ghost", request(DeliveryMethod.PICKUP)),
                    NotFoundException.class, "error.user.notFound");
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        void cancellingReturnsItemsToStock() {
            Order order = order(Order.Status.PAID, orderItem(product(1L, "Board", "100", 0), 2));
            when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            OrderResponse response = orderService.updateStatus(5L, Order.Status.CANCELLED);

            assertThat(response.status()).isEqualTo("CANCELLED");
            verify(productRepository).increaseStock(1L, 2);
        }

        @Test
        void shippingDoesNotTouchStock() {
            Order order = order(Order.Status.PAID, orderItem(product(1L, "Board", "100", 0), 2));
            when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            orderService.updateStatus(5L, Order.Status.SHIPPED);

            verify(productRepository, never()).increaseStock(any(), any(Integer.class));
        }

        @Test
        void forbiddenTransitionIsRejected() {
            Order order = order(Order.Status.COMPLETED);
            when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

            assertKey(() -> orderService.updateStatus(5L, Order.Status.NEW),
                    BadRequestException.class, "error.order.statusChange");
            verify(orderRepository, never()).save(any());
        }

        @Test
        void unknownOrderIsNotFound() {
            when(orderRepository.findById(404L)).thenReturn(Optional.empty());

            assertKey(() -> orderService.updateStatus(404L, Order.Status.PAID),
                    NotFoundException.class, "error.order.notFound");
        }
    }

    // --- данные для тестов

    private void givenUserAndCart() {
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
    }

    private void givenCart(CartItem... items) {
        givenUserAndCart();
        when(cartItemRepository.findByCartId(10L)).thenReturn(List.of(items));
        for (CartItem item : items) {
            when(productRepository.findById(item.getProduct().getId())).thenReturn(Optional.of(item.getProduct()));
        }
    }

    private void givenStockAndSaveSucceed() {
        when(productRepository.decreaseStock(any(), any(Integer.class))).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Product product(Long id, String name, String price, int quantity) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(quantity);
        product.setStatus("ACTIVE");
        return product;
    }

    private CartItem item(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private Order order(Order.Status status, OrderItem... items) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(status);
        for (OrderItem item : items) {
            item.setOrder(order);
            order.getItems().add(item);
        }
        return order;
    }

    private static OrderItem orderItem(Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setQuantity(quantity);
        item.setPriceAtPurchase(product.getPrice());
        return item;
    }

    private static CheckoutRequest request(DeliveryMethod delivery) {
        return new CheckoutRequest("Ion", "Munteanu", "+37369000000", "ion@mail.md",
                delivery, "Chișinău", "str. Albișoara 4", PaymentMethod.CASH_ON_DELIVERY, null);
    }
}

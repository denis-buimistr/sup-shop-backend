package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.model.*;
import com.supshop.sup_shop_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order checkout(String username, String shippingAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Корзина не найдена"));

        List<CartItem> cartItems = cartItemRepository
                .findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setStatus(Order.Status.NEW);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = productRepository
                    .findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Товар не найден"));

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Недостаточно товара на складе: "
                                + product.getName()
                                + ". Доступно: " + product.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            order.getItems().add(orderItem);

            product.setQuantity(
                    product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            total = total.add(
                    product.getPrice().multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteByCartId(cart.getId());

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "Пользователь не найден"));

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public Order updateStatus(Long orderId, Order.Status newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Заказ не найден: " + orderId));

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    //for ADMIN
    @Transactional(readOnly = true)
    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }
}
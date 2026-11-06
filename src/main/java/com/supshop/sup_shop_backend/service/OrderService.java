package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.order.CheckoutRequest;
import com.supshop.sup_shop_backend.dto.order.OrderItemResponse;
import com.supshop.sup_shop_backend.dto.order.OrderResponse;
import com.supshop.sup_shop_backend.model.*;
import com.supshop.sup_shop_backend.repository.*;
import com.supshop.sup_shop_backend.exception.NotFoundException;
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

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse checkout(String username, CheckoutRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("Корзина не найдена"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        DeliveryMethod delivery = request.deliveryMethod();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.Status.NEW);
        order.setFirstName(request.firstName());
        order.setLastName(request.lastName());
        order.setPhone(request.phone());
        order.setEmail(request.email());
        order.setDeliveryMethod(delivery);
        order.setPaymentMethod(request.paymentMethod());
        order.setComment(request.comment());
        order.setDeliveryPrice(delivery.getPrice());
        // при самовывозе адрес не храним, даже если фронт его прислал
        if (delivery.requiresAddress()) {
            order.setCity(request.city());
            order.setShippingAddress(request.shippingAddress());
        }

        BigDecimal total = delivery.getPrice();

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId()).orElseThrow(() -> new NotFoundException("Товар не найден"));
            if (!"ACTIVE".equals(product.getStatus())) {
                throw new IllegalArgumentException("Товар снят с продажи: " + product.getName() + ". Удалите его из корзины.");
            }

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Недостаточно товара на складе: " + product.getName() + ". Доступно: " + product.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            order.getItems().add(orderItem);

            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteByCartId(cart.getId());

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, Order.Status newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Заказ не найден: " + orderId));

        if (!order.getStatus().canChangeTo(newStatus)) {
            throw new IllegalArgumentException("Нельзя сменить статус заказа с " + order.getStatus() + " на " + newStatus);
        }

        if (newStatus == Order.Status.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    //for ADMIN
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrder() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    //Return DTO, not the entity: Order <-> OrderItem loops in jackson and would leak user
    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream().map(this::toItemResponse).toList();
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getUser().getUsername(),
                order.getUser().getEmail(),
                order.getFirstName(),
                order.getLastName(),
                order.getPhone(),
                order.getEmail(),
                order.getDeliveryMethod(),
                order.getCity(),
                order.getShippingAddress(),
                order.getDeliveryPrice(),
                order.getPaymentMethod(),
                order.getComment(),
                order.getTotalPrice(),
                items,
                order.getCreatedAt());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPriceAtPurchase());
    }
}
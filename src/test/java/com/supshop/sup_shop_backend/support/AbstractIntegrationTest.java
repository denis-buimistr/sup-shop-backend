package com.supshop.sup_shop_backend.support;

import com.supshop.sup_shop_backend.model.Cart;
import com.supshop.sup_shop_backend.model.CartItem;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.CartItemRepository;
import com.supshop.sup_shop_backend.repository.CartRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import com.supshop.sup_shop_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.UUID;

// Всё приложение целиком на Postgres из Testcontainers. контекст и контейнер Spring кэширует,
// поэтому база поднимается один раз на все наследники этого класса.
// данные не чищу, вместо этого у каждого теста уникальные username и slug
@SpringBootTest
@Import(PostgresContainerConfig.class)
public abstract class AbstractIntegrationTest {

    @Autowired protected UserRepository userRepository;
    @Autowired protected ProductRepository productRepository;
    @Autowired protected CartRepository cartRepository;
    @Autowired protected CartItemRepository cartItemRepository;

    protected User newUser() {
        String name = "user-" + shortId();
        User user = new User();
        user.setUsername(name);
        user.setEmail(name + "@test.md");
        user.setPasswordHash("not-used-in-tests");
        return userRepository.save(user);
    }

    protected Product newProduct(int quantity, String price) {
        String slug = "test-product-" + shortId();
        Product product = new Product();
        product.setName(slug);
        product.setSlug(slug);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(quantity);
        return productRepository.save(product);
    }

    protected void putInCart(User user, Product product, int quantity) {
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart created = new Cart();
            created.setUser(user);
            return cartRepository.save(created);
        });
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    protected int stockOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getQuantity();
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

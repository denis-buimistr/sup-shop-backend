package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.cart.CartItemResponse;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.model.Cart;
import com.supshop.sup_shop_backend.model.CartItem;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.CartItemRepository;
import com.supshop.sup_shop_backend.repository.CartRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import com.supshop.sup_shop_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.supshop.sup_shop_backend.support.AppExceptionAssert.assertKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    private CartService cartService;
    private User user;
    private Cart cart;
    private Product board;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, cartItemRepository, productRepository, userRepository);

        user = new User();
        user.setId(1L);
        user.setUsername("ivan");

        cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);

        board = new Product();
        board.setId(7L);
        board.setName("Red Paddle Ride");
        board.setPrice(new BigDecimal("15990.00"));
        board.setQuantity(3);
        board.setStatus("ACTIVE");
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        void addsNewProductToExistingCart() {
            givenUser();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.empty());
            givenProductAndSaveSucceeds();

            CartItemResponse response = cartService.addItem("ivan", 7L, 2);

            assertThat(response.quantity()).isEqualTo(2);
            assertThat(response.subtotal()).isEqualByComparingTo("31980.00");
            verify(cartRepository, never()).save(any());
        }

        @Test
        void createsCartOnFirstAdd() {
            givenUser();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.empty());
            givenProductAndSaveSucceeds();

            cartService.addItem("ivan", 7L, 1);

            // новая корзина должна принадлежать именно этому пользователю
            ArgumentCaptor<Cart> saved = ArgumentCaptor.forClass(Cart.class);
            verify(cartRepository).save(saved.capture());
            assertThat(saved.getValue().getUser()).isSameAs(user);
        }

        @Test
        void addsToQuantityAlreadyInCart() {
            givenUser();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.of(cartItem(1)));
            givenProductAndSaveSucceeds();

            CartItemResponse response = cartService.addItem("ivan", 7L, 2);

            assertThat(response.quantity()).isEqualTo(3);
        }

        // на складе 3, в корзине уже 2: ещё 2 положить нельзя, хотя 2 < 3 по отдельности
        @Test
        void countsWhatIsAlreadyInCartAgainstStock() {
            givenUser();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.of(cartItem(2)));
            when(productRepository.findById(7L)).thenReturn(Optional.of(board));

            assertKey(() -> cartService.addItem("ivan", 7L, 2), BadRequestException.class, "error.stock.notEnoughInCart");
            verify(cartItemRepository, never()).save(any());
        }

        @Test
        void inactiveProductIsRejected() {
            board.setStatus("DISCONTINUED");
            givenUser();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.empty());
            when(productRepository.findById(7L)).thenReturn(Optional.of(board));

            assertKey(() -> cartService.addItem("ivan", 7L, 1), BadRequestException.class, "error.product.unavailable");
            verify(cartItemRepository, never()).save(any());
        }

        // нулевое количество отсекаю до любых запросов в базу
        @Test
        void zeroQuantityIsRejectedWithoutDatabase() {
            assertKey(() -> cartService.addItem("ivan", 7L, 0), BadRequestException.class, "error.cart.quantityPositive");
            verifyNoInteractions(userRepository, cartRepository, cartItemRepository, productRepository);
        }
    }

    @Nested
    @DisplayName("updateQuantity")
    class UpdateQuantity {

        @Test
        void zeroRemovesItem() {
            CartItem item = cartItem(2);
            givenUser();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.of(item));

            CartItemResponse response = cartService.updateQuantity("ivan", 7L, 0);

            assertThat(response).isNull();
            verify(cartItemRepository).delete(item);
        }

        @Test
        void moreThanStockIsRejected() {
            givenUser();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.of(cartItem(1)));
            when(productRepository.findById(7L)).thenReturn(Optional.of(board));

            assertKey(() -> cartService.updateQuantity("ivan", 7L, 4), BadRequestException.class, "error.stock.available");
            verify(cartItemRepository, never()).save(any());
        }

        @Test
        void setsNewQuantity() {
            givenUser();
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.of(cartItem(1)));
            givenProductAndSaveSucceeds();

            CartItemResponse response = cartService.updateQuantity("ivan", 7L, 3);

            assertThat(response.quantity()).isEqualTo(3);
        }
    }

    @Test
    void removingProductThatIsNotInCartIsNotFound() {
        givenUser();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 7L)).thenReturn(Optional.empty());

        assertKey(() -> cartService.removeItem("ivan", 7L), NotFoundException.class, "error.cart.itemNotFound");
    }

    // --- данные для тестов

    private void givenUser() {
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(user));
    }

    private void givenProductAndSaveSucceeds() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(board));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private CartItem cartItem(int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(board);
        item.setQuantity(quantity);
        return item;
    }
}

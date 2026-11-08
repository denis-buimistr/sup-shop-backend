package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.wishlist.WishlistResponse;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import com.supshop.sup_shop_backend.repository.UserRepository;
import com.supshop.sup_shop_backend.repository.WishlistItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.supshop.sup_shop_backend.support.AppExceptionAssert.assertKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// сам ON CONFLICT тут не проверить, это SQL. проверяю, что сервис зовёт addAll, а не «проверил, потом вставил»
@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock private WishlistItemRepository wishlistItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    private WishlistService wishlistService;
    private User user;

    @BeforeEach
    void setUp() {
        wishlistService = new WishlistService(wishlistItemRepository, userRepository, productRepository);
        user = new User();
        user.setId(1L);
        user.setUsername("ivan");
    }

    @Test
    void addInsertsWithSingleQuery() {
        givenUser();
        when(productRepository.existsById(5L)).thenReturn(true);

        wishlistService.add("ivan", 5L);

        verify(wishlistItemRepository).addAll(1L, List.of(5L));
    }

    @Test
    void addingUnknownProductIsNotFound() {
        givenUser();
        when(productRepository.existsById(404L)).thenReturn(false);

        assertKey(() -> wishlistService.add("ivan", 404L), NotFoundException.class, "error.product.notFound");
        verify(wishlistItemRepository, never()).addAll(anyLong(), any());
    }

    @Test
    void mergePassesAllGuestIdsAndReturnsFreshList() {
        givenUser();
        when(wishlistItemRepository.findProductIdsByUserId(1L)).thenReturn(List.of(9L, 3L, 5L));

        WishlistResponse response = wishlistService.merge("ivan", List.of(3L, 9L));

        // несуществующие id и дубликаты отсеивает сам SQL, сервис просто передаёт всё одним запросом
        verify(wishlistItemRepository).addAll(1L, List.of(3L, 9L));
        assertThat(response.productIds()).containsExactly(9L, 3L, 5L);
    }

    @Test
    void mergeWithEmptyGuestListDoesNotInsert() {
        givenUser();
        when(wishlistItemRepository.findProductIdsByUserId(1L)).thenReturn(List.of(5L));

        WishlistResponse response = wishlistService.merge("ivan", List.of());

        verify(wishlistItemRepository, never()).addAll(anyLong(), any());
        assertThat(response.productIds()).containsExactly(5L);
    }

    @Test
    void removeDeletesOnlyThisUsersItem() {
        givenUser();

        wishlistService.remove("ivan", 5L);

        verify(wishlistItemRepository).deleteByUserIdAndProductId(1L, 5L);
    }

    @Test
    void unknownUserIsNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertKey(() -> wishlistService.get("ghost"), NotFoundException.class, "error.user.notFound");
    }

    private void givenUser() {
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(user));
    }
}

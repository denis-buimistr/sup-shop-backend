package com.supshop.sup_shop_backend.integration;

import com.supshop.sup_shop_backend.dto.CategoryResponse;
import com.supshop.sup_shop_backend.dto.ProductAttributeResponse;
import com.supshop.sup_shop_backend.dto.ProductFilter;
import com.supshop.sup_shop_backend.dto.ProductResponse;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.repository.CategoryRepository;
import com.supshop.sup_shop_backend.service.CategoryService;
import com.supshop.sup_shop_backend.service.ProductService;
import com.supshop.sup_shop_backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

// переводы из миграции V16 и JPQL-запросы TranslationService на настоящей базе.
// в запросе язык ставит LocaleResolver, а тут ставлю его руками через LocaleContextHolder
class TranslationIntegrationTest extends AbstractIntegrationTest {

    private static final String GENERATION = "starboard-generation-12-6-x-28";

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private CategoryRepository categoryRepository;

    @AfterEach
    void resetLanguage() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void productComesInRussian() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ru"));

        ProductResponse product = productService.findBySlug(GENERATION);

        // название модели не переводится, остальное да
        assertThat(product.name()).isEqualTo("Starboard Generation 12'6\" x 28\"");
        assertThat(product.description()).startsWith("Туринг, гонки и сёрфинг");
        assertThat(product.categoryName()).isEqualTo("Жёсткие доски");
        assertThat(product.attributes()).extracting(ProductAttributeResponse::name).contains("Длина", "Ширина");
    }

    // язык ставлю явно: без запроса LocaleContextHolder вернул бы язык системы, а на Mac это может быть русский
    @Test
    void englishStaysOriginal() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        ProductResponse product = productService.findBySlug(GENERATION);

        assertThat(product.categoryName()).isEqualTo("Composite Boards");
        assertThat(product.attributes()).extracting(ProductAttributeResponse::name).contains("Length");
    }

    @Test
    void productWithoutTranslationFallsBackToEnglish() {
        Product fresh = newProduct(1, "100");
        fresh.setDescription("Only in English for now");
        productRepository.save(fresh);
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ro"));

        ProductResponse product = productService.findBySlug(fresh.getSlug());

        assertThat(product.description()).isEqualTo("Only in English for now");
    }

    @Test
    void categoriesComeInRomanian() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ro"));

        assertThat(categoryService.getAll())
                .filteredOn(c -> c.slug().equals("foil-wing"))
                .extracting(CategoryResponse::name)
                .containsExactly("Foil și Wing");
    }

    // поиск смотрит в переводы названий категорий. свой товар создаю сам и ищу только среди него (фильтр id),
    // иначе тест зависел бы от того, сколько вёсел сейчас лежит в демо-данных
    @Test
    void searchMatchesTranslatedCategoryName() {
        Product paddle = newProduct(1, "100");
        paddle.setCategory(categoryRepository.findAll().stream()
                .filter(c -> c.getSlug().equals("paddles")).findFirst().orElseThrow());
        productRepository.save(paddle);
        Product pump = newProduct(1, "100");

        assertThat(search("вёсла", paddle)).isEqualTo(1);
        assertThat(search("vâsle", paddle)).isEqualTo(1);
        assertThat(search("вёсла", pump)).isZero();
    }

    private long search(String text, Product onlyAmong) {
        ProductFilter filter = new ProductFilter(null, null, null, null, null, null, null, text, List.of(onlyAmong.getId()));
        return productService.findAll(filter, PageRequest.of(0, 20)).getTotalElements();
    }
}

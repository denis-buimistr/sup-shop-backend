package com.supshop.sup_shop_backend;

import com.supshop.sup_shop_backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

// контекст поднимается на Postgres из контейнера: заодно проверяю, что все миграции накатываются
// на пустую базу, а Hibernate (ddl-auto: validate) согласен со схемой
class SupShopBackendApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}

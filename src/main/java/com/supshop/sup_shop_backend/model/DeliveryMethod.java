package com.supshop.sup_shop_backend.model;

import java.math.BigDecimal;
    public enum DeliveryMethod {
        COURIER_CHISINAU(BigDecimal.ZERO, true),
        COURIER_MOLDOVA(BigDecimal.ZERO, true),
        PICKUP(BigDecimal.ZERO, false),
        PARCEL_LOCKER(new BigDecimal("45.00"), true);

        private final BigDecimal price;
        private final boolean requiresAddress;

        DeliveryMethod(BigDecimal price, boolean requiresAddress) {
            this.price = price;
            this.requiresAddress = requiresAddress;
        }

        public BigDecimal getPrice() {return price;}
        public boolean requiresAddress() {return requiresAddress;}
    }


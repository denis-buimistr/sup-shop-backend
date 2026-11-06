package com.supshop.sup_shop_backend.dto.order;

import com.supshop.sup_shop_backend.model.DeliveryMethod;
import com.supshop.sup_shop_backend.model.PaymentMethod;
import jakarta.validation.constraints.*;

public record CheckoutRequest(
        @NotBlank(message = "{checkout.firstName.required}")
        @Size(max = 100, message = "{checkout.firstName.size}")
        String firstName,

        @NotBlank(message = "{checkout.lastName.required}")
        @Size(max = 100, message = "{checkout.lastName.size}")
        String lastName,

        @NotBlank(message = "{checkout.phone.required}")
        @Pattern(regexp = "^\\+?[0-9 ()-]{6,20}$", message = "{checkout.phone.invalid}")
        String phone,

        @NotBlank(message = "{checkout.email.required}")
        @Email(message = "{checkout.email.invalid}")
        @Size(max = 250, message = "{checkout.email.size}")
        String email,

        @NotNull(message = "{checkout.deliveryMethod.required}")
        DeliveryMethod deliveryMethod,

        @Size(max = 100, message = "{checkout.city.size}")
        String city,

        @Size(max = 500, message = "{checkout.shippingAddress.size}")
        String shippingAddress,

        @NotNull(message = "{checkout.paymentMethod.required}")
        PaymentMethod paymentMethod,

        @Size(max = 500, message = "{checkout.comment.size}")
        String comment
) {
    @AssertTrue(message = "{checkout.address.required}")
    public boolean isAddressProvidedIfRequired() {
        if (deliveryMethod == null || !deliveryMethod.requiresAddress()) {
            return true;
        }
        return city != null && !city.isBlank()
                && shippingAddress != null && !shippingAddress.isBlank();
    }
}
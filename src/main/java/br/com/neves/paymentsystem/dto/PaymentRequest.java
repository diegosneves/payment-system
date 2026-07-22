package br.com.neves.paymentsystem.dto;

import br.com.neves.paymentsystem.enums.PaymentSource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @NotNull(message = "Payer ID is required")
        UUID payerId,
        @NotNull(message = "Payment source is required")
        PaymentSource paymentSource,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {

    public static PaymentRequest create(
            final UUID payerId,
            final PaymentSource paymentSource,
            final BigDecimal amount
    ) {
        return new PaymentRequest(payerId, paymentSource, amount);
    }

}

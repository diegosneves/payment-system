package br.com.neves.paymentsystem.dto;

import br.com.neves.paymentsystem.enums.PaymentSource;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.model.Payment;

import java.math.BigDecimal;
import java.util.UUID;


public record PaymentResponse(
        Long id,
        UUID payerId,
        PaymentSource paymentSource,
        BigDecimal amount,
        PaymentStatus status
) {

    public static PaymentResponse create(
            final Long id,
            final UUID payerId,
            final PaymentSource paymentSource,
            final BigDecimal amount,
            final PaymentStatus status
    ) {
        return new PaymentResponse(id, payerId, paymentSource, amount, status);
    }

    public static PaymentResponse from(final Payment payment) {
        return create(
                payment.getId(),
                payment.getPayerId(),
                payment.getPaymentSource(),
                payment.getAmount(),
                payment.getStatus()
        );
    }

}

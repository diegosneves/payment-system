package br.com.neves.paymentsystem.utils;

import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.enums.PaymentSource;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Fixture {

    private static final UUID DEFAULT_PAYER_ID = UUID.fromString("739e5f1e-2459-499d-9cdd-ab59b31c02d9");

    private Fixture() {
    }


    public static final class PaymentRequests {

        public static PaymentRequest createPixRequest() {
            return PaymentRequest.create(DEFAULT_PAYER_ID, PaymentSource.PIX, new BigDecimal("100.00"));
        }

        public static PaymentRequest createPixRequestWithRandomPayerId() {
            return PaymentRequest.create(UUID.randomUUID(), PaymentSource.PIX, new BigDecimal("100.00"));
        }

        public static PaymentRequest createPixRequestWithCustomAmount(final BigDecimal amount) {
            return PaymentRequest.create(DEFAULT_PAYER_ID, PaymentSource.PIX, amount);
        }

        public static PaymentRequest createPixRequestWithRandomPayerIdAndWithCustomAmount(final BigDecimal amount) {
            return PaymentRequest.create(UUID.randomUUID(), PaymentSource.PIX, amount);
        }

        public static PaymentRequest createDebitCardRequest() {
            return PaymentRequest.create(DEFAULT_PAYER_ID, PaymentSource.DEBIT_CARD, new BigDecimal("200.00"));
        }

        public static PaymentRequest createCreditCardRequest() {
            return PaymentRequest.create(DEFAULT_PAYER_ID, PaymentSource.CREDIT_CARD, new BigDecimal("300.00"));
        }

    }

    public final static class Payments {

        public static Payment createSamplePixPaymentDataWithStatusPending(final Long paymentId) {
            return Payment.builder()
                    .id(paymentId)
                    .payerId(DEFAULT_PAYER_ID)
                    .status(PaymentStatus.PENDING)
                    .paymentSource(PaymentSource.PIX)
                    .amount(new BigDecimal("100.00"))
                    .createdAt(Instant.now())
                    .build();
        }

        public static Payment createSamplePixPaymentDataWithStatusPending() {
            return createSamplePixPaymentDataWithStatusPending(1L);
        }

        public static Payment createSampleCreditCardPaymentDataWithStatusPending(final Long paymentId) {
            return Payment.builder()
                    .id(paymentId)
                    .payerId(DEFAULT_PAYER_ID)
                    .status(PaymentStatus.PENDING)
                    .paymentSource(PaymentSource.CREDIT_CARD)
                    .amount(new BigDecimal("100.00"))
                    .createdAt(Instant.now())
                    .build();
        }

        public static Payment createSampleCreditCardPaymentDataWithStatusPending() {
            return createSampleCreditCardPaymentDataWithStatusPending(2L);
        }

        public static Payment createSamplePixPaymentDataWithStatusPaid() {
            return Payment.builder()
                    .id(3L)
                    .payerId(DEFAULT_PAYER_ID)
                    .status(PaymentStatus.PAID)
                    .paymentSource(PaymentSource.PIX)
                    .amount(new BigDecimal("100.00"))
                    .createdAt(Instant.now())
                    .build();
        }
    }

}

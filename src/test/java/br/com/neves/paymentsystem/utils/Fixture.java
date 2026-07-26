package br.com.neves.paymentsystem.utils;

import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.enums.PaymentSource;

import java.math.BigDecimal;
import java.util.UUID;

public final class Fixture {

    private Fixture() {

    }


    public static final class PaymentRequests {

        public static PaymentRequest createPixRequest() {
            return PaymentRequest.create(UUID.randomUUID(), PaymentSource.PIX, new BigDecimal("100.00"));
        }

        public static PaymentRequest createPixRequestWithAmount(final BigDecimal amount) {
            return PaymentRequest.create(UUID.randomUUID(), PaymentSource.PIX, amount);
        }

        public static PaymentRequest createDebitCardRequest() {
            return PaymentRequest.create(UUID.randomUUID(), PaymentSource.DEBIT_CARD, new BigDecimal("200.00"));
        }

        public static PaymentRequest createCreditCardRequest() {
            return PaymentRequest.create(UUID.randomUUID(), PaymentSource.CREDIT_CARD, new BigDecimal("300.00"));
        }

    }

}

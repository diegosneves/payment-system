package br.com.neves.paymentsystem.validators;

import br.com.neves.paymentsystem.exceptions.ErrorData;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;

import java.math.BigDecimal;

public final class PaymentLimitValidator {

    public static final BigDecimal MAX_PAYMENT_LIMIT = new BigDecimal("2000.00");

    private PaymentLimitValidator() {

    }

    public static Boolean isWithinLimit(final BigDecimal amount) throws PaymentLimitException {
        Boolean result = Boolean.FALSE;
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw PaymentLimitException.with(new ErrorData("Amount must be greater than zero"));
            }
            result = amount.compareTo(MAX_PAYMENT_LIMIT) <= 0;
        }

        return result;
    }
}

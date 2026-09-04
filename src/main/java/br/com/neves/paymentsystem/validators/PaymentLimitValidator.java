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
            validateAmount(amount);
            result = amount.compareTo(MAX_PAYMENT_LIMIT) <= 0;
        }
        return result;
    }

    public static void validateAmount(final BigDecimal amount) throws PaymentLimitException {
        if (isZeroOrNegativeAmount(amount)) {
            throw PaymentLimitException.with(new ErrorData("Amount must be greater than zero"));
        }
    }

    private static Boolean isZeroOrNegativeAmount(final BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) <= 0;
    }
}

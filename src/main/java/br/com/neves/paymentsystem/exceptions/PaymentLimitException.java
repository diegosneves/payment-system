package br.com.neves.paymentsystem.exceptions;

import java.util.List;

public class PaymentLimitException extends DomainException {

    private static final String DEFAULT_MESSAGE = "Payment limit exceeded";

    protected PaymentLimitException(final String message, final List<ErrorData> errors) {
        super(message, errors);
    }

    public static PaymentLimitException with(final ErrorData error) {
        return new PaymentLimitException(error.message(), List.of(error));
    }

    public static PaymentLimitException with(final List<ErrorData> errors) {
        return new PaymentLimitException(DEFAULT_MESSAGE, errors);
    }

    @Override
    public List<ErrorData> getErrors() {
        return super.getErrors();
    }
}

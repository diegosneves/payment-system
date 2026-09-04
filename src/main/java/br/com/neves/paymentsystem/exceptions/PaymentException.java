package br.com.neves.paymentsystem.exceptions;

import java.util.List;

public class PaymentException extends DomainException {

    private static final String DEFAULT_MESSAGE = "Payment constraint violation";

    protected PaymentException(final String message, final List<ErrorData> errors) {
        super(message, errors);
    }

    public static PaymentException with(final ErrorData error) {
        return new PaymentException(error.message(), List.of(error));
    }

    public static PaymentException with(final List<ErrorData> errors) {
        return new PaymentException(DEFAULT_MESSAGE, errors);
    }

    @Override
    public List<ErrorData> getErrors() {
        return super.getErrors();
    }
}

package br.com.neves.paymentsystem.exceptions;

import java.util.List;

public class PaymentNotFoundException extends DomainException {

    private static final String DEFAULT_MESSAGE = "Payment not found";

    protected PaymentNotFoundException(final String message, final List<ErrorData> errors) {
        super(message, errors);
    }

    public static PaymentNotFoundException with(final ErrorData error) {
        return new PaymentNotFoundException(error.message(), List.of(error));
    }

    public static PaymentNotFoundException with(final List<ErrorData> errors) {
        return new PaymentNotFoundException(DEFAULT_MESSAGE, errors);
    }

    @Override
    public List<ErrorData> getErrors() {
        return super.getErrors();
    }
}

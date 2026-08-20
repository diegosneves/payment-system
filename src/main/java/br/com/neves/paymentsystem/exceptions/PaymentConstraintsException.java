package br.com.neves.paymentsystem.exceptions;

import java.util.List;

public class PaymentConstraintsException extends DomainException {

    private static final String DEFAULT_MESSAGE = "Payment constraint violation";

    protected PaymentConstraintsException(final String message, final List<ErrorData> errors) {
        super(message, errors);
    }

    public static PaymentConstraintsException with(final ErrorData error) {
        return new PaymentConstraintsException(error.message(), List.of(error));
    }

    public static PaymentConstraintsException with(final List<ErrorData> errors) {
        return new PaymentConstraintsException(DEFAULT_MESSAGE, errors);
    }

    @Override
    public List<ErrorData> getErrors() {
        return super.getErrors();
    }
}

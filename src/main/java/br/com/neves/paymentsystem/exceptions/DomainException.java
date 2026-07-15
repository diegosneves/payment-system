package br.com.neves.paymentsystem.exceptions;


import java.util.List;

public class DomainException extends RuntimeException{

    protected final List<ErrorData> errors;

    protected DomainException(final String message, final List<ErrorData> errors) {
        super(message);
        this.errors = errors;
    }

    public static DomainException with(final ErrorData anErrors) {
        return new DomainException(anErrors.message(), List.of(anErrors));
    }

    public static DomainException with(final List<ErrorData> anErrors) {
        return new DomainException("", anErrors);
    }

    public List<ErrorData> getErrors() {
        return this.errors;
    }

}

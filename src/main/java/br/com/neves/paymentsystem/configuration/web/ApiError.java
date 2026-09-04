package br.com.neves.paymentsystem.configuration.web;


import br.com.neves.paymentsystem.exceptions.DomainException;
import br.com.neves.paymentsystem.exceptions.ErrorData;

import java.util.List;

public record ApiError(String message, List<ErrorData> errors) {

    public static ApiError from(final DomainException exception) {
        return new ApiError(exception.getMessage(), exception.getErrors());
    }

    public static ApiError from(final String exceptionMessage) {
        return new ApiError(exceptionMessage, List.of());
    }

}

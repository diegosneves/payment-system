package br.com.neves.paymentsystem.configuration.web;

import br.com.neves.paymentsystem.exceptions.DomainException;
import br.com.neves.paymentsystem.exceptions.PaymentException;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import br.com.neves.paymentsystem.exceptions.PaymentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * A classe {@link GlobalExceptionHandler} é um manipulador de exceções global para controladores.
 * Ela lida com as exceções lançadas durante o processamento de solicitações e gera respostas de erro apropriadas.
 * A classe é anotada com {@link RestControllerAdvice} para aplicar o tratamento de exceção globalmente
 * a todas as classes de controlador.
 *
 * @author diegosneves
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleFailures(final DomainException exception) {
        return ResponseEntity.unprocessableEntity().body(ApiError.from(exception));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiError> handlePaymentNotFoundException(final PaymentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.from(exception));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiError> handlePaymentException(final PaymentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.from(exception));
    }

    @ExceptionHandler(PaymentLimitException.class)
    public ResponseEntity<ApiError> handlePaymentLimitException(final PaymentLimitException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiError.from(exception));
    }

}

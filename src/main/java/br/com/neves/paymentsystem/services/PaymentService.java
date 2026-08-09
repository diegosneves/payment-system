package br.com.neves.paymentsystem.services;

import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.exceptions.ErrorData;
import br.com.neves.paymentsystem.exceptions.PaymentException;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import br.com.neves.paymentsystem.exceptions.PaymentNotFoundException;
import br.com.neves.paymentsystem.model.Payment;
import br.com.neves.paymentsystem.repository.PaymentRepository;
import br.com.neves.paymentsystem.utils.DataConverter;
import br.com.neves.paymentsystem.validators.PaymentLimitValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;

    @Transactional
    public PaymentResponse createPayment(final PaymentRequest request) {
        checkDailyLimit(request);

        final var newPayment = Payment.builder()
                .payerId(request.payerId())
                .paymentSource(request.paymentSource())
                .amount(request.amount())
                .status(PaymentStatus.PENDING)
                .build();

        final var savedPayment = repository.save(newPayment);
        log.info("Payment created with id {}", savedPayment.getId());

        return PaymentResponse.from(savedPayment);
    }

    private void checkDailyLimit(final PaymentRequest request) {
        PaymentLimitValidator.validateAmount(request.amount());

        final LocalDate today = DataConverter.BRAZIL.toLocalDateNow();
        final Instant startOfDay = DataConverter.BRAZIL.toInstant(today);
        final Instant endOfDay = DataConverter.BRAZIL.toInstant(today.plusDays(1));

        var dailyTotal = this.repository.sumPaymentsByPayerIdAndDate(
                request.payerId(),
                startOfDay,
                endOfDay
        );

        if (dailyTotal == null) {
            dailyTotal = BigDecimal.ZERO;
        }

        final var newDailyTotal = dailyTotal.add(request.amount());
        if (!PaymentLimitValidator.isWithinLimit(newDailyTotal)) {
            throw PaymentLimitException.with(
                    new ErrorData("Daily payment limit exceeded for source: %s".formatted(request.paymentSource()))
            );
        }

    }

    public PaymentResponse retrievePaymentDataById(final Long paymentId) {
        final Payment paymentFound = this.repository.findById(paymentId).orElseThrow(
                () -> PaymentNotFoundException.with(new ErrorData("Payment ID: %s is not found".formatted(paymentId)))
        );
        return PaymentResponse.from(paymentFound);
    }

    public List<PaymentResponse> retrieveAllPayments() {
        return this.repository.findAll()
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    public List<PaymentResponse> retrievePaymentByPayerId(final String payerId) {
        final UUID payerIdAsUUID = validateAndGetUuidByString(payerId);
        return this.repository.findAllByPayerId(payerIdAsUUID)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    private static UUID validateAndGetUuidByString(final String payerId) {
        try {
            return UUID.fromString(payerId);
        } catch (IllegalArgumentException _) {
            throw PaymentException.with(new ErrorData("Invalid UUID format for payer ID: %s".formatted(payerId)));
        } catch (NullPointerException _) {
            throw PaymentException.with(new ErrorData("UUID must not be null"));
        }
    }
}

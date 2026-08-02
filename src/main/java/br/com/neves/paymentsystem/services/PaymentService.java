package br.com.neves.paymentsystem.services;

import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.exceptions.ErrorData;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import br.com.neves.paymentsystem.exceptions.PaymentNotFoundException;
import br.com.neves.paymentsystem.model.Payment;
import br.com.neves.paymentsystem.repository.PaymentRepository;
import br.com.neves.paymentsystem.utils.DataConverter;
import br.com.neves.paymentsystem.validators.PaymentLimitValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
}

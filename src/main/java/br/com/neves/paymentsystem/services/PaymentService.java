package br.com.neves.paymentsystem.services;

import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.exceptions.ErrorData;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import br.com.neves.paymentsystem.model.Payment;
import br.com.neves.paymentsystem.repository.PaymentRepository;
import br.com.neves.paymentsystem.validators.PaymentLimitValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;

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
        final LocalDate today = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        final LocalDateTime startOfDay = today.atStartOfDay();
        final LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

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
}

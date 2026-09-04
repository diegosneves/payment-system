package br.com.neves.paymentsystem.dto;

import br.com.neves.paymentsystem.enums.PaymentStatus;

public record UpdatePaymentStatusRequest(
        PaymentStatus status
) {
}

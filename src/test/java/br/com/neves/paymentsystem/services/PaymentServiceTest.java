package br.com.neves.paymentsystem.services;

import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import br.com.neves.paymentsystem.repository.PaymentRepository;
import br.com.neves.paymentsystem.utils.Fixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService service;

    @Mock
    private PaymentRepository repository;

    @Captor
    private ArgumentCaptor<UUID> payerCaptor;

    @Test
    void shouldSavePaymentWhenLimitIsNotExceeded() {
        final var request = Fixture.PaymentRequests.createPixRequest();
        when(this.repository.sumPaymentsByPayerIdAndDate(any(), any(), any())).thenReturn(new BigDecimal("200.00"));
        when(this.repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final PaymentResponse actual = this.service.createPayment(request);

        verify(this.repository, times(1)).save(any());
        verify(this.repository, times(1)).sumPaymentsByPayerIdAndDate(payerCaptor.capture(), any(), any());

        assertThat(payerCaptor.getValue())
                .isNotNull()
                .isEqualTo(request.payerId());

        assertThat(actual)
                .isNotNull()
                .satisfies(response -> {
                    assertThat(response.payerId()).isEqualTo(request.payerId());
                    assertThat(response.amount()).isEqualByComparingTo(request.amount());
                    assertThat(response.paymentSource()).isEqualTo(request.paymentSource());
                });
    }

    @Test
    void shouldThrowExceptionWhenLimitIsExceeded() {
        final var request = Fixture.PaymentRequests.createPixRequest();
        when(this.repository.sumPaymentsByPayerIdAndDate(any(), any(), any())).thenReturn(new BigDecimal("1901.00"));

        assertThatThrownBy(() -> this.service.createPayment(request))
                .isNotNull()
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Daily payment limit exceeded for source");

        verify(this.repository, times(1)).sumPaymentsByPayerIdAndDate(any(), any(), any());
        verify(this.repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenRequestAmountIsZero() {
        final var request = Fixture.PaymentRequests.createPixRequestWithAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> this.service.createPayment(request))
                .isNotNull()
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Amount must be greater than zero");

        verify(this.repository, never()).sumPaymentsByPayerIdAndDate(any(), any(), any());
        verify(this.repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenRequestAmountIsNegative() {
        final var request = Fixture.PaymentRequests.createPixRequestWithAmount(new BigDecimal(-1));

        assertThatThrownBy(() -> this.service.createPayment(request))
                .isNotNull()
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Amount must be greater than zero");

        verify(this.repository, never()).sumPaymentsByPayerIdAndDate(any(), any(), any());
        verify(this.repository, never()).save(any());
    }

}

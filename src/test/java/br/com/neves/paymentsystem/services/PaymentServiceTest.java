package br.com.neves.paymentsystem.services;

import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.exceptions.PaymentException;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import br.com.neves.paymentsystem.exceptions.PaymentNotFoundException;
import br.com.neves.paymentsystem.model.Payment;
import br.com.neves.paymentsystem.repository.PaymentRepository;
import br.com.neves.paymentsystem.utils.DataConverter;
import br.com.neves.paymentsystem.utils.Fixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    @DisplayName("should save payment when limit is not exceeded")
    void shouldSavePaymentWhenLimitIsNotExceeded() {
        final var request = Fixture.PaymentRequests.createPixRequest();
        final var expectedPaymentId = 1L;
        final var expectedCreatedAt = Instant.now();
        when(this.repository.sumPaymentsByPayerIdAndDate(any(), any(), any())).thenReturn(new BigDecimal("200.00"));
        when(this.repository.save(any())).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(expectedPaymentId);
            payment.setCreatedAt(expectedCreatedAt);
            return payment;
        });

        final PaymentResponse actual = this.service.createPayment(request);

        verify(this.repository, times(1)).save(any());
        verify(this.repository, times(1)).sumPaymentsByPayerIdAndDate(payerCaptor.capture(), any(), any());

        assertThat(payerCaptor.getValue())
                .isNotNull()
                .isEqualTo(request.payerId());

        assertThat(actual)
                .withFailMessage("Response should not be null")
                .isNotNull()
                .satisfies(response -> {
                    assertThat(response.payerId()).isNotNull().isEqualTo(request.payerId());
                    assertThat(response.amount()).isNotNull().isEqualByComparingTo(request.amount());
                    assertThat(response.paymentSource()).isNotNull().isEqualTo(request.paymentSource());
                    assertThat(response.status()).isNotNull().isEqualTo(PaymentStatus.PENDING);
                    assertThat(response.id())
                            .withFailMessage("ID should not be null")
                            .isNotNull()
                            .withFailMessage("ID should be equal to %s".formatted(expectedPaymentId))
                            .isEqualTo(expectedPaymentId);
                    assertThat(response.createdAt())
                            .withFailMessage("Created at should not be null")
                            .isNotNull()
                            .withFailMessage("Created at should be equal to %s".formatted(expectedCreatedAt))
                            .isEqualTo(DataConverter.BRAZIL.toLocalDateTime(expectedCreatedAt));
                });
    }

    @Test
    @DisplayName("Should throw exception when limit is exceeded")
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
    @DisplayName("Should throw exception when receive zero amount")
    void shouldThrowExceptionWhenRequestAmountIsZero() {
        final var request = Fixture.PaymentRequests.createPixRequestWithCustomAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> this.service.createPayment(request))
                .isNotNull()
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Amount must be greater than zero");

        verify(this.repository, never()).sumPaymentsByPayerIdAndDate(any(), any(), any());
        verify(this.repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when receive negative amount")
    void shouldThrowExceptionWhenRequestAmountIsNegative() {
        final var request = Fixture.PaymentRequests.createPixRequestWithCustomAmount(new BigDecimal(-1));

        assertThatThrownBy(() -> this.service.createPayment(request))
                .isNotNull()
                .isInstanceOf(PaymentLimitException.class)
                .hasMessageContaining("Amount must be greater than zero");

        verify(this.repository, never()).sumPaymentsByPayerIdAndDate(any(), any(), any());
        verify(this.repository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve payment data when receive valid payment ID")
    void shouldRetrievePaymentDataWhenReceiveValidPaymentID() {
        final var expectedPaymentData = Fixture.Payments.createSamplePixPaymentDataWithStatusPending();
        final var expectedPaymentId = expectedPaymentData.getId();
        final var expectedPaymentPayerId = expectedPaymentData.getPayerId();
        final var expectedPaymentPaymentSource = expectedPaymentData.getPaymentSource();
        final var expectedPaymentAmount = expectedPaymentData.getAmount();
        final var expectedPaymentStatus = expectedPaymentData.getStatus();
        when(this.repository.findById(expectedPaymentId))
                .thenReturn(Optional.of(expectedPaymentData));

        final var actualPaymentData = this.service.retrievePaymentDataById(expectedPaymentId);

        verify(this.repository, times(1)).findById(expectedPaymentId);
        verify(this.repository, never()).sumPaymentsByPayerIdAndDate(any(), any(), any());
        verify(this.repository, never()).save(any());

        assertThat(actualPaymentData)
                .isNotNull()
                .satisfies(payment -> {
                    assertThat(payment.id())
                            .withFailMessage("Expected payment id to be not null")
                            .isNotNull()
                            .withFailMessage("Expected payment id to be valid")
                            .isEqualTo(expectedPaymentId);
                    assertThat(payment.payerId())
                            .withFailMessage("Expected payment payer id to be not null")
                            .isNotNull()
                            .withFailMessage("Expected payment payer id to be valid")
                            .isEqualTo(expectedPaymentPayerId);
                    assertThat(payment.paymentSource())
                            .withFailMessage("Expected payment source to be not null")
                            .isNotNull()
                            .withFailMessage("Expected payment source to be valid")
                            .isEqualTo(expectedPaymentPaymentSource);
                    assertThat(payment.amount())
                            .withFailMessage("Expected payment amount to be not null")
                            .isNotNull()
                            .withFailMessage("Expected payment amount to be valid")
                            .isEqualByComparingTo(expectedPaymentAmount);
                    assertThat(payment.status())
                            .withFailMessage("Expected payment status to be not null")
                            .isNotNull()
                            .withFailMessage("Expected payment status to be valid")
                            .isEqualTo(expectedPaymentStatus);
                    assertThat(payment.createdAt())
                            .withFailMessage("Expected payment creation date to be not null")
                            .isNotNull();
                });
    }

    @Test
    @DisplayName("Should throws PaymentNotFoundException when receive invalid payment ID")
    void shouldThrowsPaymentNotFoundExceptionWhenReceiveInvalidPaymentID() {
        final var expectedPaymentId = 1L;
        when(this.repository.findById(expectedPaymentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.service.retrievePaymentDataById(expectedPaymentId))
                .withFailMessage("Expected payment not found exception to be not null")
                .isNotNull()
                .withFailMessage("Expected a PaymentNotFoundException")
                .isInstanceOf(PaymentNotFoundException.class)
                .withFailMessage("Invalid message")
                .hasMessage("Payment ID: %s is not found".formatted(expectedPaymentId));

        verify(this.repository, times(1)).findById(expectedPaymentId);
        verify(this.repository, never()).sumPaymentsByPayerIdAndDate(any(), any(), any());
        verify(this.repository, never()).save(any());

    }

    @Test
    @DisplayName("Should throws PaymentNotFoundException when receive a null payment ID")
    void shouldThrowsPaymentNotFoundExceptionWhenReceiveNullPaymentID() {
        when(this.repository.findById(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.service.retrievePaymentDataById(null))
                .withFailMessage("Expected payment not found exception to be not null")
                .isNotNull()
                .withFailMessage("Expected a PaymentNotFoundException")
                .isInstanceOf(PaymentNotFoundException.class)
                .withFailMessage("Invalid message")
                .hasMessage("Payment ID: null is not found");

        verify(this.repository, times(1)).findById(any());
        verify(this.repository, never()).sumPaymentsByPayerIdAndDate(any(), any(), any());
        verify(this.repository, never()).save(any());

    }

    @Test
    @DisplayName("Should retrieve all payments when calling the method retrieveAllPayments")
    void shouldRetrieveAllPaymentsWhenCallingTheMethodRetrieveAllPayment() {
        final var expectedPaymentData1 = Fixture.Payments.createSamplePixPaymentDataWithStatusPending();
        final var expectedPaymentData2 = Fixture.Payments.createSampleCreditCardPaymentDataWithStatusPending();

        when(this.repository.findAll()).thenReturn(List.of(expectedPaymentData1, expectedPaymentData2));

        final var actual = this.service.retrieveAllPayments();

        assertThat(actual)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .satisfiesExactly(
                        first -> assertThat(first.paymentSource())
                                .isNotNull()
                                .isEqualTo(expectedPaymentData1.getPaymentSource()),
                        second -> assertThat(second.paymentSource())
                                .isNotNull()
                                .isEqualTo(expectedPaymentData2.getPaymentSource())
                );
    }

    @Test
    @DisplayName("Should retrieve empty list when calling the method retrieveAllPayments")
    void shouldRetrieveEmptyListWhenCallingTheMethodRetrieveAllPayment() {

        when(this.repository.findAll()).thenReturn(List.of());

        final var actual = this.service.retrieveAllPayments();

        assertThat(actual)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Should return payment list when calling the method retrievePaymentByPayerId")
    void shouldReturnPaymentListWhenCallingTheMethodRetrievePaymentByPayerId() {
        final var payment1 = Fixture.Payments.createSamplePixPaymentDataWithStatusPending();
        final var payment2 = Fixture.Payments.createSampleCreditCardPaymentDataWithStatusPending();

        final var expectedPayerId = payment1.getPayerId();

        when(this.repository.findAllByPayerId(expectedPayerId)).thenReturn(List.of(payment1, payment2));

        final var actual = this.service.retrievePaymentByPayerId(expectedPayerId.toString());

        assertThat(actual)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        first -> assertThat(first.payerId())
                                .isNotNull()
                                .isEqualTo(expectedPayerId),
                        second -> assertThat(second.payerId())
                                .isNotNull()
                                .isEqualTo(expectedPayerId)
                );

        verify(this.repository, times(1)).findAllByPayerId(expectedPayerId);
    }

    @Test
    @DisplayName("Should return an empty payment list when calling the method retrievePaymentByPayerId")
    void shouldReturnAnEmptyPaymentListWhenCallingTheMethodRetrievePaymentByPayerId() {

        final var expectedPayerId = UUID.randomUUID();

        when(this.repository.findAllByPayerId(expectedPayerId)).thenReturn(List.of());

        final var actual = this.service.retrievePaymentByPayerId(expectedPayerId.toString());

        assertThat(actual).isNotNull().isEmpty();

        verify(this.repository, times(1)).findAllByPayerId(expectedPayerId);
    }

    @Test
    @DisplayName("Should throw PaymentException when calling the method retrievePaymentByPayerId with null payerId")
    void shouldThrowPaymentExceptionWhenCallingTheMethodRetrievePaymentByPayerIdWithInvalidPayerId() {

        final var expectedPayerId = "invalid-payer-id";

        assertThatThrownBy(() -> this.service.retrievePaymentByPayerId(expectedPayerId))
                .isNotNull()
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining(expectedPayerId);

        verify(this.repository, never()).findAllByPayerId(any());
    }

    @Test
    @DisplayName("Should throw PaymentException when calling the method retrievePaymentByPayerId with null payerId")
    void shouldThrowPaymentExceptionWhenCallingTheMethodRetrievePaymentByPayerIdWithNullPayerId() {

        final var expectedMessageContaining = "null";

        assertThatThrownBy(() -> this.service.retrievePaymentByPayerId(null))
                .isNotNull()
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining(expectedMessageContaining);

        verify(this.repository, never()).findAllByPayerId(any());
    }

}

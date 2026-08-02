package br.com.neves.paymentsystem.controllers;

import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.repository.PaymentRepository;
import br.com.neves.paymentsystem.utils.Fixture;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymenIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;


    @Test
    @DisplayName("Should return OK when payment is created")
    @SneakyThrows
    void shouldReturnOkWhenPaymentIsCreated() {
        final var createPixRequest = Fixture.PaymentRequests.createPixRequestWithRandomPayerId();
        final var expectedPayerId = createPixRequest.payerId().toString();
        final var expectedPaymentSource = createPixRequest.paymentSource().name();
        final var expectedAmount = createPixRequest.amount().doubleValue();
        final var expectedStatus = PaymentStatus.PENDING.name();
        var request = Fixture.PaymentRequests.buildJson(createPixRequest).toJson();

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.payerId", is(expectedPayerId)))
                .andExpect(jsonPath("$.paymentSource", is(expectedPaymentSource)))
                .andExpect(jsonPath("$.amount", is(expectedAmount)))
                .andExpect(jsonPath("$.status", is(expectedStatus)))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

    }

    @Test
    @DisplayName("Should throw PaymentLimitException when payment amount is greater than limit")
    @SneakyThrows
    void shouldThrowPaymentLimitExceptionWhenPaymentAmountIsGreaterThanLimit() {
        final var createPixRequest = Fixture.PaymentRequests.createPixRequestWithCustomAmount(BigDecimal.valueOf(2001));
        final var expectedPaymentSource = createPixRequest.paymentSource().name();
        var request = Fixture.PaymentRequests.buildJson(createPixRequest).toJson();

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", is("Daily payment limit exceeded for source: %s".formatted(expectedPaymentSource))));

    }

    @Test
    @DisplayName("Should return payment response when receive valid payment id")
    @SneakyThrows
    void shouldReturnPaymentResponseWhenReceiveValidPaymentId() {
        final var createPayment = Fixture.Payments.createSamplePixPaymentDataWithStatusPending();
        createPayment.setId(null);
        final var expectedPayerId = createPayment.getPayerId().toString();
        final var expectedPaymentSource = createPayment.getPaymentSource().name();
        final var expectedAmount = createPayment.getAmount().doubleValue();
        final var expectedStatus = PaymentStatus.PENDING.name();

        final var expectedPaymentId = this.paymentRepository.save(createPayment).getId();

        mockMvc.perform(get("/api/payments/{paymentId}", expectedPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedPaymentId))
                .andExpect(jsonPath("$.payerId", is(expectedPayerId)))
                .andExpect(jsonPath("$.paymentSource", is(expectedPaymentSource)))
                .andExpect(jsonPath("$.amount", is(expectedAmount)))
                .andExpect(jsonPath("$.status", is(expectedStatus)))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when payment ID is invalid")
    @SneakyThrows
    void shouldThrowPaymentNotFoundExceptionWhenPaymentIdIsInvalid() {
        final var expectedPaymentId = -5L;

        mockMvc.perform(get("/api/payments/{paymentId}", expectedPaymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Payment ID: %s is not found".formatted(expectedPaymentId))));

    }

}
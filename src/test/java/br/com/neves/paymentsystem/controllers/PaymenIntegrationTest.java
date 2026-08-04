package br.com.neves.paymentsystem.controllers;

import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import br.com.neves.paymentsystem.repository.PaymentRepository;
import br.com.neves.paymentsystem.utils.Fixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        this.paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return payment response with status 201 created when payment is created with success")
    @SneakyThrows
    void shouldReturnPaymentResponseWithStatusCreatedWhenPaymentIsCreatedWithSuccess() {
        final var createPixRequest = Fixture.PaymentRequests.createPixRequestWithRandomPayerId();
        final var expectedPayerId = createPixRequest.payerId();
        final var expectedPaymentSource = createPixRequest.paymentSource();
        final var expectedAmount = createPixRequest.amount();
        final var expectedStatus = PaymentStatus.PENDING;

        var body = this.mapper.writeValueAsString(createPixRequest);

        final var content = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var response = this.mapper.readValue(content, PaymentResponse.class);

        assertThat(response)
                .isNotNull()
                .satisfies(paymentResponse -> {
                    assertThat(paymentResponse.id())
                            .isNotNull();
                    assertThat(paymentResponse.payerId())
                            .isNotNull()
                            .isEqualTo(expectedPayerId);
                    assertThat(paymentResponse.paymentSource())
                            .isNotNull()
                            .isEqualTo(expectedPaymentSource);
                    assertThat(paymentResponse.amount())
                            .isNotNull()
                            .isEqualByComparingTo(expectedAmount);
                    assertThat(paymentResponse.status())
                            .isNotNull()
                            .isEqualTo(expectedStatus);
                    assertThat(paymentResponse.createdAt())
                            .isNotNull();

                });
    }

    @Test
    @DisplayName("Should throw PaymentLimitException when payment amount is greater than limit")
    @SneakyThrows
    void shouldThrowPaymentLimitExceptionWhenPaymentAmountIsGreaterThanLimit() {
        final var createPixRequest = Fixture.PaymentRequests.createPixRequestWithCustomAmount(BigDecimal.valueOf(2001));
        final var expectedErrorMessage = "Daily payment limit exceeded for source: %s".formatted(createPixRequest.paymentSource());
        var request = this.mapper.writeValueAsString(createPixRequest);

        final var content = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnprocessableEntity()).andReturn().getResponse().getContentAsString();

        final var response = this.mapper.readValue(content, PaymentLimitException.class);

        assertThat(response)
                .isNotNull()
                .as("Expected response to be an instance of PaymentLimitException")
                .isInstanceOf(PaymentLimitException.class)
                .satisfies(ex -> {
                    assertThat(ex.getMessage())
                            .isNotNull()
                            .isEqualTo(expectedErrorMessage);
                    assertThat(ex.getErrors())
                            .isNotNull()
                            .isNotEmpty()
                            .hasSize(1)
                            .satisfiesExactly(error ->
                                    assertThat(error.message())
                                            .isNotNull()
                                            .isEqualTo(expectedErrorMessage)
                            );
                });

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
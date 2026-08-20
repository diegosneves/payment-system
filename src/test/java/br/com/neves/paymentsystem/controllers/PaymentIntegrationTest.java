package br.com.neves.paymentsystem.controllers;

import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.exceptions.PaymentException;
import br.com.neves.paymentsystem.exceptions.PaymentLimitException;
import br.com.neves.paymentsystem.model.Payment;
import br.com.neves.paymentsystem.repository.PaymentRepository;
import br.com.neves.paymentsystem.utils.DataConverter;
import br.com.neves.paymentsystem.utils.Fixture;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest {


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
        final var createPayment = Fixture.Payments.createSamplePixPaymentDataWithStatusPending(null);
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

    @Test
    @DisplayName("Should return list with all payment when calling method retrieve all payment")
    @SneakyThrows
    void shouldReturnListWithAllPaymentWhenCallingMethodRetrieveAllPayment() {
        final var createPayment = Fixture.Payments.createSamplePixPaymentDataWithStatusPending(null);
        final var expectedPayerId = createPayment.getPayerId();
        final var expectedPaymentSource = createPayment.getPaymentSource();
        final var expectedAmount = createPayment.getAmount();
        final var expectedStatus = PaymentStatus.PENDING;

        final var createPayment2 = Fixture.Payments.createSampleCreditCardPaymentDataWithStatusPending(null);
        final var expectedPayerId2 = createPayment2.getPayerId();
        final var expectedPaymentSource2 = createPayment2.getPaymentSource();
        final var expectedAmount2 = createPayment2.getAmount();
        final var expectedStatus2 = PaymentStatus.PENDING;

        final Payment persistedPayment = this.paymentRepository.save(createPayment);
        final var expectedPaymentId = persistedPayment.getId();
        final var expectedCreateAt = DataConverter.BRAZIL.toLocalDateTime(persistedPayment.getCreatedAt());

        final Payment persistedPayment2 = this.paymentRepository.save(createPayment2);
        final var expectedPaymentId2 = persistedPayment2.getId();
        final var expectedCreateAt2 = DataConverter.BRAZIL.toLocalDateTime(persistedPayment2.getCreatedAt());

        final var content = mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final var response = this.mapper.readValue(content, new TypeReference<List<PaymentResponse>>() {});

        // Usar para tipos dinamicos ex. Class<?> elementType = resolveTypeAtRuntime(); JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
//        final List<PaymentResponse> response = this.mapper.readValue(content, mapper.getTypeFactory().constructCollectionType(List.class, PaymentResponse.class));

        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        first -> {
                            assertThat(first.id())
                                    .isNotNull()
                                    .isEqualTo(expectedPaymentId);
                            assertThat(first.payerId())
                                    .isNotNull()
                                    .isEqualTo(expectedPayerId);
                            assertThat(first.paymentSource())
                                    .isNotNull()
                                    .isEqualTo(expectedPaymentSource);
                            assertThat(first.amount())
                                    .isNotNull()
                                    .isEqualByComparingTo(expectedAmount);
                            assertThat(first.status())
                                    .isNotNull()
                                    .isEqualTo(expectedStatus);
                            assertThat(first.createdAt())
                                    .isNotNull()
                                    .isCloseTo(expectedCreateAt, within(1, ChronoUnit.SECONDS));
                        },
                        second -> {
                            assertThat(second.id())
                                    .isNotNull()
                                    .isEqualTo(expectedPaymentId2);
                            assertThat(second.payerId())
                                    .isNotNull()
                                    .isEqualTo(expectedPayerId2);
                            assertThat(second.paymentSource())
                                    .isNotNull()
                                    .isEqualTo(expectedPaymentSource2);
                            assertThat(second.amount())
                                    .isNotNull()
                                    .isEqualByComparingTo(expectedAmount2);
                            assertThat(second.status())
                                    .isNotNull()
                                    .isEqualTo(expectedStatus2);
                            assertThat(second.createdAt())
                                    .isNotNull()
                                    .isCloseTo(expectedCreateAt2, within(1, ChronoUnit.SECONDS));
                        }
                );
    }

    @Test
    @DisplayName("Should return an empty list when calling method retrieve all payment")
    @SneakyThrows
    void shouldReturnAnEmptyListWhenCallingMethodRetrieveAllPayment() {

        final var content = mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final var response = this.mapper.readValue(content, new TypeReference<List<PaymentResponse>>() {});

        assertThat(response)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Should return list with all payment of payer when calling method retrieve all payment by payer id")
    @SneakyThrows
    void shouldReturnListWithAllPaymentOfPayerWhenCallingMethodRetrieveAllPaymentByPayerId() {
        final var createPayment = Fixture.Payments.createSamplePixPaymentDataWithStatusPending(null);
        final var expectedPayerId = createPayment.getPayerId();
        final var expectedPaymentSource = createPayment.getPaymentSource();
        final var expectedAmount = createPayment.getAmount();
        final var expectedStatus = PaymentStatus.PENDING;

        final var createPayment2 = Fixture.Payments.createSampleCreditCardPaymentDataWithStatusPending(null);
        final var expectedPayerId2 = createPayment2.getPayerId();
        final var expectedPaymentSource2 = createPayment2.getPaymentSource();
        final var expectedAmount2 = createPayment2.getAmount();
        final var expectedStatus2 = PaymentStatus.PENDING;

        final var createPayment3 = Fixture.Payments.createSampleCreditCardPaymentDataWithStatusPending(null);
        createPayment3.setPayerId(UUID.randomUUID());

        final Payment persistedPayment = this.paymentRepository.save(createPayment);
        final var expectedPaymentId = persistedPayment.getId();
        final var expectedCreateAt = DataConverter.BRAZIL.toLocalDateTime(persistedPayment.getCreatedAt());

        final Payment persistedPayment2 = this.paymentRepository.save(createPayment2);
        final var expectedPaymentId2 = persistedPayment2.getId();
        final var expectedCreateAt2 = DataConverter.BRAZIL.toLocalDateTime(persistedPayment2.getCreatedAt());

        this.paymentRepository.save(createPayment3);

        final var content = mockMvc.perform(get("/api/payments/payer/{payerId}", expectedPayerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final var response = this.mapper.readValue(content, new TypeReference<List<PaymentResponse>>() {});

        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        first -> {
                            assertThat(first.id())
                                    .isNotNull()
                                    .isEqualTo(expectedPaymentId);
                            assertThat(first.payerId())
                                    .isNotNull()
                                    .isEqualTo(expectedPayerId);
                            assertThat(first.paymentSource())
                                    .isNotNull()
                                    .isEqualTo(expectedPaymentSource);
                            assertThat(first.amount())
                                    .isNotNull()
                                    .isEqualByComparingTo(expectedAmount);
                            assertThat(first.status())
                                    .isNotNull()
                                    .isEqualTo(expectedStatus);
                            assertThat(first.createdAt())
                                    .isNotNull()
                                    .isCloseTo(expectedCreateAt, within(1, ChronoUnit.SECONDS));
                        },
                        second -> {
                            assertThat(second.id())
                                    .isNotNull()
                                    .isEqualTo(expectedPaymentId2);
                            assertThat(second.payerId())
                                    .isNotNull()
                                    .isEqualTo(expectedPayerId2);
                            assertThat(second.paymentSource())
                                    .isNotNull()
                                    .isEqualTo(expectedPaymentSource2);
                            assertThat(second.amount())
                                    .isNotNull()
                                    .isEqualByComparingTo(expectedAmount2);
                            assertThat(second.status())
                                    .isNotNull()
                                    .isEqualTo(expectedStatus2);
                            assertThat(second.createdAt())
                                    .isNotNull()
                                    .isCloseTo(expectedCreateAt2, within(1, ChronoUnit.SECONDS));
                        }
                );
    }


    @Test
    @DisplayName("Should return empty list when calling retrieve payment by payer id")
    @SneakyThrows
    void shouldReturnEmptyListWhenCallingRetrievePaymentByPayerId() {
        final var createPayment = Fixture.Payments.createSamplePixPaymentDataWithStatusPending(null);
        final var createPayment2 = Fixture.Payments.createSampleCreditCardPaymentDataWithStatusPending(null);

        final var samplePayerId = UUID.randomUUID();

        this.paymentRepository.saveAll(List.of(createPayment, createPayment2));

        final var content = mockMvc.perform(get("/api/payments/payer/{payerId}", samplePayerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        final var response = this.mapper.readValue(content, new TypeReference<List<PaymentResponse>>() {});

        assertThat(response).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should throw PaymentException when calling method retrieve all payment by payer id with invalid payer id")
    @SneakyThrows
    void shouldThrowPaymentExceptionWhenCallingMethodRetrieveAllPaymentByPayerIdWithInvalidPayerId() {

        final var expectedMessageContaining = "Invalid-uuid-string";

        final var content = mockMvc.perform(get("/api/payments/payer/{payerId}", expectedMessageContaining)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        final var response = this.mapper.readValue(content, PaymentException.class);

        assertThat(response)
                .isNotNull()
                .as("Expected response to be an instance of PaymentException")
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> {
                    assertThat(ex.getMessage())
                            .isNotNull()
                            .isNotEmpty()
                            .contains(expectedMessageContaining);
                    assertThat(ex.getErrors())
                            .isNotNull()
                            .isNotEmpty()
                            .hasSize(1)
                            .satisfiesExactly(error ->
                                    assertThat(error.message())
                                            .isNotNull()
                                            .isNotEmpty()
                                            .contains(expectedMessageContaining)
                            );
                });
    }

}
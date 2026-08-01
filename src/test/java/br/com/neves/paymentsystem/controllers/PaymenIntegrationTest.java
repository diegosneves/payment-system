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

import static org.hamcrest.Matchers.is;
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
        final var createPixRequest = Fixture.PaymentRequests.createPixRequest();
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

}
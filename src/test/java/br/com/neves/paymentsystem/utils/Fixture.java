package br.com.neves.paymentsystem.utils;

import br.com.neves.paymentsystem.enums.PaymentStatus;
import br.com.neves.paymentsystem.model.Payment;
import br.com.neves.paymentsystem.utils.adapter.PaymentRequestToJsonAdapter;
import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.enums.PaymentSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

public final class Fixture {

    private static final UUID DEFAULT_PAYER_ID = UUID.fromString("739e5f1e-2459-499d-9cdd-ab59b31c02d9");

    private Fixture() {
    }


    public static final class PaymentRequests {

        public static PaymentRequest createPixRequest() {
            return PaymentRequest.create(DEFAULT_PAYER_ID, PaymentSource.PIX, new BigDecimal("100.00"));
        }

        public static PaymentRequest createPixRequestWithRandomPayerId() {
            return PaymentRequest.create(UUID.randomUUID(), PaymentSource.PIX, new BigDecimal("100.00"));
        }

        public static PaymentRequest createPixRequestWithCustomAmount(final BigDecimal amount) {
            return PaymentRequest.create(DEFAULT_PAYER_ID, PaymentSource.PIX, amount);
        }

        public static PaymentRequest createPixRequestWithRandomPayerIdAndWithCustomAmount(final BigDecimal amount) {
            return PaymentRequest.create(UUID.randomUUID(), PaymentSource.PIX, amount);
        }

        public static PaymentRequest createDebitCardRequest() {
            return PaymentRequest.create(DEFAULT_PAYER_ID, PaymentSource.DEBIT_CARD, new BigDecimal("200.00"));
        }

        public static PaymentRequest createCreditCardRequest() {
            return PaymentRequest.create(DEFAULT_PAYER_ID, PaymentSource.CREDIT_CARD, new BigDecimal("300.00"));
        }

        /**
         * Cria e retorna uma instância de {@link PaymentRequestToJsonAdapter} a partir de um {@link PaymentRequest}
         * fornecido por um {@link Supplier}.
         *
         * <p>Este método utilitário serve como ponto de entrada para a construção de representações
         * JSON de requisições de pagamento em contextos de teste. Ele aceita um {@link Supplier}
         * funcional que, ao ser invocado, fornece a instância de {@link PaymentRequest} que será
         * encapsulada pelo {@link PaymentRequestToJsonAdapter} retornado.</p>
         *
         * <p>Exemplo de uso:</p>
         * <pre>{@code
         * String json = Fixture.PaymentRequests
         *      .buildJson(PaymentRequests::createPixRequest)
         *      .toJson();
         *
         * String json = Fixture.PaymentRequests
         *      .buildJson(PaymentRequests::createDebitCardRequest)
         *      .toJson();
         *
         * String customAmtJson = Fixture.PaymentRequests
         *      .buildJson(() -> PaymentRequests.createPixRequestWithAmount(new BigDecimal("50.00")))
         *      .toJson();
         *
         * }</pre>
         *
         * @param supplier um {@link Supplier} não nulo que provê a instância de {@link PaymentRequest}
         *                 a ser serializada; exemplos de fornecedores válidos incluem referências
         *                 a métodos como {@code createPixRequest}, {@code createDebitCardRequest}
         *                 ou {@code createCreditCardRequest}
         * @return uma instância de {@link PaymentRequestToJsonAdapter} inicializada com o {@link PaymentRequest}
         *         obtido do {@code supplier}, pronta para gerar a representação JSON via
         *         {@link PaymentRequestToJsonAdapter#toJson()}
         * @throws NullPointerException se {@code supplier} for nulo ou se {@code supplier.get()}
         *                              retornar nulo, pois o {@link PaymentRequestToJsonAdapter} requer uma
         *                              instância válida de {@link PaymentRequest}
         * @see PaymentRequestToJsonAdapter
         * @see PaymentRequest
         */
        public static JsonBuilder<PaymentRequest> buildJson(final Supplier<PaymentRequest> supplier) {
            return PaymentRequestToJsonAdapter.create(supplier.get());
        }

        public static JsonBuilder<PaymentRequest> buildJson(final PaymentRequest request) {
            return PaymentRequestToJsonAdapter.create(request);
        }

    }

    public final static class Payments {

        public static Payment createSamplePixPaymentDataWithStatusPending() {
            return Payment.builder()
                    .id(1L)
                    .payerId(DEFAULT_PAYER_ID)
                    .status(PaymentStatus.PENDING)
                    .paymentSource(PaymentSource.PIX)
                    .amount(new BigDecimal("100.00"))
                    .createdAt(Instant.now())
                    .build();
        }

    }

}

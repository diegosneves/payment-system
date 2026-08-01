package br.com.neves.paymentsystem.utils.adapter;

import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.utils.JsonBuilder;

public final class PaymentRequestToJsonAdapter extends JsonBuilder<PaymentRequest> {

    private PaymentRequestToJsonAdapter(final PaymentRequest request) {
        super(request);
    }

    public static PaymentRequestToJsonAdapter create(final PaymentRequest request) {
        return new PaymentRequestToJsonAdapter(request);
    }

    @Override
    public String toJson() {
        return """
                {
                    "payerId": "%s",
                    "paymentSource": "%s",
                    "amount": %s
                }
                """.formatted(this.request.payerId(), this.request.paymentSource(), this.request.amount());
    }
}

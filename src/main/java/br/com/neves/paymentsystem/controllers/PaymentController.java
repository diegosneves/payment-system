package br.com.neves.paymentsystem.controllers;

import br.com.neves.paymentsystem.configuration.web.ApiError;
import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.enums.PaymentSource;
import br.com.neves.paymentsystem.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("api/payments")
@Tag(name = "Payments", description = "Endpoints relacionados a pagamentos")
@RequiredArgsConstructor
public class PaymentController {

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Cria um novo pagamento", description = "Endpoint para criar um novo pagamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody final PaymentRequest paymentRequest) {
        final var response = PaymentResponse.create(1L, UUID.randomUUID(), PaymentSource.PIX, BigDecimal.TEN, PaymentStatus.PENDING);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}

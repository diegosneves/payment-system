package br.com.neves.paymentsystem.controllers;

import br.com.neves.paymentsystem.configuration.web.ApiError;
import br.com.neves.paymentsystem.dto.PaymentRequest;
import br.com.neves.paymentsystem.dto.PaymentResponse;
import br.com.neves.paymentsystem.services.PaymentService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/payments")
@Tag(name = "Payments", description = "Endpoints relacionados a pagamentos")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Cria um novo pagamento", description = "Endpoint para criar um novo pagamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Pagamento inválido", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody final PaymentRequest paymentRequest) {
        final var response = this.paymentService.createPayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Recupera dados de um pagamento por ID", description = "Endpoint para recuperar dados de um pagamento por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do pagamento recuperados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PaymentResponse> retrievePaymentByPaymentId(@PathVariable final Long paymentId) {
        final var response = this.paymentService.retrievePaymentDataById(paymentId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    @Operation(summary = "Recupera todos os pagamentos", description = "Endpoint para recuperar todos os pagamentos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamentos recuperados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<PaymentResponse> retrieveAllPayments() {
        return this.paymentService.retrieveAllPayments();
    }

    @GetMapping("/payer/{payerId}")
    @Operation(summary = "Recupera todos os pagamentos por ID do pagador", description = "Endpoint para recuperar todos os pagamentos por ID do pagador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamentos recuperados com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID do pagador inválido", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<PaymentResponse> retrieveAllPaymentsByPayerId(@PathVariable final String payerId) {
        return this.paymentService.retrievePaymentByPayerId(payerId);
    }

}

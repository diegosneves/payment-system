package br.com.neves.paymentsystem.controllers;

import br.com.neves.paymentsystem.ia.TravelAgentAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/travel-agent")
@Tag(name = "Travel Agent", description = "Travel Agent API")
@RequiredArgsConstructor
public class TravelAgentController {

    private final TravelAgentAssistant travelAgentAssistant;

    @PostMapping(
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @Operation(summary = "Chat with the travel agent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat response"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> chat(@RequestBody final String message) {
        return ResponseEntity.ok(travelAgentAssistant.chat(message));
    }

}

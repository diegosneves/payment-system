package br.com.neves.paymentsystem.ia;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface TravelAgentAssistant {

    String chat(String message);
}

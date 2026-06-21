package com.example.SprinAICode;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class OllamaAiController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory = MessageWindowChatMemory.builder().build();

    @Autowired
    private EmbeddingModel embeddingModel;

    @PostMapping("/embeddings")
    public float[] testEmbedding(@RequestParam String text) {
        float[] vector = embeddingModel.embed(text);
        System.out.println("Embedding length: " + vector.length);
        return vector;
    }

    public OllamaAiController(OllamaChatModel chatModel) {

        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @GetMapping("/apiOllama/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable String message) {

        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "default"))
                .call()
                .chatResponse();

        System.out.println("Content: " +
                response.getResult().getOutput().getText());

        System.out.println("Metadata: " +
                response.getMetadata()
                        .getModel());

        return ResponseEntity.ok(
                response.getResult().getOutput().getText()
        );
    }

    @PostMapping("/api/recommend")
    public ResponseEntity<String> recommend(@RequestParam String type,
                                            @RequestParam String year,
                                            @RequestParam String lang) {
        String tempt = """
                    I want to watchh a {type} movie with {year} {lang}.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(tempt);

        Prompt prompt = promptTemplate.create(Map.of("type", type, "year", year, "lang", lang));

        String response = chatClient
                .prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "recommend-session"))
                .call()
                .content();

        return ResponseEntity.ok(response);
    }
}

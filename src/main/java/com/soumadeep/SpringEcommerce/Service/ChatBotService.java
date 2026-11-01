package com.soumadeep.SpringEcommerce.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotService {

    private static final String USER_RULE =
            """
                    This user is a customer. The context contains only this customer's own orders.
                    If they ask about another person's orders, names, emails or any other customer's data,
                    refuse and say you can only help with their own orders.
            """;

    private static final String ADMIN_RULE =
            """
                    This user is the store admin. You may share details of any order or product
                    found in the context, including customer names and emails.
            """;

    private final ResourceLoader loader;
    private final PgVectorStore vectorStore;
    private final ChatClient chatClient;

    public String getBotResponse(String message, String email, boolean admin) {

        String promptStringTemplate;
        try {
            promptStringTemplate = loader
                    .getResource("classpath:prompts/chatbot-rag-prompt.st")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not load chatbot prompt", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Chatbot is not configured properly");
        }

        try {
            String context = fetchSemanticContext(message, email, admin);

            Map<String, Object> variables = new HashMap<>();
            variables.put("context", context);
            variables.put("userQuery", message);
            variables.put("userEmail", email);
            variables.put("accessRule", admin ? ADMIN_RULE : USER_RULE);

            PromptTemplate promptTemplate = PromptTemplate.builder()
                    .template(promptStringTemplate)
                    .variables(variables)
                    .build();

            return chatClient
                    .prompt(promptTemplate.create())
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("Chatbot failed", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Chatbot is unavailable right now. Please try again.");
        }
    }

    private String fetchSemanticContext(String message, String email, boolean admin) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        List<Document> products = search(message, b.eq("type", "PRODUCT").build(), 5, 0.4);

        Filter.Expression orderFilter = admin ? b.eq("type", "ORDER").build()
                : b.and(b.eq("type", "ORDER"), b.eq("ownerEmail", email)).build();

        List<Document> orders = search(message, orderFilter, admin ? 5 : 3, 0.25);

        StringBuilder sb = new StringBuilder("PRODUCTS:\n");
        for (Document d : products) {
            sb.append(d.getText()).append("\n");
        }
        sb.append("\nORDERS:\n");
        for (Document d : orders) {
            sb.append(d.getText()).append("\n");
        }
        return sb.toString();
    }

    private List<Document> search(String query, Filter.Expression filter, int topK, double threshold) {
        return vectorStore.doSimilaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(threshold)
                        .filterExpression(filter)
                        .build());
    }
}
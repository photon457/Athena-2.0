package org.example;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Requests {
    private static final String API_KEY = "AzQMO81fOLn1vnhltUla6YgtofomQZUe";
    private static final String API_URL = "https://api.mistral.ai/v1/chat/completions";

    public String getAIMessage(List<Map<String, Object>> messages) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .connectTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .build();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> body = new HashMap<>();
        body.put("model", "mistral-large-latest"); // Or mistral-medium, mistral-large
        body.put("messages", messages);

        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(body); // Catch JsonProcessingException here
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error serializing request body: " + e.getMessage();
        }

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(
                        jsonBody,
                        MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                return "Unexpected code " + response;

            String responseBody = response.body().string();
            try {
                JsonNode root = mapper.readTree(responseBody);
                JsonNode content = root.path("choices").get(0).path("message").path("content");
                if (content.isTextual()) {
                    return content.asText();
                } else {
                    return "Error: Unexpected response structure - content not found or not textual.";
                }
            } catch (Exception e) {
                return "Error parsing JSON response: " + e.getMessage();
            }
        } catch (IOException e) {
            return "IOException: " + e.getMessage();
        }
    }
}

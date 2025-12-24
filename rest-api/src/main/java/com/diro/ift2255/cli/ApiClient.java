package com.diro.ift2255.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Client HTTP pour communiquer avec l'API REST.
 * Le CLI ne contient pas de logique métier, il appelle juste l'API.
 */
public class ApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    /**
     * GET request vers l'API.
     * @return ApiResponse avec success, data, message, et status HTTP
     */
    public ApiResponse get(String endpoint) {
        return get(endpoint, Map.of());
    }

    public ApiResponse get(String endpoint, Map<String, String> queryParams) {
        try {
            String url = buildUrl(endpoint, queryParams);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response);

        } catch (Exception e) {
            return ApiResponse.error("Erreur de connexion: " + e.getMessage(), 0);
        }
    }

    /**
     * POST request vers l'API avec body JSON.
     */
    public ApiResponse post(String endpoint, Object body) {
        try {
            String jsonBody = mapper.writeValueAsString(body);
            String url = baseUrl + endpoint;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response);

        } catch (Exception e) {
            return ApiResponse.error("Erreur de connexion: " + e.getMessage(), 0);
        }
    }

    private String buildUrl(String endpoint, Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder(baseUrl).append(endpoint);
        if (!queryParams.isEmpty()) {
            sb.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) sb.append("&");
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                  .append("=")
                  .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                first = false;
            }
        }
        return sb.toString();
    }

    private ApiResponse parseResponse(HttpResponse<String> response) {
        try {
            JsonNode root = mapper.readTree(response.body());
            boolean success = root.has("success") && root.get("success").asBoolean();
            String message = root.has("message") ? root.get("message").asText() : null;
            JsonNode data = root.has("data") ? root.get("data") : null;

            return new ApiResponse(success, data, message, response.statusCode());

        } catch (Exception e) {
            // Si le parsing échoue, retourne le body brut comme message d'erreur
            return ApiResponse.error("Réponse invalide: " + response.body(), response.statusCode());
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    // ========================================================================
    // Classe interne pour les réponses API
    // ========================================================================
    public static class ApiResponse {
        private final boolean success;
        private final JsonNode data;
        private final String message;
        private final int statusCode;

        public ApiResponse(boolean success, JsonNode data, String message, int statusCode) {
            this.success = success;
            this.data = data;
            this.message = message;
            this.statusCode = statusCode;
        }

        public static ApiResponse error(String message, int statusCode) {
            return new ApiResponse(false, null, message, statusCode);
        }

        public boolean isSuccess() { return success; }
        public JsonNode getData() { return data; }
        public String getMessage() { return message; }
        public int getStatusCode() { return statusCode; }

        public boolean hasData() {
            return data != null && !data.isNull() && !data.isEmpty();
        }
    }
}

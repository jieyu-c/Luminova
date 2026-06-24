package cn.jieyuc.luminovanacos.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.StringJoiner;

public class NacosPromptClient {

    private static final String LOGIN_PATH = "/nacos/v3/auth/user/login";
    private static final String PROMPT_PATH = "/nacos/v3/client/ai/prompt";
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_PRELOAD_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration TOKEN_REFRESH_SKEW = Duration.ofMinutes(1);

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private volatile AccessToken accessToken;

    public NacosPromptClient(
            Environment environment,
            ObjectMapper objectMapper
    ) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout())
                .build();
    }

    public NacosPromptValue getPrompt(NacosPromptRequest promptRequest) {
        return getPrompt(promptRequest, requestTimeout());
    }

    public NacosPromptValue getPrompt(NacosPromptRequest promptRequest, Duration timeout) {
        HttpRequest request = promptRequest(promptRequest, timeout, true);
        HttpResponse<String> response = sendResponse(request);

        if (isUnauthorized(response)) {
            accessToken = null;
            response = sendResponse(promptRequest(promptRequest, timeout, true));
        }

        JsonNode root = parseSuccessfulResponse(response);
        JsonNode responseData = root.path("data");
        JsonNode data = responseData.isMissingNode() || responseData.isNull() ? root : responseData;

        String template = text(data, "template")
                .or(() -> text(data, "prompt"))
                .orElse("");
        String md5 = text(data, "md5").orElse("");
        String version = text(data, "version").orElse("");
        return new NacosPromptValue(template, md5, version);
    }

    public boolean canCallPromptApi(Duration timeout) {
        if (!shouldLogin()) {
            return true;
        }

        try {
            currentToken(timeout);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public Duration preloadTimeout() {
        return timeout("spring.cloud.nacos.config.timeout", DEFAULT_PRELOAD_TIMEOUT, DEFAULT_PRELOAD_TIMEOUT);
    }

    private JsonNode send(HttpRequest request) {
        return parseSuccessfulResponse(sendResponse(request));
    }

    private HttpResponse<String> sendResponse(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to call Nacos Prompt API", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling Nacos Prompt API", exception);
        }
    }

    private JsonNode parseSuccessfulResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("Nacos Prompt API request failed, status="
                    + response.statusCode() + ", body=" + response.body());
        }
        try {
            return objectMapper.readTree(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read Nacos Prompt API response", exception);
        }
    }

    private HttpRequest promptRequest(NacosPromptRequest promptRequest, Duration timeout, boolean withAuth) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(promptUri(promptRequest))
                .timeout(timeout)
                .GET();

        if (withAuth && shouldLogin()) {
            builder.header("Authorization", "Bearer " + currentToken(timeout));
        }

        return builder.build();
    }

    private URI promptUri(NacosPromptRequest promptRequest) {
        StringJoiner query = new StringJoiner("&");
        query.add(param("promptKey", promptRequest.key()));
        if (StringUtils.hasText(promptRequest.version())) {
            query.add(param("version", promptRequest.version()));
        }
        if (StringUtils.hasText(promptRequest.label())) {
            query.add(param("label", promptRequest.label()));
        }

        return URI.create(baseUrl() + PROMPT_PATH + "?" + query);
    }

    private String currentToken(Duration timeout) {
        AccessToken cached = accessToken;
        if (cached != null && !cached.shouldRefresh()) {
            return cached.token();
        }

        synchronized (this) {
            cached = accessToken;
            if (cached != null && !cached.shouldRefresh()) {
                return cached.token();
            }
            accessToken = login(timeout);
            return accessToken.token();
        }
    }

    private AccessToken login(Duration timeout) {
        String form = param("username", username()) + "&" + param("password", password());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + LOGIN_PATH))
                .timeout(timeout)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        JsonNode root = send(request);
        String token = text(root, "accessToken")
                .or(() -> text(root.path("data"), "accessToken"))
                .orElseThrow(() -> new IllegalStateException("Nacos login response does not contain accessToken"));
        long ttlSeconds = root.path("tokenTtl").asLong(root.path("data").path("tokenTtl").asLong(18_000));
        return new AccessToken(token, Instant.now().plusSeconds(ttlSeconds));
    }

    private boolean shouldLogin() {
        return StringUtils.hasText(username())
                && StringUtils.hasText(password());
    }

    private String baseUrl() {
        String serverAddr = firstServerAddr(
                firstText(
                        environment.getProperty("spring.cloud.nacos.server-addr"),
                        environment.getProperty("spring.cloud.nacos.config.server-addr")
                )
        );
        if (!StringUtils.hasText(serverAddr)) {
            throw new IllegalStateException("Nacos server address is not configured");
        }
        if (serverAddr.startsWith("http://") || serverAddr.startsWith("https://")) {
            return trimTrailingSlash(serverAddr);
        }
        return "http://" + trimTrailingSlash(serverAddr);
    }

    private String username() {
        return firstText(
                environment.getProperty("spring.cloud.nacos.username"),
                environment.getProperty("spring.cloud.nacos.config.username"),
                environment.getProperty("spring.cloud.nacos.discovery.username")
        );
    }

    private String password() {
        return firstText(
                environment.getProperty("spring.cloud.nacos.password"),
                environment.getProperty("spring.cloud.nacos.config.password"),
                environment.getProperty("spring.cloud.nacos.discovery.password")
        );
    }

    private Duration connectTimeout() {
        return timeout("spring.cloud.nacos.config.timeout", DEFAULT_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
    }

    private Duration requestTimeout() {
        return timeout("spring.cloud.nacos.config.timeout", DEFAULT_REQUEST_TIMEOUT, DEFAULT_REQUEST_TIMEOUT);
    }

    private Duration timeout(String propertyName, Duration defaultValue, Duration maxValue) {
        Long millis = environment.getProperty(propertyName, Long.class);
        if (millis == null || millis <= 0) {
            return defaultValue;
        }
        Duration configured = Duration.ofMillis(millis);
        return configured.compareTo(maxValue) > 0 ? maxValue : configured;
    }

    private static String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private static String firstServerAddr(String serverAddr) {
        if (!StringUtils.hasText(serverAddr)) {
            return "";
        }
        return serverAddr.split(",")[0].trim();
    }

    private static String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String param(String name, String value) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Optional<String> text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || !StringUtils.hasText(value.asText())) {
            return Optional.empty();
        }
        return Optional.of(value.asText());
    }

    private static boolean isUnauthorized(HttpResponse<String> response) {
        return response.statusCode() == 401 || response.statusCode() == 403;
    }

    private record AccessToken(String token, Instant expiresAt) {

        private boolean shouldRefresh() {
            return Instant.now().plus(TOKEN_REFRESH_SKEW).isAfter(expiresAt);
        }
    }
}

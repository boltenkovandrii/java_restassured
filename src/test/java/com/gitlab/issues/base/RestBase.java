package com.gitlab.issues.base;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.http.ConnectionClosedException;
import org.apache.http.NoHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static org.assertj.core.api.Assertions.assertThat;



// Base for REST API calls with retry logic.
// 429 is retried for all methods (delay from numeric Retry-After if sane, else linear backoff, plus jitter).
// Other retriable codes (408, 502, 503, 504) and transient exceptions are retried only for GET.
public class RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestBase.class);
    private final RequestSpecification reqSpec;
    private final RestAssuredConfig rConfig = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig())
            .logConfig(logConfig().blacklistHeader("PRIVATE-TOKEN"));

    private static final CompositeConfiguration CONFIG = TestConfig.getConfiguration();
    private final int maxAttempts = CONFIG.getInt("RetryAttempts", 3);
    private final long retryBaseDelay = CONFIG.getLong("RetryBaseDelay", 500);
    private final long maxDelay = CONFIG.getLong("RetryMaxDelay", 60000);
    private final long maxJitter = CONFIG.getLong("RetryMaxJitter", 200);

    public enum AuthMode { VALID, ABSENT, INVALID, EXPIRED, EMPTY }


    public RestBase(RequestSpecification reqSpec) {
        this.reqSpec = reqSpec;
    }


    private RequestSpecification authenticatedRequest(AuthMode mode) {
        RequestSpecification spec = given().config(rConfig).spec(reqSpec).urlEncodingEnabled(false);
        return switch (mode) {
            case VALID -> spec.header("PRIVATE-TOKEN", CONFIG.getString("GITLAB_ACCESS_TOKEN"));
            case ABSENT -> spec;
            case INVALID -> spec.header("PRIVATE-TOKEN", "invalid-token-value");
            case EXPIRED -> spec.header("PRIVATE-TOKEN", CONFIG.getString("EXPIRED_TOKEN"));
            case EMPTY -> spec.header("PRIVATE-TOKEN", "");
        };
    }

    public Response getInternal(String uri) {
        return getInternal(uri, Map.of());
    }

    public Response getInternal(String uri, Map<String, ?> queryParams) {
        return getInternal(uri, queryParams, AuthMode.VALID);
    }

    public Response getInternal(String uri, Map<String, ?> queryParams, AuthMode mode) {
        return execute("GET", uri, () -> authenticatedRequest(mode).queryParams(queryParams).when().get(uri));
    }

    public Response postInternal(Object json, String uri) {
        return postInternal(json, uri, AuthMode.VALID);
    }

    public Response postInternal(Object json, String uri, AuthMode mode) {
        return execute("POST", uri, () -> authenticatedRequest(mode).body(json).when().post(uri));
    }

    public Response putInternal(Object json, String uri) {
        return putInternal(json, uri, AuthMode.VALID);
    }

    public Response putInternal(Object json, String uri, AuthMode mode) {
        return execute("PUT", uri, () -> authenticatedRequest(mode).body(json).when().put(uri));
    }

    public Response deleteInternal(String uri) {
        return deleteInternal(uri, AuthMode.VALID);
    }

    public Response deleteInternal(String uri, AuthMode mode) {
        return execute("DELETE", uri, () -> authenticatedRequest(mode).when().delete(uri));
    }

    private Response execute(String method, String uri, Supplier<Response> request) {
        boolean isGet = "GET".equals(method);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                LOGGER.info("Sending {} request on {} (attempt {}/{})", method, uri, attempt, maxAttempts);
                Response response = request.get();
                int status = response.getStatusCode();

                boolean retriable = status == 429 || (isGet && needRetry(status));
                if (!retriable || attempt == maxAttempts) {
                    return response;
                }

                long delay = calculateDelay(response, attempt);
                LOGGER.warn("{} request to {} returned status code {}. Retrying in {} ms (attempt {}/{})", method, uri, status, delay, attempt + 1, maxAttempts);
                sleep(delay);
            } catch (Exception e) {
                if (!isGet || attempt == maxAttempts || !isRetryableException(e)) {
                    throw new RuntimeException(e);
                }
                long delay = withJitter(Math.min(retryBaseDelay * attempt, maxDelay));
                LOGGER.error("GET request to {} failed with exception: {}. Retrying in {} ms (attempt {}/{})", uri, e.getMessage(), delay, attempt + 1, maxAttempts);
                sleep(delay);
            }
        }
        throw new IllegalStateException("Unexpected end of retry loop");
    }

    private long calculateDelay(Response response, int attempt) {
        long delay = Math.min(retryBaseDelay * attempt, maxDelay);
        String retryAfter = response.getHeader("Retry-After");
        if (retryAfter != null) {
            try {
                long seconds = Long.parseLong(retryAfter.trim());
                if (seconds > 0 && seconds <= maxDelay / 1000) {
                    delay = seconds * 1000;
                }
            } catch (NumberFormatException ignored) {
                // fall back to backoff
            }
        }
        return withJitter(delay);
    }

    private long withJitter(long delay) {
        return maxJitter > 0 ? delay + ThreadLocalRandom.current().nextLong(maxJitter + 1) : delay;
    }

    static boolean isSuccess(int statusCode) {
        return 200 <= statusCode && statusCode <= 299;
    }

    public static void checkThatResponseIsSuccessful(Response response) {
        assertThat(isSuccess(response.getStatusCode()))
                .as("Response code should be in range 200-299, but was " + response.getStatusCode())
                .isTrue();
    }

    private static boolean needRetry(int statusCode) {
        return statusCode == 408 // Request Timeout
                || statusCode == 502 // Bad Gateway
                || statusCode == 503 // Service Unavailable
                || statusCode == 504; // Gateway Timeout
    }

    private boolean isRetryableException(Exception e) {
        return e instanceof SocketTimeoutException
                || e instanceof ConnectException
                || e instanceof NoHttpResponseException
                || e instanceof ConnectionClosedException;
    }

    private static void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("API request retry was interrupted", e);
        }
    }
}

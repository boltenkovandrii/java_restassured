package com.abnamro.assignment.base;

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

import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static org.assertj.core.api.Assertions.assertThat;



// This class provides a base for making REST API calls with retry logic for GET requests.
// Would be nice to add handling of error 429 (Too Many Requests) with exponential backoff, but for now, we will just retry a fixed number of times with a fixed delay only for GET.
public class RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestBase.class);
    private final RequestSpecification reqSpec;
    private final RestAssuredConfig rConfig = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig())
            .logConfig(logConfig().blacklistHeader("PRIVATE-TOKEN"));

    private static final CompositeConfiguration config = TestConfig.getConfiguration();
    private final int maxAttempts = config.getInt("GETRetryAttempts", 3);
    private final long retryBaseDelay = config.getLong("GETRetryBaseDelay", 500);

    public RestBase(RequestSpecification reqSpec){
        this.reqSpec = reqSpec;
    }

    private RequestSpecification authenticatedRequest() {
        return given().config(rConfig).spec(reqSpec).header("PRIVATE-TOKEN", config.getString("GITLAB_ACCESS_TOKEN")).urlEncodingEnabled(false);
    }

    public Response getInternal(String URI) {
        return getInternal(URI, Map.of());
    }

    public Response getInternal(String URI, Map<String, ?> queryParams) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                LOGGER.info("Sending GET request on {} (attempt {}/{})", URI, attempt, maxAttempts);
                Response response = authenticatedRequest().queryParams(queryParams).when().get(URI);

                if (!needRetry(response.getStatusCode()) || attempt == maxAttempts) {
                    return response;
                }

                LOGGER.warn("GET request to {} returned status code {}. Retrying in {} ms (attempt {}/{})", URI, response.getStatusCode(), retryBaseDelay * attempt, attempt + 1, maxAttempts);
                sleep(retryBaseDelay * attempt);

            } catch (Exception e) {
                if (attempt == maxAttempts || !isRetryableException(e)) {
                    throw new RuntimeException(e);
                }

                LOGGER.error("GET request to {} failed with exception: {}. Retrying in {} ms (attempt {}/{})", URI, e.getMessage(), retryBaseDelay * attempt, attempt + 1, maxAttempts);
                sleep(retryBaseDelay * attempt);
            }
        }
        throw new IllegalStateException("Unexpected end of retry loop");
    }

    public Response postInternal(Object JSON, String URI) {
        return authenticatedRequest().body(JSON).when().post(URI);
    }

    public Response putInternal(Object JSON, String URI) {
        return authenticatedRequest().body(JSON).when().put(URI);
    }

    public Response deleteInternal(String URI) {
        return authenticatedRequest().when().delete(URI);
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
                || statusCode == 429 // Too Many Requests
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

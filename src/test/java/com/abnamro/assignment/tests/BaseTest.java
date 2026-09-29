package com.abnamro.assignment.tests;

import com.abnamro.assignment.base.TestConfig;
import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.model.IssueCreateRequest;
import io.restassured.response.Response;
import org.apache.commons.configuration2.CompositeConfiguration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.abnamro.assignment.base.IssuesAPI.getIssues;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseTest {
    private static final CompositeConfiguration config = TestConfig.getConfiguration();
    protected static final String TEST_ISSUE_PREFIX = config.getString("TEST_ISSUE_PREFIX");
    protected static final long PROJECT_ID = config.getLong("PROJECT_ID");
    protected static final long USER_ID = config.getLong("USER_ID");
    protected static final String PROJECT_PATH = config.getString("PROJECT_PATH");
    protected static final String USER_NAME = config.getString("USER_NAME");

    protected static final String NOT_FOUND_MESSAGE = "404 Not found";
    protected static final String ISSUE_NOT_FOUND_MESSAGE = "404 Issue Not Found";

    protected static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static  final Random random = new Random();

    protected String generateUniqueIssueTitle(String baseTitle) {
        long timestamp = System.currentTimeMillis();
        return TEST_ISSUE_PREFIX + " " + baseTitle + " " + timestamp + " " + getRandomLong();
    }

    /*
    // To use this function we need a unique string in title or description
    protected void checkIssueIsAbsent(long projectId, String searchQuery) {
        List<Issue> issues = getIssues(projectId, Map.of("search", searchQuery.replace(" ", "%20")));
        assertThat(issues).as("Check that issue with title '%s' is absent", searchQuery).isEmpty();
    }

     */

    // To use this function we need a unique string in title or description
    protected void checkIssueIsAbsent(long projectId, String searchQuery) {
        String encodedSearchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        List<Issue> issues = getIssues(projectId, Map.of("search", encodedSearchQuery));
        assertThat(issues).as("Check that issue with title '%s' is absent", searchQuery).isEmpty();
    }


    protected Long getRandomLong() {
        return random.nextLong(1, Integer.MAX_VALUE); // Use Integer.MAX_VALUE to avoid overflow issues
    }

    protected void assertIssueHasDefaultValues(Issue issue, String title) {
        assertThat(issue).as("Issue should not be null").isNotNull();
        assertThat(issue.title()).isEqualTo(title);
        assertThat(issue.author()).isNotNull();
        assertThat(issue.author().id()).isEqualTo(USER_ID);
        assertThat(issue.confidential()).isEqualTo(false);
        assertThat(issue.createdAt()).isNotNull();
        assertThat(issue.issueType()).isEqualTo("issue");
        assertThat(issue.labels()).isEmpty();
        assertThat(issue.description()).isNull();
    }

    protected void assertIssueMatchesRequest(Issue issue, IssueCreateRequest request) {
        assertThat(issue).as("Issue should not be null").isNotNull();

        assertThat(issue.title()).isEqualTo(request.getTitle());
        assertThat(issue.assignee().id()).isEqualTo(request.getAssigneeId());
        assertThat(issue.confidential()).isEqualTo(request.getConfidential());
        if (request.getCreatedAt() != null) {
            assertThat(issue.createdAt()).isEqualTo(Instant.parse(request.getCreatedAt()));
        }else {
            assertThat(issue.createdAt()).isNull();
        }
        assertThat(issue.description()).isEqualTo(request.getDescription());
        assertThat(issue.dueDate()).isEqualTo(LocalDate.parse(request.getDueDate()));
        assertThat(issue.issueType()).isEqualTo(request.getIssueType());
        assertThat(issue.labels()).containsExactlyInAnyOrder(request.getLabels().split(","));
        assertThat(issue.startDate()).isEqualTo(LocalDate.parse(request.getStartDate()));
    }

    protected void verifyBasicResponseFields(Issue issue, String status){
        //Not all generated fields are verified here - just basic ones to ensure that the issue is created correctly.
        assertThat(issue.id()).isPositive();
        assertThat(issue.project_id()).isEqualTo(Math.toIntExact(PROJECT_ID)); //only used for this project
        assertThat(issue.state()).isEqualTo(status);
        assertThat(issue.webUrl()).isNotBlank();
        assertThat(issue.updatedAt()).isNotNull();
    }

    protected static void assertStatus(Response response, int expectedStatusCode) {
        assertThat(response.statusCode()).isEqualTo(expectedStatusCode);
    }

    protected static void assertMessage(Response response, int expectedStatusCode, String expectedMessage) {
        assertStatus(response, expectedStatusCode);
        assertThat(response.jsonPath().getString("message")).contains(expectedMessage);
    }

    protected static void assertError(Response response, int expectedStatusCode, String expectedMessage) {
        assertStatus(response, expectedStatusCode);
        assertThat(response.jsonPath().getString("error")).contains(expectedMessage);
    }


}

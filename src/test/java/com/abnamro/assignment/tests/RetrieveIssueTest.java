package com.abnamro.assignment.tests;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.model.IssueCreateRequest;
import com.abnamro.assignment.model.IssueUpdateRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class RetrieveIssueTest extends BaseTest{

    /*
        Basic tests for issue retrieval logic both positive and negative scenarios
        Partially covered in other tests, but this class is dedicated to retrieval scenarios
     */

    @Test
    @DisplayName("Retrieve1: Retrieve existing issue")
    void retrieveExistingIssueTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest =
                new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                        .setDescription("Issue created for retrieve test.")
                        .setLabels("retrieve,test");

        Issue created = createIssue(createRequest);

        // Retrieve issue
        Issue retrieved = getIssue(created.iid());

        // Verify response
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        verifyBasicResponseFields(retrieved, "opened");
        assertThat(retrieved.iid()).isEqualTo(created.iid());
        assertThat(retrieved.id()).isEqualTo(created.id());
        assertThat(retrieved.title()).isEqualTo(createRequest.getTitle());
        assertThat(retrieved.description()).isEqualTo(createRequest.getDescription());
        assertThat(retrieved.labels()).containsExactlyInAnyOrder("retrieve", "test");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Retrieve2: Retrieve issue after update")
    void retrieveIssueAfterUpdateTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Original title"));
        Issue created = createIssue(createRequest);

        // Update the issue
        String updatedTitle = generateUniqueIssueTitle("Updated title");
        String updatedDescription = "Updated description.";

        updateIssue(created.iid(), new IssueUpdateRequest()
                .setTitle(updatedTitle)
                .setDescription(updatedDescription)
                .setLabels("updated,retrieve"));

        // Retrieve issue
        Issue retrieved = getIssue(created.iid());

        // Verify persisted state
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        verifyBasicResponseFields(retrieved, "opened");
        assertThat(retrieved.iid()).isEqualTo(created.iid());
        assertThat(retrieved.title()).isEqualTo(updatedTitle);
        assertThat(retrieved.description()).isEqualTo(updatedDescription);
        assertThat(retrieved.labels())
                .containsExactlyInAnyOrder("updated", "retrieve");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Retrieve3: Retrieve issue using URL encoded project path")
    void retrieveIssueUsingUrlEncodedProjectPathTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("URL encoded retrieve test"));
        Issue created = createIssue(createRequest);

        // Retrieve issue using URL encoded project path
        Issue retrieved = getIssueRaw(PROJECT_PATH, created.iid()).as(Issue.class);

        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        verifyBasicResponseFields(retrieved, "opened");
        assertThat(retrieved.iid()).isEqualTo(created.iid());
        assertThat(retrieved.title()).isEqualTo(createRequest.getTitle());

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("RetrieveErrors1: Retrieve non-existing issue")
    void retrieveNonExistingIssueTest() {
        // Try to retrieve an issue with a non-existing IID
        Response response = getIssueRaw(PROJECT_ID,1_000_000L);
        assertMessage(response, 404, NOT_FOUND_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -999, Long.MAX_VALUE})
    @DisplayName("RetrieveErrors2: Retrieve issue with invalid IID")
    void retrieveIssueInvalidIidTest(long issueIid) {
        // Try to retrieve an issue with an invalid IID
        Response response = getIssueRaw(PROJECT_ID, issueIid);
        assertMessage(response, 404, NOT_FOUND_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})
    @DisplayName("RetrieveErrors3: Retrieve issue with invalid project ID")
    void retrieveIssueInvalidProjectIdTest(long projectId) {
        // Try to retrieve an issue with an invalid project ID
        Response response = getIssueRaw(String.valueOf(projectId),1);
        assertThat(response.statusCode()).isIn(400, 404);
    }
}

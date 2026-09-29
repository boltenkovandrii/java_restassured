package com.abnamro.assignment.tests;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.model.IssueCreateRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class DeleteIssueTest extends BaseTest{

    /*
        Basic tests for issue deletion logic both positive and negative scenarios
        Partially covered in other tests, but this class is dedicated to deletion scenarios
     */


    @Test
    @DisplayName("Delete1: Delete existing issue")
    void deleteExistingIssueTest() {
        // Create an issue to delete
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Delete test"));
        Issue created = createIssue(createRequest);

        // Delete the created issue
        Response response = deleteIssueRaw(PROJECT_ID, created.iid());

        // Verify the response (not so different from standard deleteIssue() method, TBH)
        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.body().asString()).isEmpty();
    }

    @Test
    @DisplayName("Delete2: Deleted issue cannot be retrieved")
    void deletedIssueCannotBeRetrievedTest() {
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Delete retrieve test"));
        Issue created = createIssue(createRequest);

        // Delete and verify response
        Response deleteResponse = deleteIssueRaw( PROJECT_ID, created.iid());
        assertThat(deleteResponse.statusCode()).isEqualTo(204);

        // Verify deletion
        Response retrieveResponse = getIssueRaw(PROJECT_ID, created.iid());
        assertMessage(retrieveResponse, 404, NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("Delete3: Delete issue using URL encoded project path")
    void deleteIssueUsingUrlEncodedProjectPathTest() {
        // Create an issue to delete
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("URL encoded delete test")));

        // Delete the created issue using URL encoded project path
        Response response = deleteIssueRaw(PROJECT_PATH, created.iid());
        assertThat(response.statusCode()).isEqualTo(204);

        // Verify deletion
        Response retrieveResponse = getIssueRaw(PROJECT_ID, created.iid());
        assertThat(retrieveResponse.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("DeleteErrors1: Delete non-existing issue")
    void deleteNonExistingIssueTest() {
        // Attempt to delete an issue with a non-existing IID
        Response response = deleteIssueRaw(PROJECT_ID,1_000_000L);
        assertMessage(response, 404, ISSUE_NOT_FOUND_MESSAGE);

    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -999, Long.MAX_VALUE})
    @DisplayName("DeleteErrors2: Delete issue with invalid IID")
    void deleteIssueInvalidIidTest(long issueIid) {
        // Attempt to delete an issue with an invalid IID
        Response response = deleteIssueRaw(PROJECT_ID,  issueIid);
        assertThat(response.statusCode()).isEqualTo(404);
    }

    @ParameterizedTest
    @ValueSource(strings = {"non-existing-group/non-existing-project", "null"})
    @DisplayName("RetrieveErrors4: Retrieve issue using invalid project path")
    void retrieveIssueInvalidProjectPathTest(String projectPath) {
        // Attempt to retrieve an issue using an invalid project path
        Response response = getIssueRaw(projectPath, 1);
        assertThat(response.statusCode()).isEqualTo(404);
    }


}

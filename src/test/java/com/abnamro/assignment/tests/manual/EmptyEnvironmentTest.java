package com.abnamro.assignment.tests.manual;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.model.IssueCreateRequest;
import com.abnamro.assignment.model.IssueUpdateRequest;
import com.abnamro.assignment.tests.BaseTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("EmptyEnvironmentTests")
public class EmptyEnvironmentTest extends BaseTest {

    /*
     * These tests require the project to contain no issues.
     * Run manually after ResetTest.
     *
     * Test methods in this class must execute sequentially because
     * createIssueEmptyEnvTest temporarily changes the environment.
     */


    @Test
    @DisplayName("EmptyEnv1: Retrieve issue list for an empty environment")
    public void getIssuesEmptyEnvTest() {
        List<Issue> issues = getIssues();
        assertThat(issues).as("Check that issues  response is not null").isNotNull();
        assertThat(issues).as("Check the issue id").hasSize(0);
    }

    @Test
    @DisplayName("EmptyEnv2: Attempt to retrieve issue for an empty environment")
    public void getIssueEmptyEnvTest() {
        Response response = getIssueRaw(PROJECT_ID, 1);
        assertMessage(response, 404, NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("EmptyEnv3: Attempt to delete issue for an empty environment")
    public void deleteIssueEmptyEnvTest() {
        Response response = deleteIssueRaw(PROJECT_ID, 1);
        assertMessage(response, 404, ISSUE_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("EmptyEnv4: Attempt to update issue for an empty environment")
    public void updateIssueEmptyEnvTest() {

        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setTitle(generateUniqueIssueTitle("Update for empty env test"))
                .setStateEvent("close");

        Response response = updateIssueRaw(PROJECT_ID, 1, updateRequest);
        assertMessage(response, 404, NOT_FOUND_MESSAGE);
    }


    @Test
    @DisplayName("EmptyEnv5: Create issue for an empty environment")
    public void createIssueEmptyEnvTest() {
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Create for empty env test"))
                .setDescription("Create for empty env test")
                .setLabels("self-check");
        Issue created = createIssue(createRequest);

        // it is important to clean up the created issue after the test, so it will not break 'empty environment' state, so we use a try-finally block
        try {
            assertThat(created).as("Created issue should not be null").isNotNull();
            assertThat(created.title()).isEqualTo(createRequest.getTitle());
        } finally {
            // Clean up
            if(created!=null){
                deleteIssue(created.iid());
            }
        }
    }

}


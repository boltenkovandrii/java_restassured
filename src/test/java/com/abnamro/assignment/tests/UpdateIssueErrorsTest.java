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

import java.util.List;
import java.util.Map;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class UpdateIssueErrorsTest extends BaseTest {

    /*
        Basic tests for issue update logic with focus on error\negative scenarios
     */

    @Test
    @DisplayName("UpdateErrors1: Update issue with empty request (no fields to update)")
    void updateIssueEmptyRequestTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test title"));
        Issue created = createIssue(createRequest);

        // Build and send empty update request
        IssueUpdateRequest updateRequest = new IssueUpdateRequest();
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertError(response, 400, "at least one parameter must be provided");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        verifyBasicResponseFields(retrieved, "opened");
        assertIssueHasDefaultValues(retrieved, createRequest.getTitle());

        // Clean up
        deleteIssue(created.iid());
    }


    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})
    @DisplayName("UpdateErrors2: Update issue with invalid numeric project ID")
    void updateIssueInvalidProjectIdTest(long projectId) {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid project ID
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setTitle("Updated title");
        Response response = updateIssueRaw(projectId, created.iid(), updateRequest);

        // check response
        assertMessage(response, 404, "404 Project Not Found");

        // Clean up
        deleteIssue(created.iid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"non-existing-project", "does-not-exist%2Fproject", "null" })
    @DisplayName("UpdateErrors3: Update issue with invalid string project ID")
    void updateIssueInvalidProjectIdStringTest(String projectId) {
        //  Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid string project ID
        Response response = updateIssueRaw(
                projectId,
                created.iid(),
                new IssueUpdateRequest().setTitle("Updated title"));

        // check response
        assertMessage(response, 404, "404 Project Not Found");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors4: Update issue with invalid string project ID, containing spaces")
    void updateIssueInvalidProjectIdWithSpacesTest() {
        //  Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid string project ID containing spaces
        Response response = updateIssueRaw(
                "project%20name%20with%20spaces",
                created.iid(),
                new IssueUpdateRequest().setTitle("Updated title"));

        // check response
        assertMessage(response, 404, "404 Project Not Found");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors5: Update non-existing issue")
    void updateNonExistingIssueTest() {
        // Build and send update request for a non-existing issue
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setTitle("Updated title");

        // check response
        Response response = updateIssueRaw(
                PROJECT_ID,
                Integer.MAX_VALUE,
                updateRequest);

        assertMessage(response, 404, NOT_FOUND_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -999, Long.MAX_VALUE})
    @DisplayName("UpdateErrors6: Update issue with invalid issue IID")
    void updateIssueInvalidIidTest(long issueIid) {
        // Build and send update request with invalid issue IID
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setTitle("Updated title");

        // check response
        Response response = updateIssueRaw(
                PROJECT_ID,
                issueIid,
                updateRequest);

        //  check response
        assertMessage(response, 404, NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("UpdateErrors7: Update issue with non-string add_labels")
    void updateIssueAddLabelsWrongTypeTest() {
        // Prepare and send create issue
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with non-string add_labels
        Map<String, Object> body = Map.of("add_labels", 12345);
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response - non-string labels are ignored, and the issue is updated with no labels added
        assertThat(response.statusCode()).as("Response code should be 200, but was "+ response.statusCode()).isEqualTo(200);
        Issue updated = response.as(Issue.class);
        assertThat(updated).as("Created issue should not be null").isNotNull();
        assertThat(updated.labels()).as("Labels should be empty").isEmpty();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors8: Update issue with non-existing assignee")
    void updateIssueNonExistingAssigneeTest() {
        // Prepare and send create issue request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with non-existing assignee
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setAssigneeIds(List.of(999999999L));
        Issue updated = updateIssue(created.iid(), updateRequest);

        // check response - the assignee is ignored, and the issue is updated with no assignee added
        assertThat(updated).as("Created issue should not be null").isNotNull();
        assertThat(updated.assignees()).as("Assignees should be empty").isEmpty();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors9: Update issue with invalid assignee_ids type")
    void updateIssueAssigneeIdsWrongTypeTest() {
        Issue created = createIssue(
                new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid assignee_ids type (string instead of array of numbers)
        Map<String, Object> body = Map.of("assignee_ids", "123");
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response - the invalid assignee_ids are ignored, and the issue is updated with no assignee added
        assertThat(response.statusCode()).as("Response code should be 200, but was "+ response.statusCode()).isEqualTo(200);
        Issue updated = response.as(Issue.class);
        assertThat(updated).as("Created issue should not be null").isNotNull();
        assertThat(updated.assignees()).as("Assignees should be empty").isEmpty();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors10: Update issue with invalid confidential type")
    void updateIssueConfidentialWrongTypeTest() {
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid confidential type (string instead of boolean)
        Map<String, Object> body = Map.of("confidential", "trueee");
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response
        assertError(response, 400, "confidential is invalid");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.confidential()).as("Confidential should be false").isFalse();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors11: Update issue with description exceeding maximum length")
    void updateIssueDescriptionTooLongTest() {
        // Prepare and send create issue request
        Issue created = createIssue( new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with description exceeding maximum length (1 MiB)
        String description = "A".repeat(1_048_577);
        IssueUpdateRequest updateRequest =  new IssueUpdateRequest().setDescription(description);
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        //  check response
        assertMessage(response, 400, "[description:[is too long (1 MiB). The maximum size is 1 MiB.]]");

        //clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors12: Update issue with invalid due date")
    void updateIssueInvalidDueDateTest() {
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid due date
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setDueDate("2024-02-30"); // Invalid date (February 30th does not exist)
        Issue updated = updateIssue(created.iid(), updateRequest);

        // check that the due date was not updated
        assertThat(updated.dueDate()).as("Due date should be null").isNull();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors13: Update issue with due date before start date")
    void updateIssueDueDateBeforeStartDateTest() {
        // Prepare and send create issue request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with due date before start date
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setStartDate("2024-02-15").setDueDate("2024-02-10");
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertMessage(response, 400, "[due_date:[must be greater than or equal to start date]]");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors14: Update issue with invalid discussion_locked type")
    void updateIssueDiscussionLockedWrongTypeTest() {
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid discussion_locked type (string instead of boolean)
        Map<String, Object> body = Map.of("discussion_locked", "trueee");
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response
        assertError(response, 400, "discussion_locked is invalid");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.discussionLocked()).as("Discussion locked should remain null").isNull();

        // Clean up
        deleteIssue(created.iid());
    }


    @ParameterizedTest
    @ValueSource(strings = {"invalid value", "", "123!@#$%^&*()_+-=[]{}|;':\",.<>/?`~"})
    @DisplayName("UpdateErrors15: Attempt to update issue type to an invalid value")
    void updateIssueTypeWithInvalidValueTest(String issueType) {
        // Create an issue to update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue type to " + issueType)));

        // build and send update request with invalid issue type
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setIssueType(issueType);
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertError(response, 400, "issue_type does not have a valid value");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.issueType()).isEqualTo("issue"); // default issue type

        deleteIssue(created.iid());
    }

    // 'task' is not a valid issue type for update request according to GitLab API docs
    @Test
    @DisplayName("UpdateErrors16: Attempt to update issue type to 'task' (not allowed)")
    void updateIssueTypeToTaskTest() {
        // Create an issue to update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue type to task")));

        // build and send update request with 'task' issue type
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setIssueType("task");
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertMessage(response, 400, "[work_item_type_id:[can not be changed to Task]]");

        //clean up
        deleteIssue(created.iid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"issue", "incident"})
    @DisplayName("UpdateErrors17: Attempt to change issue type from 'task' to another value")
    void updateIssueTypeFromTaskToAnotherValueTest(String issueType) {
        // Create an issue with 'task' type
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Create task issue"))
                .setIssueType("task");
        Issue created = createIssue(createRequest);

        // build and send update request to change issue type to another value
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setIssueType(issueType);
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertMessage(response, 400, "[work_item_type_id:[can not be changed to ");

        //clean up
        deleteIssue(created.iid());

    }


    @Test
    @DisplayName("UpdateErrors18: Update issue with non-string issue type")
    void updateIssueIssueTypeWrongTypeTest() {
        // Create an issue to update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue type to non-string value")));

        // build and send update request with non-string issue type
        Map<String, Object> body = Map.of("issue_type", 12345);
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response
        assertError(response, 400, "issue_type does not have a valid value");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.issueType()).isEqualTo("issue"); // default issue type

        deleteIssue(created.iid());
    }

    @Test
    @Tag("WIP") // FINDING-6 sending non-string labels in update request is accepted by GitLab API, but request is not ignored and actually updates the issue labels to an empty list. This is unexpected behavior and should be investigated further.
    @DisplayName("UpdateErrors19: Update issue with non-string labels")
    void updateIssueLabelsWrongTypeTest() {
        // Create an issue to update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue labels to non-string value"))
                .setLabels("initial-label"));

        // build and send update request with non-string labels
        Map<String, Object> body = Map.of("labels", 12345);
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response
        assertThat(response.statusCode()).as("Response code should be 200, but was "+ response.statusCode()).isEqualTo(200);
        Issue updated = response.as(Issue.class);
        assertThat(updated).as("Created issue should not be null").isNotNull();
        assertThat(updated.labels()).as("Labels should not be updated").containsExactlyInAnyOrder("initial-label");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.labels()).as("Labels should not be updated").containsExactlyInAnyOrder("initial-label");

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors20: Update issue with milestone and milestone ID")
    void updateIssueWithMilestoneAndMilestoneIdTest() {
        // Create an issue to update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue with milestone and milestone ID")));

        // build and send update request with both milestone and milestone_id
        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setMilestone("v1.0")
                .setMilestoneId(1L);
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertError(response, 400, "milestone_id, milestone are mutually exclusive");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.milestone()).as("Milestone should be null").isNull();

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors21: Update issue with non-string remove_labels")
    void updateIssueRemoveLabelsWrongTypeTest() {
        // Create an issue to update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue remove_labels to non-string value"))
                .setLabels("initial-label"));

        // build and send update request with non-string remove_labels
        Map<String, Object> body = Map.of("remove_labels", 12345);
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response - non-string remove_labels are ignored, and the issue is updated with no labels removed
        assertThat(response.statusCode()).as("Response code should be 200, but was "+ response.statusCode()).isEqualTo(200);
        Issue updated = response.as(Issue.class);
        assertThat(updated).as("Created issue should not be null").isNotNull();
        assertThat(updated.labels()).as("Labels should not be updated").containsExactlyInAnyOrder("initial-label");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.labels()).as("Labels should not be updated").containsExactlyInAnyOrder("initial-label");

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors22: Update issue with invalid severity")
    void updateIssueInvalidSeverityTest() {
        // Create an issue to update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue with invalid severity"))
                .setIssueType("incident")
        );

        // build and send update request with invalid severity
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setSeverity("invalid-severity");
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertError(response, 400, "severity does not have a valid value");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.severity()).as("Severity should be UNKNOWN").isEqualTo("UNKNOWN");

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors23: Update issue with non-string severity")
    void updateIssueSeverityWrongTypeTest() {
        // Create an issue to update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue severity to non-string value"))
                .setIssueType("incident")
        );

        // build and send update request with non-string severity
        Map<String, Object> body = Map.of("severity", 12345);
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response
        assertError(response, 400, "severity does not have a valid value");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.severity()).as("Severity should be UNKNOWN").isEqualTo("UNKNOWN");

        deleteIssue(created.iid());
    }

    //FINDING-1 - looks like current GitLab configuration supports only 3 issue types: issue, incident, task. Test case for test_case is commented out.
    @ParameterizedTest
    @ValueSource(strings = {"issue", "task"})
//    @ValueSource(strings = {"issue", "task", "test_case"})
    @DisplayName("UpdateErrors24: Attempt to update severity for non-incident issue")
    void updateSeverityForNonIncidentIssueTest(String issueType) {
        // Create a regular issue (not an incident)
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                .setIssueType(issueType)
        );

        //  build and send update request to set severity
        Issue updated = updateIssue(created.iid(), new IssueUpdateRequest().setSeverity("high"));

        // check response
        assertThat(updated.severity()).isEqualTo("UNKNOWN"); // Severity remains UNKNOWN for non-incident issues

        //clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors25: Update issue with invalid start date")
    void updateIssueInvalidStartDateTest() {
        // Prepare and send create issue request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid start date
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setStartDate("2024-02-30"); // Invalid date (February 30th does not exist)
        Issue updated = updateIssue(created.iid(), updateRequest);

        //check that the start date was not updated
        assertThat(updated.startDate()).as("Start date should be null").isNull();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors26: Update issue with non-string start date")
    void updateIssueStartDateWrongTypeTest() {
        // Prepare and send create issue request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with non-string start date
        Map<String, Object> body = Map.of("start_date", 12345);
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        //check that the start date was not updated
        Issue updated = response.as(Issue.class);
        assertThat(updated).as("Created issue should not be null").isNotNull();
        assertThat(updated.startDate()).as("Start date should be null").isNull();

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.startDate()).as("Start date should be null").isNull();

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors27: Update issue with invalid state event")
    void updateIssueInvalidStateEventTest() {

        // Prepare and send create issue request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with invalid state event
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setStateEvent("invalid_state");
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertError(response, 400, "state_event does not have a valid value");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.state()).as("State should remain 'opened'").isEqualTo("opened");

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors28: Update issue with non-string state event")
    void updateIssueStateEventWrongTypeTest() {
        // Prepare and send create issue request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with non-string state event
        Map<String, Object> body = Map.of("state_event", 12345);
        Response response = getRestBase().putInternal(body, projectIssuesUrl(PROJECT_ID) + "/" + created.iid());

        // check response
        assertError(response, 400, "state_event does not have a valid value");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.state()).as("State should remain 'opened'").isEqualTo("opened");

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors29: Update issue with empty title")
    void updateIssueEmptyTitleTest() {
        // Prepare and send create issue request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with empty title
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setTitle("");
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertMessage(response, 400, "[title:[can't be blank]]");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.title()).as("Title should remain unchanged").isEqualTo(created.title());

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors30: Update issue with title exceeding maximum length")
    void updateIssueTitleTooLongTest() {
        // Prepare and send create issue request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title")));

        // Build and send update request with title exceeding maximum length (255 characters)
        String longTitle = "A".repeat(256);
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setTitle(longTitle);
        Response response = updateIssueRaw(PROJECT_ID, created.iid(), updateRequest);

        // check response
        assertMessage(response, 400, "[title:[is too long (maximum is 255 characters)]]");

        // Retrieve the issue and check that it was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        assertThat(retrieved.title()).as("Title should remain unchanged").isEqualTo(created.title());

        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("UpdateErrors31: Attempt to send update request with only updated_at field(requires admin rights)")
    void updateIssueUpdatedAtOnlyTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update updated_at field to a specific timestamp")));

        // Build and send update request to set the issue updated_at field to a specific timestamp
        String newUpdatedAt = "2024-01-01T12:00:00Z";
        Response response = updateIssueRaw(
                PROJECT_ID,
                created.iid(),
                new IssueUpdateRequest().setUpdatedAt(newUpdatedAt));

        // check response
        assertError(response, 400, "at least one parameter must be provided");

        // Clean up
        deleteIssue(created.iid());

    }

}

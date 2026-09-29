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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class UpdateIssueTest extends BaseTest {

    /*
        Basic tests for issue creation logic with focus on happy path.
     */


    @Test
    @DisplayName("Update1: Update issue title")
    void updateIssueTitleTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test title"));
        Issue created = createIssue(createRequest);

        // Build and send update request
        String newTitle = generateUniqueIssueTitle("Updated title");
        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setTitle(newTitle);
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly
        assertThat(updated).as("Created issue should not be null").isNotNull();
        verifyBasicResponseFields(updated, "opened");
        assertIssueHasDefaultValues(updated, newTitle);

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        verifyBasicResponseFields(retrieved, "opened");
        assertIssueHasDefaultValues(retrieved, newTitle);

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update2: Add labels to issue")
    void updateIssueAddLabelsTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Add labels to issue"));
        Issue created = createIssue(createRequest);

        // Build and send update request to add labels
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setAddLabels("bug,backend,api");
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly
        assertThat(updated.labels()).containsExactlyInAnyOrder("bug", "backend", "api");

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.labels()).containsExactlyInAnyOrder("bug", "backend", "api");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update3: Add labels to issue which already has labels")
    void updateIssueAddLabelsToExistingLabelsTest() {
        // Prepare and send create request with initial labels
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Add labels to issue which already has labels"))
                .setLabels("frontend,ui");
        Issue created = createIssue(createRequest);

        // Build and send update request to add more labels, including some that already exist
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setAddLabels("bug, backend, ui, api");
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly with both old and new labels
        assertThat(updated.labels()).containsExactlyInAnyOrder("frontend", "ui", "bug", "backend", "api");

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.labels()).containsExactlyInAnyOrder("frontend", "ui", "bug", "backend", "api");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update4: Add labels to issue which already has all these labels")
    void updateIssueAddLabelsToExistingLabelsNoDuplicatesTest() {
        // Prepare and send create request with initial labels
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Add labels to issue which already has all these labels"))
                .setLabels("bug,backend,api");
        Issue created = createIssue(createRequest);

        // Build and send update request to add the same labels again
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setAddLabels("bug, backend, api");
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly and no duplicates are present
        assertThat(updated.labels()).containsExactlyInAnyOrder("bug", "backend", "api");

        // Retrieve the issue and check that it is updated correctly and no duplicates are present
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.labels()).containsExactlyInAnyOrder("bug", "backend", "api");

        // Clean up
        deleteIssue(created.iid());
    }


    //Only one user is configured for this assignment, but in the future, would be nice to add a test for assigning multiple users to an issue, and then unassigning them.
    @Test
    @DisplayName("Update5: Update issue assignee")
    void updateIssueAssigneeTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Update issue assignee"));
        Issue created = createIssue(createRequest);

        // Build and send update request to set assignee
        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setAssigneeIds(List.of(USER_ID));
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly
        assertThat(updated.assignee()).isNotNull();
        assertThat(updated.assignee().id()).isEqualTo(USER_ID);

        //cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Update6: Unassign all users from issue")
    void updateIssueUnassignAllTest() {
        // Prepare and send create request with an assignee
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Unassign all users from issue"))
                        .setAssigneeId(USER_ID);
        Issue created = createIssue(createRequest);

        // Build and send update request to unassign all users
        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setAssigneeIds(List.of(0L));
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly and has no assignees
        assertThat(updated.assignees()).isEmpty();

        //cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Update7: Make issue confidential")
    void updateIssueConfidentialTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest =  new IssueCreateRequest(generateUniqueIssueTitle("Make issue confidential"));
        Issue created = createIssue(createRequest);

        // Build and send update request to make the issue confidential
        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setConfidential(true);
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly
        assertThat(updated.confidential()).isTrue();

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.confidential()).isTrue();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update8: Make confidential issue public")
    void updateIssueConfidentialFalseTest() {
        // Prepare and send create request with confidential set to true
        IssueCreateRequest createRequest =  new IssueCreateRequest(generateUniqueIssueTitle("Make confidential issue public"))
                .setConfidential(true);
        Issue created = createIssue(createRequest);

        // Build and send update request to make the issue public
        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setConfidential(false);
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly
        assertThat(updated.confidential()).isFalse();

        //cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Update9: Update issue description")
    void updateIssueDescriptionTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue description")));

        // Build and send update request to update the issue description
        String description = "Updated issue description";
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setDescription(description));

        // Check that the issue is updated correctly
        assertThat(updated.description()).isEqualTo(description);

        //cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Update10: Update issue description with maximum allowed length")
    void updateIssueDescriptionMaxLengthTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue description with maximum allowed length")));

        // Build and send update request to update the issue description with maximum allowed length (1,048,576 characters)
        String description = "A".repeat(1_048_576);
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setDescription(description));

        // Check that the issue is updated correctly
        assertThat(updated.description()).isEqualTo(description);

        //cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update11: Lock issue discussion")
    void updateIssueDiscussionLockedTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Lock issue discussion")));

        // Build and send update request to lock the issue discussion
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setDiscussionLocked(true));

        // Check that the issue is updated correctly
        assertThat(updated.discussionLocked()).isTrue();

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.discussionLocked()).isTrue();


        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update12: Unlock issue discussion")
    void updateIssueDiscussionUnlockedTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Customize Toolbar...")));

        // Build and send update request to lock the issue discussion
        updateIssue(
                created.iid(),
                new IssueUpdateRequest().setDiscussionLocked(true));

        // Build and send update request to unlock the issue discussion
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setDiscussionLocked(false));

        // Check that the issue is updated correctly
        assertThat(updated.discussionLocked()).isFalse();

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.discussionLocked()).isFalse();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update13: Update issue due date")
    void updateIssueDueDateTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue due date")));

        // Build and send update request to set the issue due date to 10 days from now
        String dueDate = LocalDate.now().plusDays(10).toString();
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setDueDate(dueDate));

        // Check that the issue is updated correctly
        assertThat(updated.dueDate()).isEqualTo(LocalDate.parse(dueDate));

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.dueDate()).isEqualTo(LocalDate.parse(dueDate));

        // clean the issue due date by setting it to empty value
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("due_date", "");
        Response response = getRestBase().putInternal(requestBody, projectIssuesUrl(PROJECT_ID)+"/"+created.iid());

        // Check that the issue is updated correctly
        assertThat(response.statusCode()).as("Response code should be 200, but was "+ response.statusCode()).isEqualTo(200);
        Issue updated1 = response.as(Issue.class);
        assertThat(updated1.dueDate()).isNull();

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved1 = getIssue(created.iid());
        assertThat(retrieved1.dueDate()).isNull();

        // Clean up
        deleteIssue(created.iid());
    }


    //FINDING-1 - looks like current GitLab configuration supports only 3 issue types: issue, incident, task. Test case for test_case is commented out.
    @ParameterizedTest
    @ValueSource(strings = {"issue", "incident"}) // 'task' is not a valid issue type for update request according to GitLab API docs
//    @ValueSource(strings = {"issue", "incident", "test_case"}) // 'task' is not a valid issue type for update request according to GitLab API docs
    @DisplayName("Update14: Update issue type")
    void updateIssueTypeTest(String issueType) {
        Issue created = createIssue(
                new IssueCreateRequest(generateUniqueIssueTitle("Update issue type to " + issueType)));

        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setIssueType(issueType));

        assertThat(updated.issueType()).isEqualTo(issueType);

        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Update15: Replace issue labels")
    void updateIssueLabelsTest() {
        // Prepare and send create request with initial labels
        IssueCreateRequest createRequest =  new IssueCreateRequest(generateUniqueIssueTitle("Replace issue labels"))
                        .setLabels("old-label,another-label,reused-label");
        Issue created = createIssue(createRequest);

        // Build and send update request to replace the issue labels
        IssueUpdateRequest updateRequest = new IssueUpdateRequest().setLabels("new-label,api, reused-label");
        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly
        assertThat(updated.labels())
                .containsExactlyInAnyOrder("new-label", "api", "reused-label");

        //cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update16: Clear all issue labels")
    void updateIssueClearLabelsTest() {
        // Prepare and send create request with initial labels
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Clear all issue labels"))
                .setLabels("bug,backend");
        Issue created = createIssue(createRequest);

        // Build and send update request to remove all issue labels
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setLabels(""));

        // Check that the issue is updated correctly
        assertThat(updated.labels()).isEmpty();

        //retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.labels()).isEmpty();

        //cleanup
        deleteIssue(created.iid());
    }

    //no tests for updating milestone, or milestone_id, as this assignment does not include any milestone setup. In future, it would be nice to add tests for updating milestone, and milestone_id.

    @Test
    @DisplayName("Update17: Remove selected issue labels")
    void updateIssueRemoveLabelsTest() {
        // Prepare and send create request with initial labels
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Remove selected issue labels"))
                .setLabels("bug,backend,api");
        Issue created = createIssue(createRequest);

        // Build and send update request to remove selected issue labels
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setRemoveLabels("backend"));

        // Check that the issue is updated correctly
        assertThat(updated.labels())
                .containsExactlyInAnyOrder("bug", "api");

        //cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update18: Remove non-existing issue label")
    void updateIssueRemoveNonExistingLabelTest() {
        // Prepare and send create request with initial labels
        IssueCreateRequest createRequest =  new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                .setLabels("bug");
        Issue created = createIssue(createRequest);

        // Build and send update request to remove a non-existing issue label
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setRemoveLabels("does-not-exist"));

        assertThat(updated.labels()).containsExactly("bug");

        //cleanup
        deleteIssue(created.iid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "low", "medium", "high", "critical"})
    @DisplayName("Update19: Update incident severity")
    void updateIncidentSeverityTest(String severity) {
        // Prepare and send create request for an incident issue
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                .setIssueType("incident"));

        // Build and send update request to set the incident severity
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setSeverity(severity));

        // Check that the issue is updated correctly
        assertThat(updated.severity()).isEqualTo(severity.toUpperCase());

        //cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update20: Update issue start date")
    void updateIssueStartDateTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue start date")));

        // Build and send update request to set the issue start date to 5 days ago
        String startDate = LocalDate.now().minusDays(5).toString();
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setStartDate(startDate));

        // Check that the issue is updated correctly
        assertThat(updated.startDate()).isEqualTo(LocalDate.parse(startDate));

        //cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update21: Update issue with start date equal to due date")
    void updateIssueStartDateEqualDueDateTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue with start date equal to due date")));

        // Build and send update request to set the issue start date and due date to 5 days from now
        String date = LocalDate.now().plusDays(5).toString();
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest()
                        .setStartDate(date)
                        .setDueDate(date));

        // Check that the issue is updated correctly
        assertThat(updated.startDate()).isEqualTo(LocalDate.parse(date));
        assertThat(updated.dueDate()).isEqualTo(LocalDate.parse(date));

        //cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update22: Close issue")
    void updateIssueCloseTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Close issue")));

        // Build and send update request to close the issue
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setStateEvent("close"));

        // Check that the issue is updated correctly
        assertThat(updated.state()).isEqualTo("closed");

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.state()).isEqualTo("closed");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update23: Reopen issue")
    void updateIssueReopenTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Reopen issue"))
                .setIssueType("incident")); //setting issue type to incident to test that reopening works for different issue types

        //  Build and send update request to close the issue first
        updateIssue(
                created.iid(),
                new IssueUpdateRequest().setStateEvent("close"));

        // Build and send update request to reopen the issue
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setStateEvent("reopen"));

        // Check that the issue is updated correctly
        assertThat(updated.state()).isEqualTo("opened");

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.state()).isEqualTo("opened");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update24: Update updated_at field to a specific timestamp (requires admin rights)")
    void updateIssueUpdatedAtTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update updated_at field to a specific timestamp")));

        // Build and send update request to set the issue updated_at field to a specific timestamp
        String newUpdatedAt = "2024-01-01T12:00:00Z";
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setUpdatedAt(newUpdatedAt).setLabels("updated-at-test")); //we need to set at least one other field in the update request, otherwise GitLab API returns 400 error


        // Check that the issue is updated correctly
        assertThat(updated.updatedAt()).isEqualTo(java.time.Instant.parse(newUpdatedAt));

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.updatedAt()).isEqualTo(java.time.Instant.parse(newUpdatedAt));

        // Clean up
        deleteIssue(created.iid());

    }

    @Test
    @DisplayName("Update25: Update all supported issue fields")
    void updateIssueAllFieldsTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Original title"));
        Issue created = createIssue(createRequest);

        // Build and send update request to update all supported issue fields
        String newTitle = generateUniqueIssueTitle("Updated title");
        String description = "Updated description";
        String dueDate = LocalDate.now().plusDays(10).toString();
        String startDate = LocalDate.now().plusDays(5).toString();

        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setAssigneeIds(List.of(USER_ID))
                .setConfidential(true)
                .setDescription(description)
                .setDiscussionLocked(true)
                .setDueDate(dueDate)
                .setIssueType("incident")
                .setLabels("api,updated,test")
                .setSeverity("medium")
                .setStartDate(startDate)
                .setStateEvent("close")
                .setTitle(newTitle);

        Issue updated = updateIssue(created.iid(), updateRequest);

        // Check that the issue is updated correctly
        assertThat(updated).as("Updated issue should not be null").isNotNull();
        verifyBasicResponseFields(updated, "closed");

        assertThat(updated.title()).isEqualTo(newTitle);
        assertThat(updated.description()).isEqualTo(description);
        assertThat(updated.confidential()).isTrue();
        assertThat(updated.discussionLocked()).isTrue();
        assertThat(updated.dueDate()).isEqualTo(LocalDate.parse(dueDate));
        assertThat(updated.startDate()).isEqualTo(LocalDate.parse(startDate));
        assertThat(updated.issueType()).isEqualTo("incident");
//        assertThat(updated.severity()).isEqualTo("MEDIUM"); // FINDING-5 Looks like you can't change issue type to an incident and set severity in the same update request. Severity remains UNKNOWN for non-incident issues.
        assertThat(updated.severity()).isEqualTo("UNKNOWN");
        assertThat(updated.labels())
                .containsExactlyInAnyOrder("api", "updated", "test");

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());

        assertThat(retrieved.title()).isEqualTo(newTitle);
        assertThat(retrieved.description()).isEqualTo(description);
        assertThat(retrieved.confidential()).isTrue();
        assertThat(retrieved.discussionLocked()).isTrue();
        assertThat(retrieved.dueDate()).isEqualTo(LocalDate.parse(dueDate));
        assertThat(retrieved.startDate()).isEqualTo(LocalDate.parse(startDate));
        assertThat(retrieved.issueType()).isEqualTo("incident");
//        assertThat(retrieved.severity()).isEqualTo("MEDIUM");  // FINDING-5 Looks like you can't change issue type to an incident and set severity in the same update request. Severity remains UNKNOWN for non-incident issues.
        assertThat(retrieved.severity()).isEqualTo("UNKNOWN");
        assertThat(retrieved.labels())
                .containsExactlyInAnyOrder("api", "updated", "test");

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update26: Send update request with all 3 label-related fields (labels, add_labels, remove_labels) together")
    void updateIssueAllLabelFieldsTest() {
        // Prepare and send create request with initial labels
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Send update request with all 3 label-related fields (labels, add_labels, remove_labels) together"))
                .setLabels("label1,label2,label3");
        Issue created = createIssue(createRequest);

        // Build and send update request to set the issue labels, add new labels, and remove some existing labels
        IssueUpdateRequest updateRequest = new IssueUpdateRequest()
                .setLabels("label4,label5") // This will replace all existing labels with label4 and label5
                .setAddLabels("label6,label7") // This will add label6 and label7 to the existing labels (which are now label4 and label5)
                .setRemoveLabels("label5,label8"); // This will remove label5 and label8 from the existing labels (which are now label4, label5, label6, label7

        // Check that the issue is updated correctly
        Issue updated = updateIssue(created.iid(), updateRequest);
        assertThat(updated.labels()).containsExactlyInAnyOrder("label4", "label6", "label7"); // label5 is removed, label8 was not present, label6 and label7 are added

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.labels()).containsExactlyInAnyOrder("label4", "label6", "label7");


        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update27: Update issue with maximum allowed title length")
    void updateIssueMaxTitleLengthTest() {
        // Prepare and send create request
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Update issue with maximum allowed title length")));

        // Build and send update request to set the issue title to maximum allowed length (255 characters)
        String newTitle = "A".repeat(255);
        Issue updated = updateIssue(
                created.iid(),
                new IssueUpdateRequest().setTitle(newTitle));

        // Check that the issue is updated correctly
        assertThat(updated.title()).isEqualTo(newTitle);

        //cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Update28: Update issue using URL encoded project path")
    void updateIssueUsingUrlEncodedProjectPathTest() {
        // Prepare and send create request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Update issue using URL encoded project path"));
        Issue created = createIssue(createRequest);

        // Build and send update request to update the issue title using URL encoded project path
        String newTitle = generateUniqueIssueTitle("Updated title using URL encoded project path");
        Issue updated = updateIssueRaw(PROJECT_PATH, created.iid(), new IssueUpdateRequest().setTitle(newTitle)).as(Issue.class);

        // Check that the issue is updated correctly
        assertThat(updated.title()).isEqualTo(newTitle);

        // Retrieve the issue and check that it is updated correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.title()).isEqualTo(newTitle);

        // Clean up
        deleteIssue(created.iid());
    }
}


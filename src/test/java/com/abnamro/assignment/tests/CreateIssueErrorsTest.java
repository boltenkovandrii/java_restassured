package com.abnamro.assignment.tests;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.model.IssueCreateRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static com.abnamro.assignment.base.IssuesAPI.*;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class CreateIssueErrorsTest extends BaseTest {

    /*
        Basic tests for issue creation logic with focus on error\negative scenarios
     */


    @Test
    @DisplayName("CreateErrors1: Attempt to create an issue with both milestone and milestoneId set")
    void createIssueWithBothMilestoneAndMilestoneIdTest() {
        // prepare and send request
        IssueCreateRequest createRequest = new IssueCreateRequest(
                generateUniqueIssueTitle("Test title"))
                .setMilestoneId(123456L)
                .setMilestone("Test milestone");
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // check response
        assertError(response, 400, "milestone_id, milestone are mutually exclusive");

        // check that issue was not created
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }


    @Test
    @DisplayName("CreateErrors2: Attempt to create an issue with existing iid (test requires Admin rights)")
    void createIssueWithExistingIidTest() {
        // Create an issue
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test title"));
        Issue created = createIssue(createRequest); // create an issue to get an existing iid

        // prepare and send request with the same iid
        IssueCreateRequest createRequest2 = new IssueCreateRequest(
                generateUniqueIssueTitle("Test title"))
                .setIid(created.iid());
        Response response = createIssueRaw(PROJECT_ID, createRequest2);

        // check response
        assertMessage(response, 409, "Duplicated issue");

        // check that issue was not created
        checkIssueIsAbsent(PROJECT_ID, createRequest2.getTitle());

        // Clean up
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("CreateErrors3: Attempt to create an issue with no title")
    void createIssueWithNoTitleTest() {
        // prepare and send request
        IssueCreateRequest createRequest = new IssueCreateRequest(null);
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // check response
        assertError(response, 400, "title is missing");

        //Cannot check that issue was not created by title, or description. Assume response code 400 is enough to ensure that issue was not created.
    }


    @Test
    @DisplayName("CreateErrors4: Attempt to create an issue with no title, but with some other fields set")
    void createIssueWithNoTitleButOtherFieldsSetTest() {
        // prepare and send request
        String uniqueDescription = "Test description " + System.currentTimeMillis();
        IssueCreateRequest createRequest = new IssueCreateRequest(null)
                .setDescription(uniqueDescription)
                .setAssigneeId(USER_ID);
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // check response
        assertError(response, 400, "title is missing");

        // check that issue was not created
        checkIssueIsAbsent(PROJECT_ID, uniqueDescription);
    }


    @Test
    @DisplayName("CreateErrors5: Attempt to create an issue with no title, but with iid (requires Admin rights)")
    void createIssueWithNoTitleButIidSetTest() {
        // prepare and send request
        String uniqueDescription = "Test description " + System.currentTimeMillis();
        IssueCreateRequest createRequest = new IssueCreateRequest(null)
                .setDescription(uniqueDescription)
                .setIid(getRandomLong());
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // check response
        assertError(response, 400, "title is missing");

        // check that issue was not created
        checkIssueIsAbsent(PROJECT_ID, uniqueDescription);
    }


    @Test
    @DisplayName("CreateErrors6: Attempt to create issue with incorrect issue type")
    void createIssueWithIncorrectIssueTypeTest() {
        // Prepare and send request with an incorrect issue type.
        IssueCreateRequest createRequest = new IssueCreateRequest(
                generateUniqueIssueTitle("Incorrect issue type"))
                .setIssueType("invalid_type");
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // Check that the response indicates a bad request due to the incorrect issue type.
        assertError(response, 400, "issue_type does not have a valid value");

        // Check that the issue was not created.
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }


    @Test
    @DisplayName("CreateErrors7: Attempt to create issue with start date after the due date")
    void createIssueWithStartDateAfterDueDateTest() {
        // Prepare and send request with a start date after the due date.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Start date after due date test"))
                .setStartDate("2026-10-01")
                .setDueDate("2026-09-30");
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // Check that the response indicates a bad request due to the start date being after the due date.
        assertMessage(response, 400, "[due_date:[must be greater than or equal to start date]]");

        // Check that the issue was not created.
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }


    @Test
    @DisplayName("CreateErrors8: Attempt to create issue with too long title")
    void createIssueWithTooLongTitleTest() {
        // Prepare and send request with a title that exceeds the maximum allowed length.
        String longTitle = "A".repeat(256); // the max length is 255 characters.
        IssueCreateRequest createRequest = new IssueCreateRequest(longTitle);
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // Check that the response indicates a bad request due to the title being too long.
        assertMessage(response, 400, "[title:[is too long (maximum is 255 characters)]]");

        // Check that the issue was not created.
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());

    }


    // GitLab appears to stop validation after the first error (at least in this case) rather than  returning all validation errors in a single response.
    // let's treat is as expected behavior for now, but we can revisit if needed.
    @Test
    @DisplayName("CreateErrors9: Attempt to create issue with several validation errors")
    void createIssueWithSeveralErrorsTest() {
        // Send a request containing multiple validation errors:
        // title is too long and start date is after due date.
        String longTitle = "A".repeat(256);

        IssueCreateRequest createRequest = new IssueCreateRequest(longTitle)
                .setStartDate("2026-10-01")
                .setDueDate("2026-09-30");

        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // GitLab reports only the title validation error.
        assertMessage(response, 400, "[title:[is too long (maximum is 255 characters)]]");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }


    @Test
    @DisplayName("CreateErrors10: Attempt to create issue with empty title")
    void createTwoIssuesWithEmptyTitleTest() {
        // Prepare and send request to create the first issue with an empty title.
        String uniqueDescription = "Test description " + System.currentTimeMillis();
        IssueCreateRequest createRequest = new IssueCreateRequest("").setDescription(uniqueDescription);
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // Check that the response indicates a bad request due to the empty title.
        assertMessage(response, 400, "[title:[can't be blank]]");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, uniqueDescription);
    }


    @ParameterizedTest
    @DisplayName("CreateErrors11: Attempt to create issue for an invalid numeric project ID")
    @ValueSource(longs = {0, -1, -999})
    void createIssueWithInvalidNumericProjectIdTest(long invalidProjectId) {
        // Prepare and send request to create an issue with an invalid project ID.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Invalid project ID test"));
        Response response = createIssueRaw(invalidProjectId, createRequest);

        // Check that the response indicates a bad request due to the invalid project ID.
        assertMessage(response, 404, "404 Project Not Found");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }

    @ParameterizedTest
    @DisplayName("CreateErrors12: Attempt to create issue for non-existent or empty string project ID")
    @ValueSource(strings = {"null", "does-not-exist%2Fproject", "non_existing_project", "project%20name%20with%20spaces"})
    void createIssueWithNonExistentStringProjectIdTest(String nonExistentProjectId) {
        // Prepare and send request to create an issue with a non-existent or empty string project ID.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Non-existent project ID test"));
        Response response = createIssueRaw(nonExistentProjectId, createRequest);

        // Check that the response indicates a bad request due to the non-existent or empty string project ID.
        assertMessage(response, 404, "404 Project Not Found");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }


    @Test
    @DisplayName("CreateErrors13: Attempt to create issue for an empty project ID")
    void createIssueWithEmptyProjectIdTest() {
        // Prepare and send request to create an issue with an invalid project ID.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Invalid project ID test"));
        Response response = createIssueRaw("", createRequest);

        // Check that the response indicates a bad request due to the invalid project ID.
        assertError(response, 404, "404 Not Found");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }


    @ParameterizedTest
    @ValueSource(strings = {"not-a-number", "2026-09-05T12:00:00Z"})
    @DisplayName("CreateErrors14: Attempt to create issue with non-numeric assignee ID")
    void createIssueWithNonNumericAssigneeIdTest(String nonNumericAssigneeId) {
        // Prepare and send request to create an issue with a non-numeric assignee ID.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Invalid assignee ID type test"));
        requestBody.put("assignee_id", nonNumericAssigneeId);

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));
        
        // Check that the response indicates a bad request due to the non-numeric assignee ID.
        assertError(response, 400, "assignee_id is invalid");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, requestBody.get("title").toString());
    }


    @ParameterizedTest
    @ValueSource(strings = {"not-a-number", "2026-09-05T12:00:00Z"})
    @DisplayName("CreateErrors15: Attempt to create issue with non-boolean confidential field")
    void createIssueWithNonBooleanConfidentialFieldTest(String nonBooleanConfidential) {
        // Prepare and send request to create an issue with a non-boolean confidential field.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Invalid confidential field type test"));
        requestBody.put("confidential", nonBooleanConfidential);

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that the response indicates a bad request due to the non-boolean confidential field.
        assertError(response, 400, "confidential is invalid");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, requestBody.get("title").toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("CreateErrors16: Attempt to create issue with empty confidential field")
    void createIssueWithEmptyConfidentialFieldTest(String nonBooleanConfidential) {
        // Prepare and send request to create an issue with a empty confidential field.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Invalid confidential field type test"));
        requestBody.put("confidential", nonBooleanConfidential);

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that the response indicates a bad request due to the empty confidential field.
        assertError(response, 400, "confidential is empty");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, requestBody.get("title").toString());
    }


    @ParameterizedTest
    @DisplayName("CreateErrors17: Attempt to create issue with created_at not representing a date-time (requires Admin rights)")
    @ValueSource(strings = {"not-a-date", "2026-39-05T12:00:00Z"})
    void createIssueWithInvalidCreatedAtFieldTest(String invalidCreatedAt) {
        // Prepare and send request to create an issue with a created_at field that does not represent a date-time.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Invalid created_at field test"));
        requestBody.put("created_at", invalidCreatedAt);

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that the response indicates a bad request due to the invalid created_at field.
        assertError(response, 400, "created_at is invalid");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, requestBody.get("title").toString());
    }


    @Test
    @DisplayName("CreateErrors18: Attempt to create an issue with too long description")
    void createIssueWithTooLongDescriptionTest() {
           // prepare and send request
        String longDescription = "A".repeat(1_048_577); // 32 x 33000 = 1056000 characters, max value is 1,048,576
            IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                    .setDescription(longDescription);
            Response response = createIssueRaw(PROJECT_ID, createRequest);

            // check response
            assertMessage(response, 400, "[description:[is too long (1 MiB). The maximum size is 1 MiB.]]");

            // check that issue was not created
            checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }

    @Test
    @Tag("WIP") // FINDING-2 It is possible to create issue with due date in the past, but probably it should not be allowed. This test is marked as WIP until the issue is fixed.
    @DisplayName("CreateErrors19: Attempt to create issue with due date in the past")
    void createIssueWithDueDateInThePastTest() {
        // prepare and send request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                .setDueDate("2020-01-01");
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // check response
        assertMessage(response, 400, "[due_date:[must be greater than or equal to today]]");

        // check that issue was not created
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-date", "2026-39-05"})
    @DisplayName("CreateErrors20: Attempt to create issue with invalid due date")
    void createIssueWithInvalidDueDateTest(String invalidDueDate) {
        // Prepare and send request to create an issue with an invalid due date.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Invalid due date test"));
        requestBody.put("due_date", invalidDueDate);

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // check that issue is created correctly with due_date set to null
        assertThat(response.statusCode()).as("Response code should be 201, but was "+ response.statusCode()).isEqualTo(201);
        Issue created = response.as(Issue.class);
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.dueDate()).as("Created issue due_date should be null").isNull();

        // Clean up
        deleteIssue(created.iid());
    }



    @Tag("WIP") // FINDING-4 Handling of invalid  (negative or too big) iid's in create request is incorrect.  You receive: "message": "500 Internal Server Error".
    // But even worse - issue is created and it breaks the system - you can't view issues in UI anymore and GetIssues request is also returning 500 error.
    // Good news is that the issue can be deleted via API and then everything works again (you will receive another 500 error, but issue will be deleted). This test is marked as WIP until the issue is fixed.
    @ParameterizedTest
    @DisplayName("CreateErrors21: Attempt to create issue with invalid integer iid's (Requires Admin rights)")
    @ValueSource(longs = {-1, -999, Long.MAX_VALUE})
    void createIssueWithInvalidIidTest(long invalidIid) {

//        deleteIssue(invalidIid); // If you have run this test accidentally, and you project is broken, uncomment this line and run it again to delete the issues with invalid iid and restore the project to working state.

        // Prepare and send request to create an issue with an invalid iid.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Invalid iid test"))
                .setIid(invalidIid);
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // Check that the response indicates a bad request due to the invalid iid.
        assertError(response, 400, "iid is invalid");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }


    @Test
    @DisplayName("CreateErrors22: Attempt to create an issue with non-string issue_type")
    void createIssueWithNonStringIssueTypeTest() {
        // Prepare and send request to create an issue with a non-string issue_type.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Non-string issue_type test"));
        requestBody.put("issue_type", 12345); // Non-string value

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that the response indicates a bad request due to the non-string issue_type.
        assertError(response, 400, "issue_type does not have a valid value");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, requestBody.get("title").toString());

    }

    @Test
    @DisplayName("CreateErrors23: Attempt to create an issue with non-string labels")
    void createIssueWithNonStringLabelsTest() {
        // Prepare and send request to create an issue with non-string labels.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Non-string labels test"));
        requestBody.put("labels", 12345); // Non-string value

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // check that issue is created correctly with labels set to empty list
        assertThat(response.statusCode()).as("Response code should be 201, but was " + response.statusCode()).isEqualTo(201);
        Issue created = response.as(Issue.class);
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.labels()).as("Created issue labels should be empty").isEmpty();

        // Clean up
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("CreateErrors24: Attempt to create an issue with non-numeric milestone_id")
    void createIssueWithNonNumericMilestoneIdTest() {
        // Prepare and send request to create an issue with a non-numeric milestone_id.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Non-numeric milestone_id test"));
        requestBody.put("milestone_id", "not-a-number"); // Non-numeric value

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that the response indicates a bad request due to the non-numeric milestone_id.
        assertError(response, 400, "milestone_id is invalid");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, requestBody.get("title").toString());
    }

    @Test
    @DisplayName("CreateErrors25: Attempt to create an issue with negative milestone_id")
    void createIssueWithNegativeMilestoneIdTest() {
        // Prepare and send request to create an issue with a negative milestone_id.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Negative milestone_id test"));
        requestBody.put("milestone_id", -12345); // Negative value

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // check that issue is created correctly with empty milestone
        assertThat(response.statusCode()).as("Response code should be 201, but was "+ response.statusCode()).isEqualTo(201);
        Issue created = response.as(Issue.class);
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.milestone()).as("Created issue milestone should be null").isNull();

        // Clean up
        deleteIssue(created.iid());

    }


    @Test
    @DisplayName("CreateErrors26: Attempt to create an issue with incorrect string severity")
    void createIssueWithIncorrectStringSeverityTest() {
        // Prepare and send request to create an issue with an incorrect string severity.
        // prepare and send request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                .setSeverity("invalid_severity");
        Response response = createIssueRaw(PROJECT_ID, createRequest);

        // check response
        assertError(response, 400, "severity does not have a valid value");

        // check that issue was not created
        checkIssueIsAbsent(PROJECT_ID, createRequest.getTitle());
    }

    @Test
    @DisplayName("CreateErrors27: Attempt to create an issue with non-string severity")
    void createIssueWithNonStringSeverityTest() {
        // Prepare and send request to create an issue with a non-string severity.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Non-string severity test"));
        requestBody.put("severity", 12345); // Non-string value

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that the response indicates a bad request due to the non-string severity.
        assertError(response, 400, "severity does not have a valid value");

        // The issue must not be created.
        checkIssueIsAbsent(PROJECT_ID, requestBody.get("title").toString());

    }

    @Test
    @DisplayName("CreateErrors28: Create non-incident issue with severity field set (should be ignored)")
    void createNonIncidentIssueWithSeverityTest() {
        // Prepare and send request with severity field set for a non-incident issue.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Non-incident issue with severity"))
                .setIssueType("issue")
                .setSeverity("high");
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the severity field is ignored (should be "UNKNOWN").
        assertThat(created).isNotNull();
        assertThat(created.issueType()).isEqualTo("issue");
        assertThat(created.severity()).isEqualTo("UNKNOWN"); // Severity should be ignored for non-incident issues and default to "UNKNOWN".

        // Clean up
        deleteIssue(created.iid());
    }

    @ParameterizedTest
    @DisplayName("CreateErrors29: Attempt to create issue with start_date not representing a date-time")
    @ValueSource(strings = {"not-a-date", "2026-39-05T12:00:00Z"})
    void createIssueWithInvalidStartDateFieldTest(String invalidStartDate) {
        // Prepare and send request to create an issue with a start_date field that does not represent a date-time.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Invalid start_date field test"));
        requestBody.put("start_date", invalidStartDate);

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that issue is created correctly with start_date set to null
        assertThat(response.statusCode()).as("Response code should be 201, but was "+ response.statusCode()).isEqualTo(201);
        Issue created = response.as(Issue.class);
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.startDate()).as("Created issue start_date should be null").isNull();

        // Clean up
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("CreateErrors30: Attempt to create issue with non-string start_date value")
    void createIssueWithNonStringStartDateFieldTest() {
        // Prepare and send request to create an issue with a non-string start_date field.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Non-string start_date test"));
        requestBody.put("start_date", 12345); // Non-string value

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that issue is created correctly with start_date set to null
        assertThat(response.statusCode()).as("Response code should be 201, but was "+ response.statusCode()).isEqualTo(201);
        Issue created = response.as(Issue.class);
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.startDate()).as("Created issue start_date should be null").isNull();

        // Clean up
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("CreateErrors31: Attempt to create issue with non-string title value")
    void createIssueWithNonStringTitleFieldTest() {
        // Prepare and send request to create an issue with a non-string title field.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", 12345); // Non-string value

        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));

        // Check that issue is created correctly with title set to string representation of the number
        assertThat(response.statusCode()).as("Response code should be 201, but was "+ response.statusCode()).isEqualTo(201);
        Issue created = response.as(Issue.class);
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.title()).as("Created issue title should be string representation of the number").isEqualTo("12345");

        // Clean up
        deleteIssue(created.iid());

    }



}

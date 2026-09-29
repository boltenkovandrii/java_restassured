package com.abnamro.assignment.tests;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.model.IssueCreateRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class CreateIssueTest extends BaseTest {

    /*
        Basic tests for issue creation logic with focus on happy path.
     */


    @Test
    @DisplayName("Create1: Create issue with only mandatory fields")
    void createIssueMandatoryFieldsTest() {
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test title"));
        Issue created = createIssue(createRequest);

        // check response
        assertThat(created).as("Created issue should not be null").isNotNull();
        verifyBasicResponseFields(created, "opened");
        assertThat(created.title()).isEqualTo(createRequest.getTitle());

        // Retrieve the issue and check that it is created correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).as("Retrieved issue should not be null").isNotNull();
        verifyBasicResponseFields(retrieved, "opened");
        assertThat(retrieved.title()).isEqualTo(createRequest.getTitle());

        // Clean up
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Create2: Create issue with all fields")
    void createIssueAllFieldsTest() {

        // prepare and send request
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                .setAssigneeId(USER_ID)
                .setConfidential(false)
                .setCreatedAt("2026-09-05T12:00:00Z")
                .setDescription("Created by createIssueAllFieldsTest")
                .setDiscussionToResolve("Please resolve the discussion before closing this issue.")
                .setDueDate("2026-09-30")
                .setIid(getRandomLong())
                .setIssueType("incident")
                .setLabels("api-test,full-fields")
                .setMergeRequestToResolveDiscussionsOf(9999999999L)
//                .setMilestoneId(123456L) //empty for this request since mutually exclusive with milestone.
                .setMilestone("Test milestone")
                .setSeverity("medium")
                .setStartDate("2026-09-05");

        Issue created = createIssue(createRequest);

        // check response
        verifyBasicResponseFields(created, "opened");
        assertIssueMatchesRequest(created, createRequest);
        assertThat(created.iid()).isEqualTo(createRequest.getIid());
        assertThat(created.milestone()).isNull(); //just checking that field exists. covers both milestone and milestoneId. No verification for milestone\milestoneId - require project configuration
        assertThat(created.severity()).isEqualTo(createRequest.getSeverity().toUpperCase()); // stored in UPPERCASE in GitLab
        // no verification for discussion_to_resolve - require project configuration alongside with merge_request_to_resolve_discussions_of
        // no verification for merge_request_to_resolve_discussions_of - require project configuration


        // Retrieve the issue and check that it is created correctly
        Issue retrieved = getIssue(created.iid());
        verifyBasicResponseFields(retrieved, "opened");
        assertIssueMatchesRequest(retrieved, createRequest);
        assertThat(retrieved.iid()).isEqualTo(createRequest.getIid());
        assertThat(retrieved.milestone()).isNull(); //just checking that field exists. covers both milestone and milestoneId. No verification for milestone\milestoneId - require project configuration
        assertThat(retrieved.severity()).isEqualTo(createRequest.getSeverity().toUpperCase()); // stored in UPPERCASE in GitLab
        // no verification for discussion_to_resolve - require project configuration alongside with merge_request_to_resolve_discussions_of
        // no verification for merge_request_to_resolve_discussions_of - require project configuration

        // Clean up
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Create3: Create issue with non-existing milestone ID")
    void createIssueWithNonExistingMilestoneIdTest() {
        // Prepare and send request.
        // A non-existing milestone ID avoids dependency on project milestone configuration.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test issue"))
                .setMilestoneId(9999999999L); // Non-existing milestone ID

        Issue created = createIssue(createRequest);

        // Verify that issue creation succeeds and the unresolved milestone
        // does not prevent the issue from being created.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.milestone()).isNull();

        //cleanup
        deleteIssue(created.iid());

    }

    @Test
    @DisplayName("Create4: Create confidential issue")
    void createConfidentialIssueTest() {
        // Prepare and send request.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Confidential issue"))
                .setConfidential(true);
        Issue created = createIssue(createRequest);

        // Verify that issue creation succeeds and the issue is marked as confidential.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.confidential()).isTrue();

        //cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Create5: Create issue with date created in the future (requires Admin rights)")
    void createIssueWithFutureCreatedAtTest() {
        // Prepare and send request.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Future created issue"))
                .setCreatedAt("2050-01-01T12:00:00Z");
        Issue created = createIssue(createRequest);

        // Verify that issue creation succeeds and the createdAt field is set to the future date.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.createdAt()).isEqualTo(Instant.parse(createRequest.getCreatedAt()));

        //cleanup
        deleteIssue(created.iid());

    }

    @Test
    @DisplayName("Create6: Create issue with non-existing assignee ID")
    void createIssueWithNonExistingAssigneeIdTest() {
        // Prepare and send request.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test issue"))
                .setAssigneeId(9999999999L); // Non-existing assignee ID

        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the assignee is not set (null).
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.assignee()).isNull();

        //cleanup
        deleteIssue(created.iid());

    }


    @Test
    @DisplayName("Create7: Create issue with very long (but still valid) description") // Limited to 1,048,576 characters on GitLab
    void createIssueWithLongDescriptionTest() {
        // Prepare and send request.
        String longDescription = "A".repeat(1_048_576); // 1,048,576 characters long
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Test issue"))
                .setDescription(longDescription);
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the description is set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.description()).isEqualTo(longDescription);

        //cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("Create8: Create issue with special characters in the title and description")
    void createIssueWithSpecialCharactersTest() {
        // Prepare and send request.
        String specialTitle = generateUniqueIssueTitle("Special characters: !@#$%^&*()_+-=[]{}|;':\",.<>/?`~");
        String specialDescription = "Description with special characters: !@#$%^&*()_+-=[]{}|;':\",.<>/?`~";
        IssueCreateRequest createRequest = new IssueCreateRequest(specialTitle)
                .setDescription(specialDescription);
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the title and description are set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.title()).isEqualTo(specialTitle);
        assertThat(created.description()).isEqualTo(specialDescription);

        //cleanup
        deleteIssue(created.iid());
    }


    //FINDING-1 - looks like GitLab supports only 3 issue types: issue, incident, task. Test case for test_case is commented out.
    @ParameterizedTest
//    @ValueSource(strings = {"issue", "incident", "test_case", "task"})
    @ValueSource(strings = {"issue", "incident", "task"})
    @DisplayName("Create9: Create issue with supported issue type")
    void createIssueWithSupportedIssueTypeTest(String issueType) {
        // Prepare and send request.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Issue type test"))
                .setIssueType(issueType);
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the issue type is set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.issueType()).isEqualTo(issueType);

        // Clean up
        deleteIssue(created.iid());

    }

    @Test
    @DisplayName("Create10: Create issue without issue type")
    void createIssueWithoutIssueTypeTest() {
        // Prepare and send request without specifying the issue type.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Default issue type"));
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the default issue type is set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.issueType()).isEqualTo("issue");

        deleteIssue(created.iid());

    }

    @Test
    @DisplayName("Create11: Create issue with long list of labels")
    void createIssueWithLongListOfLabelsTest() {
        // Prepare and send request with a long list of labels.
        String longLabelsList = "label1,label2,label3,label4,label5,label6,label7,label8,label9,label10";
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Long labels test"))
                .setLabels(longLabelsList);
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the labels are set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.labels()).containsExactlyInAnyOrder(longLabelsList.split(","));

        // Clean up
        deleteIssue(created.iid());

    }


    @Test
    @DisplayName("Create12: Create issue special symbols in labels")
    void createIssueWithSpecialSymbolsInLabelsTest() {
        // Prepare and send request with special symbols in labels.
        String specialLabels = "label!@#$%^&*(),label[]{}|;':\",.<>/?`~";
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Special symbols in labels test"))
                .setLabels(specialLabels);
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the labels are set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.labels()).containsExactlyInAnyOrder(specialLabels.split(","));

        // Clean up
        deleteIssue(created.iid());

    }


    @Test
    @DisplayName("Create13: Create issue with non-ascii characters in text fields")
    void createIssueWithNonAsciiCharactersTest() {
        // Prepare and send request with non-ASCII characters in title and description.
        String nonAsciiTitle = generateUniqueIssueTitle("Non-ASCII title: Привет, 你好, مرحبا, नमस्ते");
        String nonAsciiDescription = "Non-ASCII description: Привет, 你好, مرحبا, नमस्ते";
        String nonAsciiLabels = "label-Привет,label-你好,label-مرحبا,label-नमस्ते";
        IssueCreateRequest createRequest = new IssueCreateRequest(nonAsciiTitle)
                .setDescription(nonAsciiDescription)
                .setLabels(nonAsciiLabels);

        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the title and description are set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.title()).isEqualTo(nonAsciiTitle);
        assertThat(created.description()).isEqualTo(nonAsciiDescription);
        assertThat(created.labels()).containsExactlyInAnyOrder(nonAsciiLabels.split(","));

        // Clean up
        deleteIssue(created.iid());

    }


    @Test
    @DisplayName("Create14: Create issue with empty severity field")
    void createIssueWithEmptySeverityTest() {
        // Prepare and send request with an empty severity field.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Empty severity test"))
                .setIssueType("incident"); // Severity is only applicable for incidents, so we set the issue type to "incident"
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the severity field is empty.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.severity()).isEqualTo("UNKNOWN"); // default value for severity when not set is "UNKNOWN" in GitLab

        // Clean up
        deleteIssue(created.iid());

    }


    @ParameterizedTest
    @ValueSource(strings = {"unknown", "low", "medium", "high", "critical"})
    @DisplayName("Create15: Create issue with supported severity values for incidents")
    void createIssueWithSupportedSeverityValuesTest(String severity) {
        // Prepare and send request with supported severity values.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Supported severity test"))
                .setIssueType("incident")
                .setSeverity(severity);
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the severity field is set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.severity()).isEqualTo(severity.toUpperCase()); // GitLab stores severity in uppercase

        // Clean up
        deleteIssue(created.iid());

    }


    @Test
    @DisplayName("Create16: Create issue with start date in the past")
    void createIssueWithPastStartDateTest() {
        // Prepare and send request with a start date in the past.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Paste start date test"))
                .setStartDate("2000-01-01");
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the start date is set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.startDate()).isEqualTo(LocalDate.parse(createRequest.getStartDate()));

        // Clean up
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Create17: Create issue with very long title (limited to 255 characters on GitLab)")
    void createIssueWithVeryLongTitleTest() {
        // Prepare and send request with a very long title.
        String veryLongTitle = "A".repeat(255); // 255 characters long
        IssueCreateRequest createRequest = new IssueCreateRequest(veryLongTitle);
        Issue created = createIssue(createRequest);

        // Check that issue creation succeeds and the title is set correctly.
        assertThat(created).as("Created issue should not be null").isNotNull();
        assertThat(created.title()).isEqualTo(veryLongTitle);

        // Clean up
        deleteIssue(created.iid());

    }

    @Test
    @DisplayName("Create18: Create two issues with the same title")
    void createTwoIssuesWithSameTitleTest() {
        // Prepare and send request to create the first issue.
        String duplicateTitle = generateUniqueIssueTitle("Duplicate title test");
        IssueCreateRequest createRequest1 = new IssueCreateRequest(duplicateTitle);
        Issue created1 = createIssue(createRequest1);

        // Prepare and send request to create the second issue with the same title.
        IssueCreateRequest createRequest2 = new IssueCreateRequest(duplicateTitle);
        Issue created2 = createIssue(createRequest2);

        // Check that both issues are created successfully and have the same title.
        assertThat(created1).as("First created issue should not be null").isNotNull();
        assertThat(created2).as("Second created issue should not be null").isNotNull();
        assertThat(created1.title()).isEqualTo(duplicateTitle);
        assertThat(created2.title()).isEqualTo(duplicateTitle);

        // Clean up
        deleteIssue(created1.iid());
        deleteIssue(created2.iid());
    }


    @Test
    @Tag("WIP") //FINDING-3 according to GitLab API documentation, the iid field could be a string, but in practice, it seems to only accept numeric values.
    // This test is marked as WIP and represents existing logic - not the one descried in documentation.
    @DisplayName("Create19: Create issue with string iid (requires Admin rights)")
    void createIssueWithStringIidTest() {
        // Prepare and send request with a string iid.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", generateUniqueIssueTitle("Issue with string iid test"));
        requestBody.put("iid", "StringIid");
        Response response = getRestBase().postInternal(requestBody, projectIssuesUrl(PROJECT_ID));


        // Check that issue creation fails with a 400 Bad Request status code.
        assertThat(response.statusCode()).isEqualTo(400);

    }


    @Test
    @DisplayName("Create20: Create issue with start date equal to due date")
    void createIssueWithEqualStartAndDueDateTest() {
        // Prepare and send request with start date equal to due date.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Equal start and due date"))
                        .setStartDate("2026-09-30")
                        .setDueDate("2026-09-30");
        Issue created = createIssue(createRequest);


        // Check that issue creation succeeds and the start date and due date are set correctly.
        assertThat(created).isNotNull();
        assertThat(created.startDate()).isEqualTo("2026-09-30");
        assertThat(created.dueDate()).isEqualTo("2026-09-30");

        // Clean up
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("Create21: Create issue using URL encoded project path")
    void createIssueUsingUrlEncodedProjectPathTest() {
        // Prepare and send request using URL encoded project path.
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("URL encoded project path test"));
        Issue created = createIssueRaw(PROJECT_PATH, createRequest).as(Issue.class);

        // Check that issue creation succeeds and the title is set correctly.
        assertThat(created).isNotNull();
        assertThat(created.title()).isEqualTo(createRequest.getTitle());

        //retrieve the issue and check that it is created correctly
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.title()).isEqualTo(createRequest.getTitle());

        // Clean up
        deleteIssue(created.iid());
    }
}


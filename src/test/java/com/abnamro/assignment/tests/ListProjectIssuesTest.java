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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class ListProjectIssuesTest extends BaseTest {

    /*
        Tests for getting list of project issues with focus on happy path
    */


    @Test
    @DisplayName("ListIssues1: List issues with a valid filter, but no matching issues")
    void listIssuesWithValidFilterButNoMatchingIssuesTest() {
        // Use a unique label that is unlikely to exist in the project
        String uniqueLabel = "nonexistent-label-" + UUID.randomUUID();

        // List issues with the unique label filter
        List<Issue> issues = getIssues(Map.of("labels", uniqueLabel));

        // Verify that the returned list is empty
        assertThat(issues).as("Check that result is empty for a valid filter with no matching issues").isEmpty();
    }


    @Test
    @DisplayName("ListIssues2: List issues assigned to a specific user")
    void listIssuesByAssigneeIdTest() {
        // Create an assigned and an unassigned issue
        // we don't have another user in this setup, but it would be nice to add an issue assigned to another user too
        Issue assignedIssue = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Assigned issue"))
                .setAssigneeId(USER_ID));
        Issue unassignedIssue = createIssue(
                new IssueCreateRequest(generateUniqueIssueTitle("Unassigned issue")));

        // List issues assigned to the specified user
        List<Issue> issues = getIssues(Map.of("assignee_id", USER_ID));

        //verify that the returned issues are assigned to the specified user and that the unassigned issue is not returned
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues)
                .as("Check that all returned issues are assigned to the specified user")
                .allSatisfy(issue -> assertThat(issue.assignee().id()).isEqualTo(USER_ID));

        // Cleanup
        deleteIssue(assignedIssue.iid());
        deleteIssue(unassignedIssue.iid());
    }

    @Test
    @DisplayName("ListIssues3: List issues assigned to a specific user by username")
    void listIssuesByAssigneeUsernameTest() {
        // Create an assigned and an unassigned issue
        // we don't have another user in this setup, but it would be nice to add an issue assigned to another user too
        Issue assignedIssue = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Assigned issue"))
                .setAssigneeId(USER_ID));
        Issue unassignedIssue = createIssue(
                new IssueCreateRequest(generateUniqueIssueTitle("Unassigned issue")));

        // List issues assigned to the target username
        List<Issue> issues = getIssues(Map.of("assignee_username", USER_NAME));

        // Verify that the returned issues are assigned to the specified username
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue -> assertThat(issue.assignee().username()).isEqualTo(USER_NAME));

        // Cleanup
        deleteIssue(assignedIssue.iid());
        deleteIssue(unassignedIssue.iid());
    }


    @Test
    @DisplayName("ListIssues4: List issues created by a specific user")
    void listIssuesByAuthorIdTest() {
        // Create an issue using the current authenticated user
        // We don't have another user in this setup, and author is always filled. So test id not very m=meaningful. It would be nice to add an issue assigned to another user too
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Author ID test")));

        // List issues created by the specified user
        List<Issue> issues = getIssues(Map.of(
                "author_id", USER_ID,
                "scope", "all" //scope=all or scope=assigned_to_me required by GitLab API to for this filter
        ));

        // Verify that the returned issues are created by the specified user
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue ->  assertThat(issue.author().id()).isEqualTo(USER_ID));

        // Cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("ListIssues5: List issues created by a specific user by username")
    void listIssuesByAuthorUsernameTest() {
        // Create an issue using the current authenticated user
        // We don't have another user in this setup, and author is always filled. So test id not very m=meaningful. It would be nice to add an issue assigned to another user too
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Author username test")));

        // List issues created by the specified username
        List<Issue> issues = getIssues(Map.of(
                "author_username", USER_NAME,
                "scope", "all"
        ));

        // Verify that the returned issues are created by the specified username
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue ->  assertThat(issue.author().username()).isEqualTo(USER_NAME));

        // Cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("ListIssues6: Filter by confidential parameter")
    void listIssuesByConfidentialParameterTest() {
        // Create a confidential and a non-confidential issue
        Issue confidentialIssue = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Confidential issue"))
                .setConfidential(true));
        Issue nonConfidentialIssue = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Non-confidential issue"))
                .setConfidential(false));

        // List issues with confidential=true
        List<Issue> confidentialIssues = getIssues(Map.of("confidential", true));
        assertThat(confidentialIssues).as("Check that result is not empty").isNotEmpty();
        assertThat(confidentialIssues).allSatisfy(issue -> assertThat(issue.confidential()).isTrue());

        // List issues with confidential=false
        List<Issue> nonConfidentialIssues = getIssues(Map.of("confidential", false));
        assertThat(nonConfidentialIssues).as("Check that result is not empty").isNotEmpty();
        assertThat(nonConfidentialIssues).allSatisfy(issue -> assertThat(issue.confidential()).isFalse());

        // Clean up
        deleteIssue(confidentialIssue.iid());
        deleteIssue(nonConfidentialIssue.iid());
    }

    @Test
    @DisplayName("ListIssues7: List issues created after specified date")
    void listIssuesCreatedAfterTest() {
        // Create an issue and record the time before creation
        Instant beforeCreation = Instant.now().minusSeconds(2);
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Created after test")));

        // List issues created after the lower boundary
        List<Issue> issues = getIssues(Map.of("created_after", beforeCreation.toString()));
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue -> assertThat(issue.createdAt()).isAfterOrEqualTo(beforeCreation));

        // Cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("ListIssues8: List issues created before specified date")
    void listIssuesCreatedBeforeTest() {
        // Create an issue and record the time after creation
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Created before test")));
        Instant afterCreation = Instant.now().plusSeconds(2);

        // List issues created before the upper boundary
        List<Issue> issues = getIssues(Map.of("created_before", afterCreation.toString()));
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue -> assertThat(issue.createdAt()).isBeforeOrEqualTo(afterCreation));

        // Cleanup
        deleteIssue(created.iid());
    }


    @ParameterizedTest
    @DisplayName("ListIssues9: List issues with a due date - due_date filter options")
    // test just verifies that the request is accepted and returns no error and a list of issues (could be empty - no data generated specifically to match the request).
    @ValueSource(strings = {"any", "today", "tomorrow", "overdue", "week", "month", "next_month_and_previous_two_weeks"})
    void listIssuesByDueDateFilterTest(String dueDateFilter) {
        // Create an issue with a due date in the future to ensure there is at least one issue in the project
        LocalDate dueDate = LocalDate.now().plusDays(5);
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Due date filter test"))
                .setDueDate(dueDate.format(DATE_FORMATTER)));

        // List issues with the specified due date filter
        List<Issue> issues = getIssues(Map.of("due_date", dueDateFilter));
        assertThat(issues).as("Check that result is not null").isNotNull();

        // Cleanup
        deleteIssue(created.iid());
    }


    //for the sake of simplicity only testing due_date=month filter here. But checking other filters would be good test as well - good tests to add.
    @Test
    @DisplayName("ListIssue10: List issues with a due date")
    void listIssuesByDueDateTest() {
        LocalDate dueDate = LocalDate.now().plusDays(5);

        // Create an issue with a due date
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Due date test"))
                .setDueDate(dueDate.format(DATE_FORMATTER)));

        // List issues due this month as appropriate
        List<Issue> issues = getIssues(Map.of("due_date", "month"));
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        // Verify that the returned issues have a due date set and it is within the expected range (this month)
        assertThat(issues).allSatisfy(issue -> {
            assertThat(issue.dueDate()).isNotNull();
            assertThat(issue.dueDate()).isAfterOrEqualTo(LocalDate.now());
            assertThat(issue.dueDate()).isBeforeOrEqualTo(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        });


        // Cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("ListIssues11: List issues by issue IIDs")
    void listIssuesByIidsTest() {
        // Create two issues
        Issue created1 = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("IID filter test 1")));
        Issue created2 = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("IID filter test 2")));

        // Request only the created issue IIDs
        Map<String, Object> queryParams = Map.of("iids%5B%5D", List.of(created1.iid(), created2.iid()));

        // List issues by IIDs
        List<Issue> issues = getIssues(queryParams);
        assertThat(issues)
                .extracting(Issue::iid)
                .containsExactlyInAnyOrder(created1.iid(), created2.iid());

        // Cleanup
        deleteIssue(created1.iid());
        deleteIssue(created2.iid());
    }


    //FINDING-1 - looks like current GitLab configuration supports only 3 issue types: issue, incident, task. Test case for test_case is commented out.
    @ParameterizedTest
    @ValueSource(strings = {"issue", "incident", "task"})
    @DisplayName("ListIssues12: List issues by issue type")
    void listIssuesByIssueTypeTest(String issueType) {
        //create one issue for each type and then filter by that type to verify that the filtering works correctly.
        Issue created1 = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Issue type filter test 1"))
                .setIssueType("issue"));
        Issue created2 = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Issue type filter test 2"))
                .setIssueType("incident"));
        Issue created3 = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Issue type filter test 3"))
                .setIssueType("task"));


        // List issues by issue type
        List<Issue> issues = getIssues(Map.of("issue_type", issueType));

        // Verify that the returned issues are of the specified issue type
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue -> assertThat(issue.issueType()).isEqualTo(issueType));

        // Cleanup
        deleteIssue(created1.iid());
        deleteIssue(created2.iid());
        deleteIssue(created3.iid());
    }

    @Test
    @DisplayName("ListIssues13: List all issues for a single label")
    void listIssuesBySingleLabelTest() {
        // Create issues with different sets of labels
        IssueCreateRequest createRequest1 = new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                .setLabels("label1,label2");
        Issue created1 = createIssue(createRequest1);
        IssueCreateRequest createRequest2 = new IssueCreateRequest(generateUniqueIssueTitle("Test title 2"))
                .setLabels("label2,label3");
        Issue created2 = createIssue(createRequest2);

        // List issues with label1
        List<Issue> issuesWithLabel1 = getIssues(Map.of("labels", "label1"));
        assertThat(issuesWithLabel1).as("Check that that result is not empty").isNotEmpty();
        assertThat(issuesWithLabel1)
                .as("Check that all returned issues have label1")
                .allSatisfy(issue -> assertThat(issue.labels()).contains("label1"));

        // Clean up
        deleteIssue(created1.iid());
        deleteIssue(created2.iid());
    }


    // for the sake of simplicity only testing excluding a single label here. But checking other filters would be good test as well - good tests to add.
    @Test
    @DisplayName("ListIssues14: Exclude issues with a specified label")
    void listIssuesExcludingLabelTest() {
        // Create issues with different sets of labels.
        // Using unique labels to avoid conflicts with other tests and existing issues in the project.
        String includedLabel = "included" + System.currentTimeMillis();
        String excludedLabel = "excluded" + System.currentTimeMillis();
        Issue excluded = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Excluded issue"))
                        .setLabels(excludedLabel+","+includedLabel));
        Issue included = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Included issue"))
                .setLabels(includedLabel));

        // Exclude issues having the specified label
        Map<String, Object> queryParams = Map.of(
                "not%5Blabels%5D", excludedLabel, // URL-encoded "not[labels]"
                "labels", includedLabel
        );

        // List issues excluding the specified label. Due to usage of unique labels, we should get only the one issue in the result.
        List<Issue> issues = getIssues(queryParams);
        assertThat(issues).as("Check that result is not empty").hasSize(1);
        assertThat(issues.get(0).iid()).isEqualTo(included.iid());

        // Cleanup
        deleteIssue(excluded.iid());
        deleteIssue(included.iid());
    }


    @ParameterizedTest
    @DisplayName("ListIssues15: Ordering results - order_by options")
    // test just verifies that the request is accepted and returns no error and a list of issues (could be empty - no data generated specifically to match the request).
    @ValueSource(strings = {"created_at", "updated_at", "priority", "due_date", "relative_position", "label_priority", "milestone_due", "popularity", "weight"})
    void listIssuesByOrderByFilterTest(String orderBy) {
        // Create an issue to ensure there is at least one issue in the project
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Order by filter test")));

        // List issues with the specified order_by filter
        List<Issue> issues = getIssues(Map.of("order_by", orderBy));
        assertThat(issues).as("Check that result is not null").isNotNull();

        //cleanup -
        deleteIssue(created.iid());
    }


    // for the sake of simplicity only testing order_by=created_at filter here. But checking other filters would be good test as well - good tests to add.
    @Test
    @DisplayName("ListIssues16: Order issues by creation date")
    void listIssuesOrderByCreatedAtTest() {
        // Create issues in a known order
        Issue first = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Order first")));
        Issue second = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Order second")));
        Issue third = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Order third")));

        // Request issues ordered by creation date ascending
        List<Issue> issues = getIssues(Map.of(
                "order_by", "created_at",
                "sort", "asc"
        ));

        // Verify that the returned issues are ordered by creation date
        assertThat(issues).extracting(Issue::createdAt).isSorted();

        // Cleanup
        deleteIssue(first.iid());
        deleteIssue(second.iid());
        deleteIssue(third.iid());
    }

    //Only testing scope=assigned_to_me as it is the only one that makes sense in this context. We don't have another user in this setup.
    @Test
    @DisplayName("ListIssues17: Filter by scope parameter")
    void listIssuesByScopeTest() {
        // Create an assigned and an unassigned issue
        Issue assignedIssue = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Assigned issue"))
                .setAssigneeId(USER_ID));
        Issue unassignedIssue = createIssue(
                new IssueCreateRequest(generateUniqueIssueTitle("Unassigned issue")));

        // List issues with scope=assigned_to_me
        List<Issue> issues = getIssues(Map.of("scope", "assigned_to_me"));

        // Verify that the returned issues are assigned to the current user
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue -> assertThat(issue.assignee().id()).isEqualTo(USER_ID));

        // Cleanup
        deleteIssue(assignedIssue.iid());
        deleteIssue(unassignedIssue.iid());
    }


    @Test
    @DisplayName("ListIssues18: Search issues by title")
    void listIssuesByTitleTest() {
        // Create an issue containing the search text in the title
        String searchText = "UniqueSearch" + UUID.randomUUID();

        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle(searchText + " title")));

        // Search for issues by title
        List<Issue> issues = getIssues(Map.of("search", searchText));

        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).anySatisfy(issue -> assertThat(issue.iid()).isEqualTo(created.iid()));

        // Cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("ListIssues19: Search issues by description")
    void listIssuesByDescriptionTest() {
        // Create an issue containing the search text in the description
        String searchText = "UniqueSearch" + UUID.randomUUID();

        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Test title"))
                .setDescription(searchText + " description"));

        // Search for issues by description
        List<Issue> issues = getIssues(Map.of("search", searchText));

        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).anySatisfy(issue -> assertThat(issue.iid()).isEqualTo(created.iid()));

        // Cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("ListIssues20: Search issues using asc or desc sorting")
    void listIssuesBySortingTest() {
        // Create issues in a known order
        Issue first = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Order first")));
        Issue second = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Order second")));

        // Request issues ordered by creation date ascending
        List<Issue> issuesAsc = getIssues(Map.of(
                "order_by", "created_at",
                "sort", "asc"
        ));

        // Verify that the returned issues are ordered by creation date ascending
        assertThat(issuesAsc).extracting(Issue::createdAt).isSorted();

        // Request issues ordered by creation date descending
        List<Issue> issuesDesc = getIssues(Map.of(
                "order_by", "created_at",
                "sort", "desc"
        ));

        // Verify that the returned issues are ordered by creation date descending
        assertThat(issuesDesc).extracting(Issue::createdAt).isSortedAccordingTo(Comparator.reverseOrder()); // Descending order

        // Cleanup
        deleteIssue(first.iid());
        deleteIssue(second.iid());
    }


    @ParameterizedTest
    @ValueSource(strings = {"opened", "closed"})
    @DisplayName("ListIssues21: List issues by state")
    void listIssuesByStateTest(String state) {
        IssueCreateRequest request =  new IssueCreateRequest(generateUniqueIssueTitle("State filter test"));
        Issue created = createIssue(request);

        if ("closed".equals(state)) {
            updateIssue(created.iid(), new IssueUpdateRequest().setStateEvent("close"));
        }

        // List issues by state
        List<Issue> issues = getIssues(Map.of("state", state));
        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue -> assertThat(issue.state()).isEqualTo(state));

        // Cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("ListIssues22: List issues updated after specified date")
    void listIssuesUpdatedAfterTest() {
        // Create an issue and record the time before update
        Instant beforeUpdate = Instant.now().minusSeconds(2);
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Updated after test")));

        // Update the issue after the lower boundary
        updateIssue(created.iid(),  new IssueUpdateRequest().setDescription("Updated after boundary"));

        // List issues updated after the lower boundary
        List<Issue> issues = getIssues(Map.of(
                "updated_after", beforeUpdate.toString()
        ));

        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue -> assertThat(issue.updatedAt()).isAfterOrEqualTo(beforeUpdate));

        // Cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("ListIssues23: List issues updated before specified date")
    void listIssuesUpdatedBeforeTest() {
        // Create an issue and record the time after update
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Updated before test")));
        updateIssue(created.iid(),  new IssueUpdateRequest().setDescription("Updated before boundary"));
        Instant afterUpdate = Instant.now().plusSeconds(2);

        // List issues updated before the upper boundary
        List<Issue> issues = getIssues(Map.of(
                "updated_before", afterUpdate.toString()
        ));

        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).allSatisfy(issue -> assertThat(issue.updatedAt()).isBeforeOrEqualTo(afterUpdate));

        // Cleanup
        deleteIssue(created.iid());
    }

    @Test
    @DisplayName("ListIssues24: Include detailed label information")
    void listIssuesWithLabelsDetailsTest() {
        String label = "details-" + UUID.randomUUID();

        // Create an issue with a unique label
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Labels details test"))
                .setLabels(label));

        // Request detailed label information
        Response response = getIssuesRaw(PROJECT_ID,
                Map.of(
                        "with_labels_details", true,
                        "labels", label
                )
        );

        assertThat(response.statusCode()).isEqualTo(200);

        // Find the created issue in the response
        List<Map<String, Object>> issues = response.jsonPath().getList("");

        // The created issue should be first and only in the list since we are filtering by the unique label.
        Map<String, Object> issueWithLabel = issues.get(0);

        // Verify detailed information for the provided label
        List<Map<String, Object>> labels = (List<Map<String, Object>>) issueWithLabel.get("labels");

        Map<String, Object> detailedLabel = labels.stream()
                .filter(issueLabel -> label.equals(issueLabel.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Label '" + label + "' was not found"));

        assertThat(detailedLabel)
                .as("Check detailed information for the created label")
                .containsEntry("name", label)
                .containsKey("id")
                .containsKey("description")
                .containsEntry("text_color", "#FFFFFF") //default value
                .containsKey("description_html")
                .containsKey("color")
                .containsEntry("archived", false);

        assertThat(((Number) detailedLabel.get("id")).longValue())
                .as("Label ID should be a positive number")
                .isPositive();

        // Cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("ListIssues25: List issues with multiple filters combined")
    void listIssuesWithMultipleFiltersTest() {
        // Create an issue with specific attributes
        String uniqueLabel = "multi-filter-" + UUID.randomUUID();
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Multi-filter test"))
                .setAssigneeId(USER_ID)
                .setLabels(uniqueLabel)
                .setConfidential(true));

        // List issues with multiple filters combined
        List<Issue> issues = getIssues(Map.of(
                "assignee_id", USER_ID,
                "labels", uniqueLabel,
                "confidential", true
        ));

        assertThat(issues).as("Check that result is not empty").isNotEmpty();
        assertThat(issues).anySatisfy(issue -> {
            assertThat(issue.iid()).isEqualTo(created.iid());
            assertThat(issue.assignee().id()).isEqualTo(USER_ID);
            assertThat(issue.labels()).contains(uniqueLabel);
            assertThat(issue.confidential()).isTrue();
        });

        // Cleanup
        deleteIssue(created.iid());
    }


    @Test
    @DisplayName("ListIssues26: List issues using URL encoded project path")
    void listIssuesUsingUrlEncodedProjectPathTest() {
        // Create an issue to ensure there is at least one issue in the project
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("URL encoded project path test")));

        // List issues using URL encoded project path
        Response response = getIssuesRaw(PROJECT_PATH, Map.of());

        //check response status code
        assertThat(response.statusCode()).isEqualTo(200);
        // Verify that the returned issues list is not empty
        List<Issue> issues =response.jsonPath().getList("", Issue.class);
        assertThat(issues).as("Check that result is not empty").isNotEmpty();

        // Cleanup
        deleteIssue(created.iid());
    }

}


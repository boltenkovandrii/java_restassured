package com.abnamro.assignment.tests;

import com.abnamro.assignment.model.Issue;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static com.abnamro.assignment.base.IssuesAPI.getIssues;
import static com.abnamro.assignment.base.IssuesAPI.getIssuesRaw;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class ListIssuesErrorsTest extends BaseTest {

    /*
        Tests for getting list of project issues with focus on error scenarios
    */

    @ParameterizedTest
    @ValueSource(strings = {"state", "issue_type", "sort", "scope"})
    @DisplayName("ListIssuesErrors1: Reject invalid query parameter values")
    void listIssuesWithInvalidEnumValueTest(String parameter) {
        // Request the issue list with an invalid parameter value
        Response response = getIssuesRaw(
                PROJECT_ID,
                Map.of(parameter, "invalid_value"));

        assertError(response, 400, parameter+" does not have a valid value");
    }

    @ParameterizedTest
    @ValueSource(strings = {"confidential", "with_labels_details"})
    @DisplayName("ListIssuesErrors2: Reject invalid boolean query parameters")
    void listIssuesWithInvalidValueTest(String parameter) {
        // Request the issue list with an invalid parameter value
        Response response = getIssuesRaw(
                PROJECT_ID,
                Map.of(parameter, "invalid_value"));

        assertError(response, 400, parameter+" is invalid");
    }


    @Test
    @DisplayName("ListIssuesErrors3: Reject author_id combined with author_username")
    void listIssuesWithConflictingAuthorFiltersTest() {
        // Request mutually exclusive author filters
        Response response = getIssuesRaw(
                PROJECT_ID,
                Map.of(
                        "author_id", USER_ID,
                        "author_username", USER_NAME
                ));

        assertError(response, 400, "author_id, author_username are mutually exclusive");
    }

    @Test
    @DisplayName("ListIssuesErrors4: Reject assignee_id combined with assignee_username")
    void listIssuesWithConflictingAssigneeFiltersTest() {
        // Request mutually exclusive assignee filters
        Response response = getIssuesRaw(
                PROJECT_ID,
                Map.of(
                        "assignee_id", USER_ID,
                        "assignee_username", USER_NAME
                ));

        assertError(response, 400, "assignee_id, assignee_username are mutually exclusive");
    }

    @Test
    @DisplayName("ListIssuesErrors5: Reject mutually exclusive milestone parameters")
    void listIssuesWithConflictingMilestoneFiltersTest() {
        // Request mutually exclusive milestone filters
        Response response = getIssuesRaw(
                PROJECT_ID,
                Map.of(
                        "milestone", "foo",
                        "milestone_id", "Any"
                ));

        assertError(response, 400, "milestone_id, milestone are mutually exclusive");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-date", "2026-99-99", "2026-21-01T25:00:00Z"})
    @DisplayName("ListIssuesErrors6: Reject invalid datetime parameters")
    void listIssuesWithInvalidDatetimeTest(String value) {
        // Request the issue list with an invalid datetime
        Response response = getIssuesRaw(
                PROJECT_ID,
                Map.of("created_after", value));

        assertError(response, 400, "created_after is invalid");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "abc"}) //treated as valid request - just returns empty list - assuming it is OK
    @DisplayName("ListIssuesErrors7: Reject invalid issue IIDs")
    void listIssuesWithInvalidIidsTest(String iid) {
        // Request the issue list with an invalid IID
        List<Issue> issues = getIssues(Map.of("iids[]", List.of(iid)));

        assertThat(issues).as("Check that no issues are returned for invalid IID '%s'", iid).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("ListIssuesErrors8: Attempt to list issues for invalid numeric project IDs")
    @ValueSource(longs = {-1, 0, 999999999})
    void listIssuesWithInvalidNumericProjectIdTest(long projectId) {
        // Request the issue list with an invalid project ID
        Response response = getIssuesRaw(projectId, Map.of());

        assertMessage(response, 404, "404 Project Not Found");
    }

    @ParameterizedTest
    @DisplayName("ListIssuesErrors9: Attempt to list issues for invalid string project IDs")
    @ValueSource(strings = {"invalid_project", "123abc", "!", "invalid/project/id", "project%20name%20with%20spaces", "project%2Fname%2Fwith%2Fslashes"})
    void listIssuesWithInvalidStringProjectIdTest(String projectId) {
        // Request the issue list with an invalid project ID
        Response response = getIssuesRaw(projectId, Map.of());

        assertMessage(response, 404, "404 Project Not Found");
    }

}



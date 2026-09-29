package com.abnamro.assignment.tests;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.model.IssueCreateRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
public class ListIssuesPaginationTest extends BaseTest {

    /*
        Tests for getting list of project issues with focus on pagination, both positive and negative scenarios.
        The tests create a set of issues with a unique search text to ensure that the pagination behavior can be tested in isolation from other issues in the project.
    */

    private static final int PAGE_SIZE = 2;
    private static final int PAGINATION_ISSUE_COUNT = 7; //Important to have number of issues not divisible by PAGE_SIZE to test last page behavior

    private static final List<Long> paginationIssueIids = new ArrayList<>();
    private static String paginationSearchText;


    @BeforeAll
    static void createPaginationTestData() {
        // Create a unique search text for pagination test issues
        paginationSearchText = "pagination-" + UUID.randomUUID();
        // Create multiple issues for pagination testing
        for (int i = 1; i <= PAGINATION_ISSUE_COUNT; i++) {
            Issue created = createIssue(new IssueCreateRequest(paginationSearchText + "-issue-" + i));
            paginationIssueIids.add(created.iid());
        }
    }

    @AfterAll
    static void deletePaginationTestData() {
        // Clean up the created issues after tests
        for(Long iid : paginationIssueIids) {
            deleteIssue(iid);
        }
    }

    @Test
    @DisplayName("Pagination7: List issues using default pagination without parameters")
    void listIssuesUsingDefaultPaginationNoParametersTest() {
        // List the pagination test issues without specifying page or per_page
        List<Issue> issues = getIssues();

        assertThat(issues).isNotEmpty();
        assertThat(issues.size()).isLessThanOrEqualTo(20); // Default per_page is 20
    }


    @Test
    @DisplayName("Pagination1: List issues using default pagination")
    void listIssuesUsingDefaultPaginationTest() {
        // List the pagination test issues without specifying page or per_page
        List<Issue> issues = getIssues(Map.of("search", paginationSearchText));

        assertThat(issues).isNotEmpty();
        assertThat(issues.size()).isLessThanOrEqualTo(20); // Default per_page is 20
    }

    @Test
    @DisplayName("Pagination2: Limit number of issues per page")
    void listIssuesWithPerPageTest() {
        // Request two issues per page
        List<Issue> issues = getIssues(Map.of(
                "search", paginationSearchText,
                "per_page", PAGE_SIZE
        ));

        assertThat(issues).hasSize(PAGE_SIZE);
    }

    @Test
    @DisplayName("Pagination3: Retrieve different issues from different pages")
    void listIssuesFromDifferentPagesTest() {
        //using order_by and sort to ensure consistent ordering of issues across pages

        // Request the first page
        List<Issue> page1 = getIssues(Map.of(
                "search", paginationSearchText,
                "per_page", PAGE_SIZE,
                "page", 1,
                "order_by", "created_at",
                "sort", "asc"
        ));

        // Request the second page
        List<Issue> page2 = getIssues(Map.of(
                "search", paginationSearchText,
                "per_page", PAGE_SIZE,
                "page", 2,
                "order_by", "created_at",
                "sort", "asc"
        ));

        // Verify that both pages have the expected number of issues and that they contain different issues
        assertThat(page1).hasSize(PAGE_SIZE);
        assertThat(page2).hasSize(PAGE_SIZE);

        // Verify that the issues in page1 and page2 are different by comparing their IIDs
        assertThat(page1)
                .extracting(Issue::iid)
                .doesNotContainAnyElementsOf(
                        page2.stream()
                                .map(Issue::iid)
                                .toList());
    }

    @Test
    @DisplayName("Pagination4: Retrieve all issues across multiple pages")
    void listIssuesAcrossMultiplePagesTest() {
        // Retrieve all pagination test issues page by page
        List<Long> returnedIids = new ArrayList<>();

        // using order_by and sort to ensure consistent ordering of issues across pages
        // Loop through pages until all issues
        //First check pages which has PAGE_SIZE issues
        for (int page = 1; page <= PAGINATION_ISSUE_COUNT/PAGE_SIZE; page++) {
            List<Issue> issues = getIssues(Map.of(
                    "search", paginationSearchText,
                    "per_page", PAGE_SIZE,
                    "page", page,
                    "order_by", "created_at",
                    "sort", "asc"
            ));

            returnedIids.addAll(
                    issues.stream()
                            .map(Issue::iid)
                            .toList());
        }

        // then check the last page which has less than PAGE_SIZE issues
        List<Issue> issues = getIssues(Map.of(
                "search", paginationSearchText,
                "per_page", PAGE_SIZE,
                "page", PAGINATION_ISSUE_COUNT/PAGE_SIZE + 1,
                "order_by", "created_at",
                "sort", "asc"
        ));
        //assert that the last page has the expected number of issues
        assertThat(issues).hasSize(PAGINATION_ISSUE_COUNT % PAGE_SIZE);

        returnedIids.addAll(
                issues.stream()
                        .map(Issue::iid)
                        .toList());



        //Check that all the created issues are returned across all pages
        assertThat(returnedIids)
                .doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(paginationIssueIids);
    }

    @Test
    @DisplayName("Pagination5: Return empty result after the last page, number of issues is not divisible by page size")
    void listIssuesAfterLastPageTest() {
        // Request a page beyond the available pagination range
        List<Issue> issues = getIssues(Map.of(
                "search", paginationSearchText,
                "per_page", PAGE_SIZE,
                "page", (PAGINATION_ISSUE_COUNT / PAGE_SIZE + 2) //Requesting a page beyond the last page - we assumed that the last page is PAGINATION_ISSUE_COUNT is not divisible by PAGE_SIZE
        ));

        assertThat(issues).isEmpty();
    }

    @Test
    @DisplayName("Pagination6: Return empty result after the last page, number of issues not divisible by page size")
    void listIssuesAfterLastPageNotDivisibleTest() {
        // Request a page beyond the available pagination range
        // Just select  second page providing per_page as PAGINATION_ISSUE_COUNT, so all pages are presented on a first page and second page should be empty
        List<Issue> issues = getIssues(Map.of(
                "search", paginationSearchText,
                "per_page", PAGINATION_ISSUE_COUNT,
                "page",  2
        ));

        assertThat(issues).isEmpty();
    }

    @Test
    // looks like negative value is simply ignored. Assuming it is OK and only checking hat the response is successful and returns valid list of issues
    @DisplayName("Pagination8: Request with negative per_page value")
    void listIssuesWithNegativePerPageValueTest() {
        // Request the issue list with a negative 'per_page' value
        Response response = getIssuesRaw(PROJECT_ID, Map.of(
                "search", paginationSearchText,
                "per_page", -5
        ));

        assertStatus(response, 200);
        List<Issue> issues = response.jsonPath().getList("", Issue.class);
        assertThat(issues).isNotEmpty();

        //check that the issues returned are matching the search text
        for (Issue issue : issues) {
            assertThat(issue.iid()).isIn(paginationIssueIids);
        }

    }

    @Test
    @DisplayName("Pagination9: Request with zero per_page value")
    void listIssuesWithZeroPerPageValueTest() {
        // Request the issue list with a zero 'per_page' value
        Response response = getIssuesRaw(PROJECT_ID, Map.of(
                "search", paginationSearchText,
                "per_page", 0
        ));

        assertError(response, 400, "per_page has a value not allowed");
    }

    @Test
    @DisplayName("Pagination10: Request with non-numeric per_page value")
    void listIssuesWithNonNumericPerPageValueTest() {
        // Request the issue list with a non-numeric 'per_page' value
        Response response = getIssuesRaw(PROJECT_ID, Map.of(
                "search", paginationSearchText,
                "per_page", "abc"
        ));

        assertError(response, 400, "per_page is invalid");
    }

    @Test
    // looks like negative value is simply ignored. Assuming it is OK and only checking hat the response is successful and returns valid list of issues
    @DisplayName("Pagination11: Request with negative 'page' value")
    void listIssuesWithNegativePageValueTest() {
        // Request the issue list with a negative 'page' value
        Response response = getIssuesRaw(PROJECT_ID, Map.of(
                "search", paginationSearchText,
                "page", -5
        ));

        assertStatus(response, 200);
        List<Issue> issues = response.jsonPath().getList("", Issue.class);
        assertThat(issues).isNotEmpty();

        //check that the issues returned are matching the search text
        for (Issue issue : issues) {
            assertThat(issue.iid()).isIn(paginationIssueIids);
        }
    }

    @Test
    // looks like zero value is simply ignored. Assuming it is OK and only checking hat the response is successful and returns valid list of issues
    @DisplayName("Pagination12: Request with zero 'page' value")
    void listIssuesWithZeroPageValueTest() {
        // Request the issue list with a zero 'page' value
        Response response = getIssuesRaw(PROJECT_ID, Map.of(
                "search", paginationSearchText,
                "page", 0
        ));

        assertStatus(response, 200);
        List<Issue> issues = response.jsonPath().getList("", Issue.class);
        assertThat(issues).isNotEmpty();

        //check that the issues returned are matching the search text
        for (Issue issue : issues) {
            assertThat(issue.iid()).isIn(paginationIssueIids);
        }
    }

    @Test
    @DisplayName("Pagination13: Request with non-numeric 'page' value")
    void listIssuesWithNonNumericPageValueTest() {
        // Request the issue list with a non-numeric 'page' value
        Response response = getIssuesRaw(PROJECT_ID, Map.of(
                "search", paginationSearchText,
                "page", "abc"
        ));

        assertError(response, 400, "page is invalid");
    }

}



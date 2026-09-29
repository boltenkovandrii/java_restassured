package com.abnamro.assignment.tests.manual;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.tests.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.abnamro.assignment.base.IssuesAPI.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CleanupTest extends BaseTest {


    // This test is intended to be run manually to reset the project state by deleting all issues created (and not cleaned) by tests.
    // NOTE: This test will only delete issues that start with the prefix defined in the TEST_ISSUE_PREFIX constant. Most issues created by tests have this prefix, but not all of them.
    // DANGEROUS: For environments with useful data, make sure that no other issues start with the same prefix, otherwise they will be deleted as well.



    @Test
    @Tag("Cleanup")
    @DisplayName("Cleaning up the test issues")
    void cleanupTest() {
        final int perPage = 100;
        final int maxIterations = 100;

        int currentPage = 1;
        int iterationCount = 0;

        while (iterationCount++ < maxIterations) {
            List<Issue> currentPageIssues = getIssues(Map.of(
                    "search", TEST_ISSUE_PREFIX,
                    "page", currentPage,
                    "per_page", perPage));

            // No more issues on the current page.
            if (currentPageIssues.isEmpty()) {
                break;
            }

            List<Issue> currentPageIssuesToDelete = currentPageIssues.stream()
                    .filter(issue -> issue.title().startsWith(TEST_ISSUE_PREFIX))
                    .toList();

            // No matching issues on this page - move to the next page.
            if (currentPageIssuesToDelete.isEmpty()) {
                currentPage++;
                continue;
            }

            // At least half of the page matches - delete them and continue.
            if (currentPageIssuesToDelete.size() >= perPage / 2) {
                currentPageIssuesToDelete.forEach(issue -> deleteIssue(issue.iid()));
                continue;
            }

            // Some matching issues, but fewer than half of the page.
            // Inspect the next page as well, because deleting from the current
            // page may cause issues from the next page to shift backwards.
            List<Issue> nextPageIssues = getIssues(Map.of(
                    "search", TEST_ISSUE_PREFIX,
                    "page", currentPage + 1,
                    "per_page", perPage));

            List<Issue> nextPageIssuesToDelete = nextPageIssues.stream()
                    .filter(issue -> issue.title().startsWith(TEST_ISSUE_PREFIX))
                    .toList();

            currentPageIssuesToDelete.forEach(issue -> deleteIssue(issue.iid()));
            nextPageIssuesToDelete.forEach(issue -> deleteIssue(issue.iid()));

            currentPage++;
        }

        assertThat(iterationCount)
                .as("Cleanup should finish before reaching the iteration limit")
                .isLessThanOrEqualTo(maxIterations);
    }

}

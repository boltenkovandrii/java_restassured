package com.abnamro.assignment.tests.manual;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.tests.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.abnamro.assignment.base.IssuesAPI.deleteIssue;
import static com.abnamro.assignment.base.IssuesAPI.getIssues;
import static org.assertj.core.api.Fail.fail;

public class ResetTest extends BaseTest {

    // This test is intended to be run manually to reset the project state by deleting all issues.
    // DANGEROUS: Never use in environment containing any useful data.

    @Test
    @Tag("Reset")
    @DisplayName("Remove all issues from the project")
    void resetIssuesTest() {
        final int perPage = 100;
        final int maxIterations = 100;

        for (int iterationCount = 1; iterationCount <= maxIterations; iterationCount++) {
            List<Issue> issues = getIssues(Map.of(
                    "page", 1,
                    "per_page", perPage));

            // No more issues in the project.
            if (issues.isEmpty()) {
                return;
            }

            // Delete all issues from the first page.
            issues.forEach(issue -> deleteIssue(issue.iid()));
        }

        fail("Reset did not finish within " + maxIterations + " iterations");
    }


}

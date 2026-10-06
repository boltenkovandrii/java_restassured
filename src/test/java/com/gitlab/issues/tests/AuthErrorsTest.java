package com.gitlab.issues.tests;

import com.gitlab.issues.base.RestBase.AuthMode;
import com.gitlab.issues.model.Issue;
import com.gitlab.issues.model.IssueCreateRequest;
import com.gitlab.issues.model.IssueUpdateRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.gitlab.issues.base.IssuesAPI.createIssue;
import static com.gitlab.issues.base.IssuesAPI.createIssueRaw;
import static com.gitlab.issues.base.IssuesAPI.deleteIssue;
import static com.gitlab.issues.base.IssuesAPI.deleteIssueRaw;
import static com.gitlab.issues.base.IssuesAPI.getIssue;
import static com.gitlab.issues.base.IssuesAPI.getIssueRaw;
import static com.gitlab.issues.base.IssuesAPI.getIssuesRaw;
import static com.gitlab.issues.base.IssuesAPI.updateIssueRaw;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("UnitTest")
class AuthErrorsTest extends BaseTest {

    /*
        Tests for incorrect authentication: absent, invalid and expired PRIVATE-TOKEN
     */

    @ParameterizedTest
    @EnumSource(value = AuthMode.class, mode = EnumSource.Mode.EXCLUDE, names = "VALID")
    @DisplayName("AuthErrors1: List issues with incorrect authentication")
    void listIssuesAuthTest(AuthMode mode) {
        Response response = getIssuesRaw(mode);
        assertAuthErrorR(response, mode);
    }

    @ParameterizedTest
    @EnumSource(value = AuthMode.class, mode = EnumSource.Mode.EXCLUDE, names = "VALID")
    @DisplayName("AuthErrors2: Retrieve issue with incorrect authentication")
    void retrieveIssueAuthTest(AuthMode mode) {
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Auth retrieve")));

        Response response = getIssueRaw(created.iid(), mode);
        assertAuthErrorR(response, mode);

        // Clean up
        deleteIssue(created.iid());
    }

    @ParameterizedTest
    @EnumSource(value = AuthMode.class, mode = EnumSource.Mode.EXCLUDE, names = "VALID")
    @DisplayName("AuthErrors3: Create issue with incorrect authentication")
    void createIssueAuthTest(AuthMode mode) {
        IssueCreateRequest request = new IssueCreateRequest(generateUniqueIssueTitle("Auth create"));
        Response response = createIssueRaw(request, mode);
        assertAuthErrorCUD(response, mode);
    }

    @ParameterizedTest
    @EnumSource(value = AuthMode.class, mode = EnumSource.Mode.EXCLUDE, names = "VALID")
    @DisplayName("AuthErrors4: Update issue with incorrect authentication")
    void updateIssueAuthTest(AuthMode mode) {
        IssueCreateRequest createRequest = new IssueCreateRequest(generateUniqueIssueTitle("Auth update"));
        Issue created = createIssue(createRequest);

        // Attempt to update the issue with incorrect authentication
        IssueUpdateRequest updateRequest = new IssueUpdateRequest();
        updateRequest.setTitle(generateUniqueIssueTitle("Updated title"));
        Response response = updateIssueRaw(created.iid(), updateRequest, mode);
        assertAuthErrorCUD(response, mode);

        // Verify that the issue was not updated
        Issue retrieved = getIssue(created.iid());
        assertThat(retrieved.title()).as("Title should not be changed").isEqualTo(createRequest.getTitle());

        // Clean up
        deleteIssue(created.iid());

    }

    @ParameterizedTest
    @EnumSource(value = AuthMode.class, mode = EnumSource.Mode.EXCLUDE, names = "VALID")
    @DisplayName("AuthErrors5: Delete issue with incorrect authentication")
    void deleteIssueAuthTest(AuthMode mode) {
        Issue created = createIssue(new IssueCreateRequest(generateUniqueIssueTitle("Auth delete")));

        // Attempt to delete the issue with incorrect authentication
        Response response = deleteIssueRaw(created.iid(), mode);
        assertAuthErrorCUD(response, mode);

        // Verify that the issue still exists
        assertThat(getIssue(created.iid())).as("Issue should still exist").isNotNull();

        // Clean up
        deleteIssue(created.iid());
    }


    void assertAuthErrorCUD(Response response, AuthMode mode) {
        switch (mode) {
            case ABSENT, INVALID, EMPTY:
                assertMessage(response, 401, "401 Unauthorized");
                break;
            case EXPIRED:
                assertError(response, 401, "invalid_token");
                break;
            default:
                throw new IllegalArgumentException("Unexpected AuthMode: " + mode);
        }
    }

    // If a user is not a member of a private project, a GET request on that project results in a 404 status code. So we need separate handling for the Read methods.
    // See https://docs.gitlab.com/api/issues/
    // NOTE: GitLab returns 404 for an unauthenticated GET request to the private project, while an explicitly supplied invalid token results in 401.
    // This appears to be endpoint-specific behavior related to private-project visibility. Consider this as not worth mentioning as a finding.
    void assertAuthErrorR(Response response, AuthMode mode) {
        switch (mode) {
            case ABSENT, EMPTY:
                assertMessage(response, 404, "404 Project Not Found");
                break;
            case INVALID:
                assertMessage(response, 401, "401 Unauthorized");
                break;
            case EXPIRED:
                assertError(response, 401, "invalid_token");
                break;
            default:
                throw new IllegalArgumentException("Unexpected AuthMode: " + mode);
        }
    }


}

package com.abnamro.assignment.base;

import com.abnamro.assignment.model.Issue;
import com.abnamro.assignment.model.IssueCreateRequest;
import com.abnamro.assignment.model.IssueUpdateRequest;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.configuration2.CompositeConfiguration;

import java.util.List;
import java.util.Map;

import static com.abnamro.assignment.base.RestBase.checkThatResponseIsSuccessful;



//class for business-level APIs
public class IssuesAPI {

    private static final CompositeConfiguration config = TestConfig.getConfiguration();
    private static final long PROJECT_ID = config.getLong("PROJECT_ID");

    private static final String PROJECT_ISSUES_URL = "/api/v4/projects/";

    private static final RestBase restBase = new RestBase(new RequestSpecBuilder()
            .setBaseUri(config.getString("GITLAB_BASE_URL"))
            .setContentType(ContentType.JSON)
            .addFilter(new AllureRestAssured())
            .build());



    ////////////// List all project issues ////////////////

    public static List<Issue> getIssues() {
        return getIssues(PROJECT_ID, Map.of());
    }

    public static List<Issue> getIssues(long projectId) {
        return getIssues(projectId, Map.of());
    }

    public static List<Issue> getIssues(Map<String, ?> queryParams) {
        return getIssues(PROJECT_ID, queryParams);
    }

    public static List<Issue> getIssues(long projectId, Map<String, ?> queryParams) {
        Response response = getIssuesRaw(projectId, queryParams);
        checkThatResponseIsSuccessful(response);
        return response.jsonPath().getList("", Issue.class);
    }

    public static Response getIssuesRaw(long projectId, Map<String, ?> queryParams) {
        return restBase.getInternal(projectIssuesUrl(projectId), queryParams);
    }

    public static Response getIssuesRaw(String projectId, Map<String, ?> queryParams) {
        return restBase.getInternal(projectIssuesUrl(projectId), queryParams);
    }



    ////////////// Retrieve a project issue ////////////////

    public static Issue getIssue(long issueIid) {
        return getIssue(PROJECT_ID, issueIid);
    }

    public static Issue getIssue(long projectId, long issueIid) {
        Response response = getIssueRaw(projectId, issueIid);
        checkThatResponseIsSuccessful(response);
        return response.as(Issue.class);
    }

    public static Response getIssueRaw(long projectId, long issueIid) {
        return restBase.getInternal(projectIssuesUrl(projectId) + "/" + issueIid);
    }

    public static Response getIssueRaw(String projectId, long issueIid) {
        return restBase.getInternal(projectIssuesUrl(projectId) + "/" + issueIid);
    }


    ////////////// Create a new issue ////////////////

    public static Issue createIssue(IssueCreateRequest request) {
        return createIssue(PROJECT_ID, request);
    }

    public static Issue createIssue(long projectId, IssueCreateRequest request) {
        Response response = createIssueRaw(projectId, request);
        checkThatResponseIsSuccessful(response);
        return response.as(Issue.class);
    }

    public static Response createIssueRaw(long projectId, IssueCreateRequest request) {
        return restBase.postInternal(request, projectIssuesUrl(projectId));
    }

    public static Response createIssueRaw(String projectId, IssueCreateRequest request) {
        return restBase.postInternal(request, projectIssuesUrl(projectId));
    }


    ////////////// Edit an existing issue ////////////////

    public static Issue updateIssue(long issueIid, IssueUpdateRequest request) {
        return updateIssue(PROJECT_ID, issueIid, request);
    }

    public static Issue updateIssue(long projectId, long issueIid, IssueUpdateRequest request) {
        Response response = updateIssueRaw(projectId, issueIid, request);
        checkThatResponseIsSuccessful(response);
        return response.as(Issue.class);
    }

    public static Response updateIssueRaw(long projectId, long issueIid, IssueUpdateRequest request) {
        return restBase.putInternal(request, projectIssuesUrl(projectId) + "/" + issueIid);
    }

    public static Response updateIssueRaw(String projectId, long issueIid, IssueUpdateRequest request) {
        return restBase.putInternal(request, projectIssuesUrl(projectId) + "/" + issueIid);
    }


    ////////////// Delete an issue ////////////////

    public static void deleteIssue(long issueIid) {
        deleteIssue(PROJECT_ID, issueIid);
    }

    public static void deleteIssue(long projectId, long issueIid) {
        Response response = deleteIssueRaw(projectId, issueIid);
        checkThatResponseIsSuccessful(response);
    }

    public static Response deleteIssueRaw(long projectId, long issueIid) {
        return restBase.deleteInternal(projectIssuesUrl(projectId) + "/" + issueIid);
    }

    public static Response deleteIssueRaw(String projectId, long issueIid) {
        return restBase.deleteInternal(projectIssuesUrl(projectId) + "/" + issueIid);
    }


    ////////////// Utilities ////////////////

    public static String projectIssuesUrl(long projectId) {
        return PROJECT_ISSUES_URL + projectId + "/issues";
    }

    private static String projectIssuesUrl(String projectId) {
        return PROJECT_ISSUES_URL + projectId.replace("/", "%2F") + "/issues";
    }

    public static RestBase getRestBase() {
        return restBase;
    }
}

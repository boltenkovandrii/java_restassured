package com.gitlab.issues.base;

import com.gitlab.issues.model.Issue;
import com.gitlab.issues.model.IssueCreateRequest;
import com.gitlab.issues.model.IssueUpdateRequest;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.configuration2.CompositeConfiguration;

import java.util.List;
import java.util.Map;


//class for business-level APIs
public class IssuesAPI {

    private static final CompositeConfiguration CONFIG = TestConfig.getConfiguration();

    private static final long PROJECT_ID = CONFIG.getLong("PROJECT_ID");

    private static final String PROJECT_ISSUES_URL = "/api/v4/projects/";

    private static final RestBase REST_BASE = new RestBase(new RequestSpecBuilder()
            .setBaseUri(CONFIG.getString("GITLAB_BASE_URL"))
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
        RestBase.checkThatResponseIsSuccessful(response);
        return response.jsonPath().getList("", Issue.class);
    }

    public static Response getIssuesRaw(long projectId, Map<String, ?> queryParams) {
        return REST_BASE.getInternal(projectIssuesUrl(projectId), queryParams);
    }

    public static Response getIssuesRaw(String projectId, Map<String, ?> queryParams) {
        return REST_BASE.getInternal(projectIssuesUrl(projectId), queryParams);
    }



    ////////////// Retrieve a project issue ////////////////

    public static Issue getIssue(long issueIid) {
        return getIssue(PROJECT_ID, issueIid);
    }

    public static Issue getIssue(long projectId, long issueIid) {
        Response response = getIssueRaw(projectId, issueIid);
        RestBase.checkThatResponseIsSuccessful(response);
        return response.as(Issue.class);
    }

    public static Response getIssueRaw(long projectId, long issueIid) {
        return REST_BASE.getInternal(projectIssuesUrl(projectId) + "/" + issueIid);
    }

    public static Response getIssueRaw(String projectId, long issueIid) {
        return REST_BASE.getInternal(projectIssuesUrl(projectId) + "/" + issueIid);
    }


    ////////////// Create a new issue ////////////////

    public static Issue createIssue(IssueCreateRequest request) {
        return createIssue(PROJECT_ID, request);
    }

    public static Issue createIssue(long projectId, IssueCreateRequest request) {
        Response response = createIssueRaw(projectId, request);
        RestBase.checkThatResponseIsSuccessful(response);
        return response.as(Issue.class);
    }

    public static Response createIssueRaw(long projectId, IssueCreateRequest request) {
        return REST_BASE.postInternal(request, projectIssuesUrl(projectId));
    }

    public static Response createIssueRaw(String projectId, IssueCreateRequest request) {
        return REST_BASE.postInternal(request, projectIssuesUrl(projectId));
    }


    ////////////// Edit an existing issue ////////////////

    public static Issue updateIssue(long issueIid, IssueUpdateRequest request) {
        return updateIssue(PROJECT_ID, issueIid, request);
    }

    public static Issue updateIssue(long projectId, long issueIid, IssueUpdateRequest request) {
        Response response = updateIssueRaw(projectId, issueIid, request);
        RestBase.checkThatResponseIsSuccessful(response);
        return response.as(Issue.class);
    }

    public static Response updateIssueRaw(long projectId, long issueIid, IssueUpdateRequest request) {
        return REST_BASE.putInternal(request, projectIssuesUrl(projectId) + "/" + issueIid);
    }

    public static Response updateIssueRaw(String projectId, long issueIid, IssueUpdateRequest request) {
        return REST_BASE.putInternal(request, projectIssuesUrl(projectId) + "/" + issueIid);
    }


    ////////////// Delete an issue ////////////////

    public static void deleteIssue(long issueIid) {
        deleteIssue(PROJECT_ID, issueIid);
    }

    public static void deleteIssue(long projectId, long issueIid) {
        Response response = deleteIssueRaw(projectId, issueIid);
        RestBase.checkThatResponseIsSuccessful(response);
    }

    public static Response deleteIssueRaw(long projectId, long issueIid) {
        return REST_BASE.deleteInternal(projectIssuesUrl(projectId) + "/" + issueIid);
    }

    public static Response deleteIssueRaw(String projectId, long issueIid) {
        return REST_BASE.deleteInternal(projectIssuesUrl(projectId) + "/" + issueIid);
    }


    ////////////// Requests with custom auth (for auth error tests) ////////////////

    public static Response getIssuesRaw(RestBase.AuthMode mode) {
        return REST_BASE.getInternal(projectIssuesUrl(PROJECT_ID), Map.of(), mode);
    }

    public static Response getIssueRaw(long issueIid, RestBase.AuthMode mode) {
        return REST_BASE.getInternal(projectIssuesUrl(PROJECT_ID) + "/" + issueIid, Map.of(), mode);
    }

    public static Response createIssueRaw(IssueCreateRequest request, RestBase.AuthMode mode) {
        return REST_BASE.postInternal(request, projectIssuesUrl(PROJECT_ID), mode);
    }

    public static Response updateIssueRaw(long issueIid, IssueUpdateRequest request, RestBase.AuthMode mode) {
        return REST_BASE.putInternal(request, projectIssuesUrl(PROJECT_ID) + "/" + issueIid, mode);
    }

    public static Response deleteIssueRaw(long issueIid, RestBase.AuthMode mode) {
        return REST_BASE.deleteInternal(projectIssuesUrl(PROJECT_ID) + "/" + issueIid, mode);
    }


    ////////////// Utilities ////////////////

    public static String projectIssuesUrl(long projectId) {
        return PROJECT_ISSUES_URL + projectId + "/issues";
    }

    private static String projectIssuesUrl(String projectId) {
        return PROJECT_ISSUES_URL + projectId.replace("/", "%2F") + "/issues";
    }

    public static RestBase getRestBase() {
        return REST_BASE;
    }
}

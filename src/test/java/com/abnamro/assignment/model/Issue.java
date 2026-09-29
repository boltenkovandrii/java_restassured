package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
public record Issue(
        Long id,
        Milestone milestone,
        GitLabUser author,
        String description,
        String state,
        Long iid,
        Integer project_id,
        List<GitLabUser> assignees,
        GitLabUser assignee,
        String type,
        List<String> labels,
        Integer upvotes,
        Integer downvotes,
        
        @JsonProperty("merge_requests_count")
        Integer mergeRequestsCount,
        String title,
        
        @JsonProperty("updated_at")
        Instant updatedAt,
        
        @JsonProperty("created_at")
        Instant createdAt,
        
        @JsonProperty("closed_at")
        Instant closedAt,
        
        @JsonProperty("closed_by")
        GitLabUser closedBy,
        Boolean subscribed,
        
        @JsonProperty("user_notes_count")
        Integer userNotesCount,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("due_date")
        LocalDate dueDate,
        Boolean imported,
        
        @JsonProperty("imported_from")
        String importedFrom,
        
        @JsonProperty("web_url")
        String webUrl,
        IssueReferences references,
        
        @JsonProperty("time_stats")
        TimeStats timeStats,
        Boolean confidential,
        
        @JsonProperty("discussion_locked")
        Boolean discussionLocked,
        
        @JsonProperty("issue_type")
        String issueType,
        String severity,
        
        @JsonProperty("task_completion_status")
        TaskCompletionStatus taskCompletionStatus,

        @JsonProperty("blocking_issues_count")
        Integer blockingIssuesCount,
        Integer weight,

        @JsonProperty("has_tasks")
        Boolean hasTasks,

        @JsonProperty("task_status")
        String taskStatus,

        @JsonProperty("_links")
        IssueLinks links,
        
        @JsonProperty("moved_to_id")
        Integer movedToId,
        
        @JsonProperty("service_desk_reply_to")
        String serviceDeskReplyTo
) {}

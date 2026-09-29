package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueCreateRequest {

        @JsonProperty("assignee_id")
        private Long assigneeId;

        private Boolean confidential;

        @JsonProperty("created_at")
        private String createdAt;

        private String description;

        @JsonProperty("discussion_to_resolve")
        private String discussionToResolve;

        @JsonProperty("due_date")
        private String dueDate;

        private Long iid;

        @JsonProperty("issue_type")
        private String issueType;

        private String labels;

        @JsonProperty("merge_request_to_resolve_discussions_of")
        private Long mergeRequestToResolveDiscussionsOf;

        @JsonProperty("milestone_id")
        private Long milestoneId;

        private String milestone;

        private String severity;

        @JsonProperty("start_date")
        private String startDate;

        private String title;

        public IssueCreateRequest(String title) {
                this.title = title;
        }

        public IssueCreateRequest setAssigneeId(Long assigneeId) {
                this.assigneeId = assigneeId;
                return this;
        }

        public IssueCreateRequest setConfidential(Boolean confidential) {
                this.confidential = confidential;
                return this;
        }

        public IssueCreateRequest setCreatedAt(String createdAt) {
                this.createdAt = createdAt;
                return this;
        }

        public IssueCreateRequest setDescription(String description) {
                this.description = description;
                return this;
        }

        public IssueCreateRequest setDiscussionToResolve(String discussionToResolve) {
                this.discussionToResolve = discussionToResolve;
                return this;
        }

        public IssueCreateRequest setDueDate(String dueDate) {
                this.dueDate = dueDate;
                return this;
        }

        public IssueCreateRequest setIid(Long iid) {
                this.iid = iid;
                return this;
        }

        public IssueCreateRequest setIssueType(String issueType) {
                this.issueType = issueType;
                return this;
        }

        public IssueCreateRequest setLabels(String labels) {
                this.labels = labels;
                return this;
        }

        public IssueCreateRequest setMergeRequestToResolveDiscussionsOf(
                Long mergeRequestToResolveDiscussionsOf) {
                this.mergeRequestToResolveDiscussionsOf = mergeRequestToResolveDiscussionsOf;
                return this;
        }

        public IssueCreateRequest setMilestoneId(Long milestoneId) {
                this.milestoneId = milestoneId;
                return this;
        }

        public IssueCreateRequest setMilestone(String milestone) {
                this.milestone = milestone;
                return this;
        }

        public IssueCreateRequest setSeverity(String severity) {
                this.severity = severity;
                return this;
        }

        public IssueCreateRequest setStartDate(String startDate) {
                this.startDate = startDate;
                return this;
        }

        public IssueCreateRequest setTitle(String title) {
                this.title = title;
                return this;
        }


        public Long getAssigneeId() {
                return assigneeId;
        }

        public Boolean getConfidential() {
                return confidential;
        }

        public String getCreatedAt() {
                return createdAt;
        }

        public String getDescription() {
                return description;
        }

        public String getDiscussionToResolve() {
                return discussionToResolve;
        }

        public String getDueDate() {
                return dueDate;
        }

        public Long getIid() {
                return iid;
        }

        public String getIssueType() {
                return issueType;
        }

        public String getLabels() {
                return labels;
        }

        public Long getMergeRequestToResolveDiscussionsOf() {
                return mergeRequestToResolveDiscussionsOf;
        }

        public Long getMilestoneId() {
                return milestoneId;
        }

        public String getMilestone() {
                return milestone;
        }

        public String getSeverity() {
                return severity;
        }

        public String getStartDate() {
                return startDate;
        }

        public String getTitle() {
                return title;
        }
}



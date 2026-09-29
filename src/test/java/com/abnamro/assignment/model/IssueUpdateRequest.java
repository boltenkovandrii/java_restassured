package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueUpdateRequest {

        @JsonProperty("add_labels")
        private String addLabels;

        @JsonProperty("assignee_ids")
        private List<Long> assigneeIds;

        private Boolean confidential;

        private String description;

        @JsonProperty("discussion_locked")
        private Boolean discussionLocked;

        @JsonProperty("due_date")
        private String dueDate;

        @JsonProperty("issue_type")
        private String issueType;

        private String labels;

        @JsonProperty("milestone_id")
        private Long milestoneId;

        private String milestone;

        @JsonProperty("remove_labels")
        private String removeLabels;

        private String severity;

        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("state_event")
        private String stateEvent;

        private String title;

        @JsonProperty("updated_at")
        private String updatedAt;


        public IssueUpdateRequest() {
        }

        public IssueUpdateRequest setAddLabels(String addLabels) {
                this.addLabels = addLabels;
                return this;
        }


        public IssueUpdateRequest setAssigneeIds(List<Long> assigneeIds) {
                this.assigneeIds = assigneeIds;
                return this;
        }

        public IssueUpdateRequest setConfidential(Boolean confidential) {
                this.confidential = confidential;
                return this;
        }

        public IssueUpdateRequest setDescription(String description) {
                this.description = description;
                return this;
        }

        public IssueUpdateRequest setDiscussionLocked(Boolean discussionLocked) {
                this.discussionLocked = discussionLocked;
                return this;
        }

        public IssueUpdateRequest setDueDate(String dueDate) {
                this.dueDate = dueDate;
                return this;
        }

        public IssueUpdateRequest setIssueType(String issueType) {
                this.issueType = issueType;
                return this;
        }

        public IssueUpdateRequest setLabels(String labels) {
                this.labels = labels;
                return this;
        }

        public IssueUpdateRequest setMilestone(String milestone) {
                this.milestone = milestone;
                return this;
        }

        public IssueUpdateRequest setMilestoneId(Long milestoneId) {
                this.milestoneId = milestoneId;
                return this;
        }

        public IssueUpdateRequest setRemoveLabels(String removeLabels) {
                this.removeLabels = removeLabels;
                return this;
        }

        public IssueUpdateRequest setSeverity(String severity) {
                this.severity = severity;
                return this;
        }

        public IssueUpdateRequest setStartDate(String startDate) {
                this.startDate = startDate;
                return this;
        }

        public IssueUpdateRequest setStateEvent(String stateEvent) {
                this.stateEvent = stateEvent;
                return this;
        }

        public IssueUpdateRequest setTitle(String title) {
                this.title = title;
                return this;
        }

        public IssueUpdateRequest setUpdatedAt(String updatedAt) {
                this.updatedAt = updatedAt;
                return this;
        }


        public String getAddLabels() {
                return addLabels;
        }

        public List<Long> getAssigneeIds() {
                return assigneeIds;
        }

        public Boolean getConfidential() {
                return confidential;
        }

        public String getDescription() {
                return description;
        }

        public Boolean getDiscussionLocked() {
                return discussionLocked;
        }

        public String getDueDate() {
                return dueDate;
        }

        public String getIssueType() {
                return issueType;
        }

        public String getLabels() {
                return labels;
        }

        public Long getMilestoneId() {
                return milestoneId;
        }

        public String getMilestone() {
                return milestone;
        }

        public String getRemoveLabels() {
                return removeLabels;
        }

        public String getSeverity() {
                return severity;
        }

        public String getStartDate() {
                return startDate;
        }

        public String getStateEvent() {
                return stateEvent;
        }

        public String getTitle() {
                return title;
        }

        public String getUpdatedAt() {
                return updatedAt;
        }
}

package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;

public record Milestone(
        @JsonProperty("due_date")
        LocalDate dueDate,

        @JsonProperty("project_id")
        Long projectId,

        String state,
        String description,
        int iid,
        Long id,
        String title,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("updated_at")
        Instant updatedAt,

        @JsonProperty("closed_at")
        Instant closedAt
) {
}
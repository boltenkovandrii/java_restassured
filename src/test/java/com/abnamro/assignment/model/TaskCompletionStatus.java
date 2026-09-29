package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TaskCompletionStatus(
        Integer count,

        @JsonProperty("completed_count")
        Integer completedCount
) {
}
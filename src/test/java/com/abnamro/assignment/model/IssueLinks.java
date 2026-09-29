package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IssueLinks(
        String self,
        String notes,

        @JsonProperty("award_emoji")
        String awardEmoji,

        String project,

        @JsonProperty("closed_as_duplicate_of")
        String closedAsDuplicateOf
) {
}
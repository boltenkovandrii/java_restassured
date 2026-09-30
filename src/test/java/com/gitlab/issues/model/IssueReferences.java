package com.gitlab.issues.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IssueReferences(
        @JsonProperty("short")
        String shortReference,

        String relative,
        String full
) {
}
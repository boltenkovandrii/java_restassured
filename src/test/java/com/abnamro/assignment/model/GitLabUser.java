package com.abnamro.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitLabUser(
        Long id,
        String username,
        String name,
        @JsonProperty("public_email")
        String publicEmail,
        String state,
        Boolean locked,

        @JsonProperty("avatar_url")
        String avatarUrl,

        @JsonProperty("web_url")
                String webUrl
        ) {
}
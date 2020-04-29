package com.opensource.gitlab.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class GitProject {
    Long id;
    String name;
    @JsonProperty(value = "default_branch")
    String defaultBranch;
    @JsonProperty(value = "ssh_url_to_repo")
    String sshUrlToRepo;
    @JsonProperty(value = "http_url_to_repo")
    String httpUrlToRepo;
    @JsonProperty(value = "path_with_namespace")
    String pathWithNamespace;
    @JsonProperty(value = "created_at")
    Date createdAt;
    @JsonProperty(value = "last_activity_at")
    Date lastActivityAt;
}

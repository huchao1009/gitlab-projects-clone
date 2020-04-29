package com.opensource.gitlab.vo;

import lombok.Data;

@Data
public class GitGroup {
    Long id;
    String name;
    String path;
    String description;
}

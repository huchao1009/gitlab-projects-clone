package com.opensource.gitlab.vo;

import lombok.Data;

@Data
public class GitBranch {
    String name;
    GitBranchCommit commit;
}

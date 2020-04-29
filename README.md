### 通过gitlab Api自动下载gitLab上的所有项目



### 背景

现在越来越多的公司采用gitlab来管理代码。但是公司越来越大，项目越来越多，一个个clone比较麻烦，于是写个java程序批量clone


### 思路

gitlab有提供api来获取git数据，利用这些信息clone项目

参考文档：[https://docs.gitlab.com/ee/api/projects.html#list-all-projects](https://links.jianshu.com/go?to=https%3A%2F%2Fdocs.gitlab.com%2Fee%2Fapi%2Fprojects.html%23list-all-projects)



### 步骤：

#### 1、申请gitlab token

 进入gitlab Settings页面, 点击Access Tokens标签

![image-20200429165609200](https://tva1.sinaimg.cn/large/007S8ZIlgy1geaqonuvnmj32k20qu7fs.jpg)



#### 2 、实现逻辑

- 1、通过API获取分组列表

- 2、遍历分组列表

- 3、通过指定分组名称获取项目列表

- 4、遍历项目列表

- 5、通过指定项目ID获取最近修改的分支名称

- 6、克隆指定分支的项目到指定目录

  

核心代码如下

```java
/**
 * 通过gitlab Api自动下载gitLab上的所有项目
 */
@Service
public class GitlabProjectCloneService {

    @Value("${git.gitlabUrl}")
    private String gitlabUrl;

    @Value("${git.privateToken}")
    private String privateToken;

    @Value("${git.projectDir}")
    private String projectDir;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

    @PostConstruct
    private void start() {
        File execDir = new File(projectDir);
        System.out.println("start get gitlab projects");
        List<GitGroup> groups = getGroups();
        try {
            System.out.println(objectMapper.writeValueAsString(groups));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        for (GitGroup group : groups) {
            List<GitProject> projects = getProjectsByGroup(group.getName());
            for (GitProject project : projects) {
                String lastActivityBranchName = getLastActivityBranchName(project.getId());
                if (StringUtils.isEmpty(lastActivityBranchName)) {
                    System.out.println("branches is empty, break project...");
                    continue;
                }
                clone(lastActivityBranchName, project, execDir);
            }
        }
        System.out.println("end get gitlab projects");
    }

    /**
     * 获取所有项目
     *
     * @return
     */
    private List<GitProject> getAllProjects() {
        String url = gitlabUrl + "/api/v3/projects?per_page={per_page}&private_token={private_token}";
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("per_page", "100");
        uriVariables.put("private_token", privateToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<GitProject>> responseType = new ParameterizedTypeReference<List<GitProject>>() {
        };
        ResponseEntity<List<GitProject>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, responseType, uriVariables);
        if (HttpStatus.OK == responseEntity.getStatusCode()) {
            return responseEntity.getBody();
        }
        return null;
    }

    /**
     * 获取指定分组下的项目
     *
     * @param group
     * @return
     */
    private List<GitProject> getProjectsByGroup(String group) {
        String url = gitlabUrl + "/api/v3/groups/{group}/projects?per_page={per_page}&private_token={private_token}";
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("group", group);
        uriVariables.put("per_page", "100");
        uriVariables.put("private_token", privateToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<GitProject>> responseType = new ParameterizedTypeReference<List<GitProject>>() {
        };
        ResponseEntity<List<GitProject>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, responseType, uriVariables);
        if (HttpStatus.OK == responseEntity.getStatusCode()) {
            return responseEntity.getBody();
        }
        return null;
    }

    /**
     * 获取分组列表
     *
     * @return
     */
    private List<GitGroup> getGroups() {
        String url = gitlabUrl + "/api/v3/groups?private_token={private_token}";
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("private_token", privateToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<GitGroup>> responseType = new ParameterizedTypeReference<List<GitGroup>>() {
        };
        ResponseEntity<List<GitGroup>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, responseType, uriVariables);
        if (HttpStatus.OK == responseEntity.getStatusCode()) {
            return responseEntity.getBody();
        }
        return null;
    }

    /**
     * 获取最近修改的分支名称
     *
     * @param projectId 项目ID
     * @return
     */
    private String getLastActivityBranchName(Long projectId) {
        List<GitBranch> branches = getBranches(projectId);
        if (CollectionUtils.isEmpty(branches)) {
            return "";
        }
        GitBranch gitBranch = getLastActivityBranch(branches);
        return gitBranch.getName();
    }

    /**
     * 获取指定项目的分支列表
     * https://docs.gitlab.com/ee/api/branches.html#branches-api
     *
     * @param projectId 项目ID
     * @return
     */
    private List<GitBranch> getBranches(Long projectId) {
        String url = gitlabUrl + "/api/v3/projects/{projectId}/repository/branches?private_token={privateToken}";
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("projectId", projectId);
        uriVariables.put("privateToken", privateToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity entity = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<GitBranch>> responseType = new ParameterizedTypeReference<List<GitBranch>>() {
        };
        ResponseEntity<List<GitBranch>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, responseType, uriVariables);
        if (HttpStatus.OK == responseEntity.getStatusCode()) {
            return responseEntity.getBody();
        }
        return null;
    }

    /**
     * 获取最近修改的分支
     *
     * @param gitBranches 分支列表
     * @return
     */
    private GitBranch getLastActivityBranch(final List<GitBranch> gitBranches) {
        GitBranch lastActivityBranch = gitBranches.get(0);
        for (GitBranch gitBranch : gitBranches) {
            if (gitBranch.getCommit().getCommittedDate().getTime() > lastActivityBranch.getCommit().getCommittedDate().getTime()) {
                lastActivityBranch = gitBranch;
            }
        }
        return lastActivityBranch;
    }

    private void clone(String branchName, GitProject gitProject, File execDir) {
        String command = String.format("git clone -b %s %s %s", branchName, gitProject.getHttpUrlToRepo(), gitProject.getPathWithNamespace());
        System.out.println("start exec command : " + command);
        try {
            Process exec = Runtime.getRuntime().exec(command, null, execDir);
            exec.waitFor();
            String successResult = inputStreamToString(exec.getInputStream());
            String errorResult = inputStreamToString(exec.getErrorStream());
            System.out.println("successResult: " + successResult);
            System.out.println("errorResult: " + errorResult);
            System.out.println("================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String inputStreamToString(final InputStream input) {
        StringBuilder result = new StringBuilder();
        Reader reader = new InputStreamReader(input);
        BufferedReader bf = new BufferedReader(reader);
        String line;
        try {
            while ((line = bf.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}

```

代码github地址：[https://github.com/huchao1009/gitlab-projects-clone](https://github.com/huchao1009/gitlab-projects-clone)
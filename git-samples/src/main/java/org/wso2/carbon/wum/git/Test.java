package org.wso2.carbon.wum.git;

import org.wso2.carbon.wum.git.resources.Commit;
import org.wso2.carbon.wum.git.resources.PullRequest;
import org.wso2.carbon.wum.git.resources.Resource;

import java.io.IOException;

public class Test {
    public static void main(String... args) {
        try {
            Resource resource = GitHubGraphQLUtils.createGitResourceFromURL("https://github.com/wso2/kubernetes-ei/6.3.x/commit/85905nffo000493nf");

            System.out.println("Organization: " + resource.getOrganization());
            System.out.println("Repository: " + resource.getRepository());
            System.out.println("Branch: " + resource.getBranch());

            if (resource instanceof Commit) {
                System.out.println("Hash: " + ((Commit) resource).getHash());
            } else if (resource instanceof PullRequest) {
                System.out.println("Number: " + ((PullRequest) resource).getNumber());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

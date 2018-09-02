package org.wso2.carbon.wum.git.resources;

/**
 * A model class which represents a Git Pull Request.
 */
public class PullRequest extends Resource {
    private String number;

    public PullRequest(String organization, String repository, String branch, String number) {
        setOrganization(organization);
        setRepository(repository);
        setBranch(branch);
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}

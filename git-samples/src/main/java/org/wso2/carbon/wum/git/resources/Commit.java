package org.wso2.carbon.wum.git.resources;

/**
 * A model class which represents a Git Commit.
 */
public class Commit extends Resource {
    private String hash;

    public Commit(String organization, String repository, String branch, String hash) {
        setOrganization(organization);
        setRepository(repository);
        setBranch(branch);
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}

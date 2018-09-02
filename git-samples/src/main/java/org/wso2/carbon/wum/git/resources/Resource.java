package org.wso2.carbon.wum.git.resources;

/**
 * An abstract class representing common data associated with any Git resource.
 */
public abstract class Resource {
    private String organization;
    private String repository;
    private String branch;

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}

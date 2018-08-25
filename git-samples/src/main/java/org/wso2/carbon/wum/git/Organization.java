package org.wso2.carbon.wum.git;

import io.aexp.nodes.graphql.annotations.GraphQLArgument;
import io.aexp.nodes.graphql.annotations.GraphQLProperty;

@GraphQLProperty(name = "organization",
        arguments = {@GraphQLArgument(name = "login", type = "String")}
)
public class Organization {
    private String name;
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

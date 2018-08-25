package org.wso2.carbon.wum.git;

import io.aexp.nodes.graphql.Argument;
import io.aexp.nodes.graphql.Arguments;
import io.aexp.nodes.graphql.GraphQLRequestEntity;
import io.aexp.nodes.graphql.GraphQLResponseEntity;
import io.aexp.nodes.graphql.GraphQLTemplate;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class GraphQLRequestSender {
    public static void main(String... args) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "bearer <access-token>");

        GraphQLRequestEntity requestEntity = null;

        try {
            requestEntity = GraphQLRequestEntity.Builder()
                    .headers(headers)
                    .url("https://api.github.com/graphql")
                    .request(Organization.class)
                    .arguments(new Arguments("organization", new Argument("login", "nasa")))
                    .build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        GraphQLTemplate graphQLTemplate = new GraphQLTemplate();

        GraphQLResponseEntity<Organization> responseEntity = graphQLTemplate.query(requestEntity, Organization.class);
        System.out.println(responseEntity.getResponse().getName());
        System.out.println(responseEntity.getResponse().getUrl());
    }
}

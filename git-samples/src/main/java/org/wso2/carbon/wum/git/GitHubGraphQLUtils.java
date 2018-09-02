package org.wso2.carbon.wum.git;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.wum.git.resources.Commit;
import org.wso2.carbon.wum.git.resources.PullRequest;
import org.wso2.carbon.wum.git.resources.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * This class contains utility functions required during communication with the GitHub GraphQL API.
 */
public class GitHubGraphQLUtils {
    //  set of valid GitHub organizations from which commits/pull requests are accepted
    private static final String[] validGitHubOrganizations = {"wso2"};
    //  access token required for communication with the GitHub GraphQL API
    private static final String gitHubAccessToken;

    static {
        //  check if the `GIT_ACCESS_TOKEN` is set
        gitHubAccessToken = Optional.ofNullable(System.getenv("GIT_ACCESS_TOKEN"))
                .orElseThrow(() -> new IllegalStateException("Environment variable `GIT_ACCESS_TOKEN` not defined"));
    }

    /**
     * Creates a Git {@link Resource} instance from the given URL. Only Git commit and pull request URLs are supported.
     *
     * @param url {@link String} version of the URL
     * @return a Git {@link Resource} (Commit/Pull Request), else throws an {@link IllegalArgumentException}
     * @throws IOException if an I/O error occurs when extracting the GitHub GraphQL result set
     */
    public static Resource createGitResourceFromURL(String url) throws IOException {
        //  check if the provided input string is null
        String nullCheckedString = Optional.ofNullable(url)
                .orElseThrow(() -> new IllegalArgumentException("URL string cannot be null"));

        //  validate if the provided input string is a valid URL
        URL validatedURL;
        try {
            validatedURL = new URL(nullCheckedString);
        } catch (MalformedURLException malformedException) {
            throw new IllegalArgumentException(url + " cannot be parsed as a URL");
        }

        //  capture the hostname of the validated URL
        String host = validatedURL.getHost();
        //  check if the URL is a GitHub based URL using the the hostname
        if (!host.equals("github.com")) {
            throw new IllegalArgumentException("Not a GitHub based URL");
        }

        //  capture the path of the validated URL
        String path = validatedURL.getPath();
        //  split the components of the path extracted from the validated URL
        String[] pathComponents = path.substring(1, path.length()).split("/");

        //  validate the GitHub organization
        String organization = pathComponents[0];
        Arrays.stream(validGitHubOrganizations)
                .filter(validOrganization -> validOrganization.equals(organization))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid organization " + organization));

        //  extract the GitHub repository from input
        String repository = pathComponents[1];

        //  obtain data from the GitHub GraphQL API based on the type of input
        if (pathComponents[2].equals("pull")) {
            //  if the URL refers to a GitHub Pull Request

            //  construct the data request query to GitHub GraphQL API
            String number = pathComponents[3];
            String validationQuery = "{ repository(owner: \"" + organization + "\", name: \"" + repository +
                    "\") { pullRequest(number: " + number + ") { id state baseRefName } } }";

            //  execute the GitHub GraphQL request
            JSONObject graphQLOutput =
                    getGitHubGraphQLResult(gitHubAccessToken, validationQuery).getJSONObject("data");

            //  validate the defined repository
            if (graphQLOutput.isNull("repository")) {
                throw new IllegalArgumentException(
                        "Invalid repository under organization " + organization + " -> " + repository);
            }

            //  validate the defined pull request based on its number
            if (graphQLOutput.getJSONObject("repository").isNull("pullRequest")) {
                throw new IllegalArgumentException(
                        "Invalid pull request number under repository " + repository + " -> " + number);
            }

            //  extract the pull request data
            JSONObject pullRequestData = graphQLOutput.getJSONObject("repository").getJSONObject("pullRequest");

            //  validate the pull request state
            String pullRequestState = pullRequestData.getString("state");
            if (!pullRequestState.equals("MERGED")) {
                throw new IllegalArgumentException(
                        "Invalid pull request state -> Pull Request #" + number + " is not merged");
            }

            return new PullRequest(organization, repository, pullRequestData.getString("baseRefName"), number);
        } else if (pathComponents[3].equals("commit")) {
            //  if the URL refers to a GitHub Commit

            //  construct the data request query to GitHub GraphQL API
            String validationQuery = "{ repository(owner: \"" + organization + "\", name: \"" + repository + "\")" +
                    " { refs(first: 100, refPrefix: \"refs/heads/\") { edges { node { name } } } } }";

            //  execute the GitHub GraphQL request
            JSONObject graphQLOutput =
                    getGitHubGraphQLResult(gitHubAccessToken, validationQuery).getJSONObject("data");

            //  validate the defined repository
            if (graphQLOutput.isNull("repository")) {
                throw new IllegalArgumentException(
                        "Invalid repository under organization " + organization + " -> " + repository);
            }

            //  extract the branch name from input
            String branch = pathComponents[2];
            //  extract the branches from GitHub GraphQL response data
            JSONArray branches =
                    graphQLOutput.getJSONObject("repository").getJSONObject("refs").getJSONArray("edges");

            //  loop through the branch array to check if the defined branch is valid
            boolean matchedBranch = IntStream
                    .range(0, branches.length())
                    .anyMatch(index ->
                            branches.getJSONObject(index).getJSONObject("node").getString("name").equals(branch));
            if (!matchedBranch) {
                throw new IllegalArgumentException("Invalid repository branch -> " + branch);
            }

            //  TODO: validate the Git commit

            return new Commit(organization, repository, branch, pathComponents[4]);
        } else {
            throw new IllegalArgumentException("Invalid Git resource type (supports commits and pull requests)");
        }
    }

    /**
     * Gets the result set for a defined GraphQL query from the GitHub GraphQL API.
     *
     * @param accessToken access token for GitHub GraphQL API
     * @param query       the {@link String} version of GraphQL query
     * @return a {@link JSONObject} corresponding to the GraphQL result set
     * @throws IOException if an I/O error occurs when extracting the GitHub GraphQL result set
     */
    private static JSONObject getGitHubGraphQLResult(String accessToken, String query) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://api.github.com/graphql");

            request.addHeader("Authorization", "Bearer " + accessToken);
            request.addHeader("Accept", "application/json");

            JSONObject requestContent = new JSONObject();
            requestContent.put("query", query);

            StringEntity entity = new StringEntity(requestContent.toString());
            request.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(request)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                return new JSONObject(builder.toString());
            }
        }
    }
}

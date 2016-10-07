package sonarqube;

import graphql.GraphQL;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Rules;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issue.SearchWsRequest;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.util.Arrays.asList;

public class Schema {

  public static GraphQL create() {

    GraphQLObjectType ruleType = newObject()
      .name("Rule")
      .field(newFieldDefinition()
        .name("key")
        .type(GraphQLString)
        .build())
      .field(newFieldDefinition()
        .name("name")
        .type(GraphQLString)
        .build())
      .build();

    GraphQLObjectType issueType = newObject()
      .name("Issue")
      .description("A SonarQube issue")
      .field(newFieldDefinition()
        .name("key")
        .description("The key.")
        .type(GraphQLString)
        .build())
      .field(newFieldDefinition()
        .name("message")
        .description("The msg")
        .type(GraphQLString)
        .build())
      .field(newFieldDefinition()
        .name("severity")
        .description("The severity")
        .type(GraphQLString)
        .build())
      .field(newFieldDefinition()
        .name("rule")
        .type(ruleType)
        .dataFetcher(env -> {
          Issues.Issue issue = (Issues.Issue) env.getSource();
          return searchRule(issue.getRule());
        })
        .build())
      .build();

    GraphQLObjectType queryType = newObject()
      .name("sonarQubeQuery")

      .field(newFieldDefinition()
        .type(ruleType)
        .name("rule")
        .argument(GraphQLArgument.newArgument()
          .name("key")
          .type(new GraphQLNonNull(GraphQLString))
          .build())
        .dataFetcher(env -> searchRule(env.getArgument("key")))
        .build())

      .field(newFieldDefinition()
        .type(new GraphQLList(new GraphQLTypeReference("Issue")))
        .name("issues")
        .argument(GraphQLArgument.newArgument()
          .name("severity")
          .type(GraphQLString)
          .build())
        .dataFetcher(env -> searchIssues(new SearchWsRequest().setSeverities(asList((String)env.getArgument("severity")))).getIssuesList())
        .build())

      .field(newFieldDefinition()
        .type(issueType)
        .name("issue")
        .dataFetcher(env -> searchIssues(new SearchWsRequest()).getIssues(0))
        .build())
      .build();

    GraphQLSchema schema = GraphQLSchema.newSchema()
      .query(queryType)
      .build();


    return new GraphQL(schema);
  }

  public static Issues.SearchWsResponse searchIssues(SearchWsRequest request) {
    System.out.println("HTTP ----- issues ");
    HttpConnector httpConnector = HttpConnector.newBuilder()
      .url("https://sonarqube.com")
      .build();
    WsClient wsClient = WsClientFactories.getDefault().newClient(httpConnector);
    request.setPageSize(5);
    return wsClient.issues().search(request);
  }

  public static Rules.Rule searchRule(String key) {
    System.out.println("HTTP ----- rule " + key);
    HttpConnector httpConnector = HttpConnector.newBuilder()
      .url("https://sonarqube.com")
      .build();
    WsClient wsClient = WsClientFactories.getDefault().newClient(httpConnector);
    return wsClient.rules().search(new org.sonarqube.ws.client.rule.SearchWsRequest().setRuleKey(key)).getRules(0);
  }
}

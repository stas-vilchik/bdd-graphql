package sonarqube;

import graphql.GraphQL;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import org.sonarqube.ws.Issues;
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
      .build();

    GraphQLObjectType queryType = newObject()
      .name("sonarQubeQuery")
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

    //Object data = .execute("{issues(severity: \"BLOCKER\") { key, message } }").getData();

  }

  public static Issues.SearchWsResponse searchIssues(SearchWsRequest request) {
    HttpConnector httpConnector = HttpConnector.newBuilder()
      .url("https://sonarqube.com")
      .build();
    WsClient wsClient = WsClientFactories.getDefault().newClient(httpConnector);
    return wsClient.issues().search(request);
  }
}


package sonarqube;

import graphql.GraphQL;
import spark.Spark;

import static spark.Spark.post;

public class WebServer {
  public static void main(String[] args) {
    GraphQL schema = Schema.create();

    Spark.port(8080);
    Spark.get("/", (req, res) -> "SQ GraphQL");
    post("/graphql", (req, res) -> {
      System.out.print("post " + req.body());
      return schema.execute(req.body());
    });
  }
}


package sonarqube;

import com.google.gson.Gson;
import graphql.GraphQL;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import spark.Spark;

import static spark.Spark.post;

public class WebServer {
  public static void main(String[] args) {
    GraphQL schema = Schema.create();

    Spark.port(8080);
    //Spark.get("/", (req, res) -> "SQ GraphQL");
    post("/graphql", (req, res) -> {
      //System.out.print("post ");
      try {
        res.header("Access-Control-Allow-Origin", "*");
        String json = req.body();
        System.out.println("got " + json);
        JSONObject jsonObj = (JSONObject) new JSONParser().parse(json);
        System.out.println("Executing " + jsonObj.get("query"));
        Object data = schema.execute((String)jsonObj.get("query"), new HashMap<>()).getData();
        String dataJson = new Gson().toJson(data);
        return "{\"data\":" + dataJson + "}";
      } catch (Throwable throwable) {
        throwable.printStackTrace();
        res.status(500);
        return "ko";
      }
    });
  }
}

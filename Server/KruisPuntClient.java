// set CLASSPATH=/users/remote/downloads/json-simple-1.1.1.jar;.
import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;

public class KruisPuntClient {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: java KruisPuntClient <hostname> <portnumber>");
      System.exit(1);
    }
    String serverName = args[0];
    int port = Integer.parseInt(args[1]);
    try {
      System.out.println("Trying to connect to " + serverName + " on port " + port);

      Socket client = new Socket(serverName, port);
      System.out.println("Connected to server on " + client.getRemoteSocketAddress());

      PrintWriter out = new PrintWriter(client.getOutputStream(), true);
      JSONObject obj = new JSONObject();
      obj.put("type", "PrimaryTrigger");
      obj.put("triggered", "true");
      obj.put("id", "1.1");
      out.println(obj);
      // out.println("hello server from " + client.getLocalSocketAddress());

      BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      System.out.println("Server says: " + in.readLine());
      //client.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
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

      OutputStreamWriter outStream = new OutputStreamWriter(client.getOutputStream());
      BufferedWriter out = new BufferedWriter(outStream);
      JSONObject obj = new JSONObject();
      obj.put("type", "PrimaryTrigger");
      obj.put("triggered", "true");
      obj.put("id", "1.1");
      out.write(obj + "\n");
      out.flush();

      InputStreamReader inStream = new InputStreamReader(client.getInputStream());
      BufferedReader in = new BufferedReader(inStream);
      System.out.println("Server says: " + in.readLine());
      
      inStream.close();
      outStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
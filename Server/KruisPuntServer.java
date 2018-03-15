// set CLASSPATH=/users/remote/downloads/json-simple-1.1.1.jar;.
import java.net.*;
import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class TrafficLight implements JSONAware {
  private String id;
  private String lightStatus;

  public TrafficLight(String id, String lightStatus) {
    this.id = id;
    this.lightStatus = lightStatus;
  }

  public String toJSONString() {
    JSONObject obj = new JSONObject();
    obj.put("id", id);
    obj.put("lightStatus", lightStatus);
    return obj.toString();
  }
}

public class KruisPuntServer {
   private ServerSocket serverSocket;
   private Socket server;
   private PrintWriter out;
   private BufferedReader in;
   
   public KruisPuntServer(int port) throws IOException {
      serverSocket = new ServerSocket(port);
      System.out.println("Listening on port " + serverSocket.getLocalPort() + "...");
   }

   public void run() {
      while(true) {
         try {            
            server = serverSocket.accept();
            System.out.println("Connected to client on " + server.getRemoteSocketAddress());
            
            JSONParser parser = new JSONParser();
            in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            String line = in.readLine();
            Object parsedLine = parser.parse(line);
            
            JSONObject receivedObj = (JSONObject)parsedLine;
            System.out.println("------RECEIVED DATA------");
            System.out.println("type: " + receivedObj.get("type"));
            System.out.println("triggered: " + receivedObj.get("triggered"));
            System.out.println("id: " + receivedObj.get("id"));
            System.out.println("-------------------------\n");       
            // System.out.println("Client says: " + line);
            
            JSONObject sendObj = new JSONObject();
            sendObj.put("type", "TrafficLightData");
            JSONArray list = new JSONArray();
            list.add(new TrafficLight("1.1", "green"));
            list.add(new TrafficLight("1.2", "red"));
            sendObj.put("trafficLights", list);

            System.out.println("SENT DATA: " + sendObj);

            out = new PrintWriter(server.getOutputStream(), true);
            out.println(sendObj);
            // out.println("hello client from " + server.getLocalSocketAddress());
            //server.close();
         } catch (IOException e) {
            e.printStackTrace();
            break;
         } catch (ParseException pe) {
           System.out.println("position: " + pe.getPosition());
           System.out.println(pe);
         }
      }
   }
   
  public static void main(String [] args) {
	  if(args.length != 1) {
		  System.out.println("Usage: java KruisPuntServer <portnumber>");
		  System.exit(1);
	  }
    int port = Integer.parseInt(args[0]);
    try {
      KruisPuntServer kpServer = new KruisPuntServer(port);
      kpServer.run();
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}
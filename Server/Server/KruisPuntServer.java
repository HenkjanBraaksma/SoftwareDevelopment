
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
    private BufferedReader in;
    private BufferedWriter out;
    private JSONParser parser;

    public KruisPuntServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Listening on port " + serverSocket.getLocalPort() + "...");
    }

    public void run() {
        try {
            server = serverSocket.accept();
            System.out.println("Connected to client on " + server.getRemoteSocketAddress());

            parser = new JSONParser();
            InputStreamReader inStream = new InputStreamReader(server.getInputStream());
            in = new BufferedReader(inStream);

            OutputStreamWriter outStream = new OutputStreamWriter(server.getOutputStream());
            out = new BufferedWriter(outStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                String line = in.readLine();
                Object parsedLine = parser.parse(line);

                JSONObject receivedObj = (JSONObject) parsedLine;
                JSONObject sendObj = new JSONObject();

                String type = (String)receivedObj.get("type");
                System.out.println("type: " + type);

                if("BridgeStatusData".equals(type)) {
                    System.out.println("opened: " + receivedObj.get("opened"));
                    sendObj.put("type", "BridgeData");
                    sendObj.put("bridgeOpen", true);
                }
                else if("TimeScaleData".equals(type)) {
                    System.out.println("scale: " + receivedObj.get("scale"));
                    sendObj.put("type", "TImeScaleVerifyData");
                    sendObj.put("status", true);
                }
                else if("PrimaryTrigger".equals(type) || "SecondaryTrigger".equals(type)) {
                    System.out.println("triggered: " + receivedObj.get("triggered"));
                    System.out.println("id: " + receivedObj.get("id"));
                    sendObj.put("type", "TrafficLightData");
                    JSONArray list = new JSONArray();
                    list.add(new TrafficLight("1.1", "red"));
                    list.add(new TrafficLight("1.2", "green"));
                    sendObj.put("trafficLights", list);
                }
                System.out.println("SENT DATA: " + sendObj);

                out.write(sendObj + "\n");
                out.flush();
//                inStream.close();
//                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ParseException pe) {
                System.out.println("position: " + pe.getPosition());
                System.out.println(pe);
                break;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java KruisPuntServer <portnumber>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        try {
            KruisPuntServer kpServer = new KruisPuntServer(port);
            kpServer.run();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

// set CLASSPATH=/users/remote/downloads/json-simple-1.1.1.jar;.

import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;
import java.util.concurrent.TimeUnit;

public class KruisPuntClient {

    private static BufferedWriter out;
    private static BufferedReader in;

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
            out = new BufferedWriter(outStream);
            InputStreamReader inStream = new InputStreamReader(client.getInputStream());
            in = new BufferedReader(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {

                JSONObject obj = new JSONObject();
                obj.put("type", "PrimaryTrigger");
                obj.put("triggered", "true");
                obj.put("id", "1.1");
                out.write(obj + "\n");
                out.flush();

                System.out.println("Server says: " + in.readLine());
                TimeUnit.SECONDS.sleep(5);
//                inStream.close();
//                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException ex) {
                System.out.println(ex);
                break;
            }
        }
    }
}

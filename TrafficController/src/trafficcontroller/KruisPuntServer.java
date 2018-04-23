package trafficcontroller;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class KruisPuntServer extends Thread {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStreamReader inStream;
    private OutputStreamWriter outStream;
    private BufferedWriter out;
    private ArrayList<TrafficLight> lights = new ArrayList<>();
    private ArrayList<TrafficLight> currentCyclus = new ArrayList<>();
    private ArrayList<TrafficLight> nextCyclus = new ArrayList<>();

    private final int CYCLUS_TIME;
    private final int DELAY; // time to wait after lights are red (in milliseconds)
    private int ticks = 0;
    private boolean isBridgeOpen = false;
    volatile boolean cyclusRunning = false;

    private Thread readerThread;

    public KruisPuntServer(int port, int cyclusTime, int delay) {
        this.CYCLUS_TIME = cyclusTime;
        this.DELAY = delay;
        try {
            createTrafficLights();
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {

        }
    }

    public void run() {
        try {
            clientSocket = serverSocket.accept();
            System.out.println("Connected to client on " + clientSocket.getRemoteSocketAddress());
            readerThread = new Thread(new Reader(clientSocket));
            readerThread.start();
        } catch (IOException ex) {

        }

        try {
            outStream = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
            out = new BufferedWriter(outStream);

            // set bridge light to green on start
            JSONArray changedLights = new JSONArray();
            TrafficLight tl = findTrafficLight("1.13");
            tl.setLightStatus("green");
            changedLights.add(tl);
            sendTrafficLightData(changedLights);
            cyclusRunning = true;
        } catch (IOException ex) {

        }

        while (true) {
            try {
                if (ticks == CYCLUS_TIME) { // cyclus time is up, stop and start new cyclus
                    cyclusRunning = false;
                    JSONArray oldCyclusLights = new JSONArray();
                    for (TrafficLight tl : currentCyclus) {
                        tl.setLightStatus("red");
                        oldCyclusLights.add(tl);
                    }
                    if (oldCyclusLights.size() > 0) {
                        sendTrafficLightData(oldCyclusLights); // set all lights from previous cyclus to red
                    }
                    ticks = 0;
                    Thread.sleep(DELAY); // wait for the specified delay time so all cars can leave junction
                    JSONArray newCyclusLights = new JSONArray();
                    currentCyclus.clear();
                    currentCyclus = new ArrayList<TrafficLight>(nextCyclus);
                    nextCyclus.clear();
                    boolean openBridge = false;
                    for (TrafficLight tl : currentCyclus) {
                        if (tl.getId().startsWith("4.")) { // a boat is waiting, open the bridge
                            openBridge = true;
                            TrafficLight bridgeLight = findTrafficLight("1.13");
                            bridgeLight.setLightStatus("red");
                            newCyclusLights.add(bridgeLight);
                        } else if (checkCanSetToGreen(tl)) { // check if no conflicting lanes are already green
                            tl.setLightStatus("green");
                            newCyclusLights.add(tl);
                        } else {
                            nextCyclus.add(tl); // conflicting lane is already green, wait until next cyclus
                        }
                    }
                    if (newCyclusLights.size() > 0) {
                        sendTrafficLightData(newCyclusLights); // set the lights for new cyclus to green
                    }
                    if (openBridge == true) {
                        sendBridgeData(true);
                    }
                    cyclusRunning = true;
                } else {
                    ticks += 1;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                try {
                    serverSocket.close();
                    inStream.close();
                    outStream.close();
                    readerThread.interrupt();
                    System.out.println("Server stopped.");
                    break;
                } catch (IOException ex1) {

                }
            } catch (IOException ex1) {

            }
        }
    }

    private TrafficLight findTrafficLight(String id) {
        for (TrafficLight t : lights) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        throw new IllegalStateException("Id " + id + " is not in the list.");
    }

    private boolean checkCanSetToGreen(TrafficLight tl) {
        for (String id : tl.getConflictingLanes()) {
            TrafficLight conflictingLight = findTrafficLight(id);
            if (conflictingLight.getLightStatus().equals("green")) {
                return false;
            }
        }
        return true;
    }

    private boolean checkIsInNextCyclus(TrafficLight tl) {
        for (TrafficLight t : nextCyclus) {
            if (t.getId().equals(tl.getId())) {
                return true;
            }
        }
        return false;
    }

    private void sendTrafficLightData(JSONArray changedLights) throws IOException {
        JSONObject obj = new JSONObject();
        obj.put("type", "TrafficLightData");
        obj.put("trafficLights", changedLights);
        out.write(obj + "\n");
        out.flush();
    }

    private void sendBridgeData(boolean open) throws IOException {
        JSONObject obj = new JSONObject();
        obj.put("type", "BridgeData");
        obj.put("bridgeOpen", open);
        out.write(obj + "\n");
        out.flush();
    }

    private void sendTimeScaleVerifyData(boolean status) throws IOException {
        JSONObject obj = new JSONObject();
        obj.put("type", "TimeScaleVerifyData");
        obj.put("status", status);
        out.write(obj + "\n");
        out.flush();
    }

    private void createTrafficLights() {
        lights.add(new TrafficLight("1.1", new String[]{"1.5", "1.9", "2.1", "2.4", "3.1.1", "3.1.2", "3.4.3", "3.4.4"}));
        lights.add(new TrafficLight("1.2", new String[]{"1.5", "1.9", "1.10", "1.11", "1.12", "2.1", "2.3", "3.1.1", "3.1.2", "3.3.3", "3.3.4"}));
        lights.add(new TrafficLight("1.3", new String[]{"1.5", "1.6", "1.7", "1.8", "1.11", "1.12", "2.1", "2.2", "3.1.1", "3.1.2", "3.2.3", "3.2.4"}));
        lights.add(new TrafficLight("1.4", new String[]{"1.6", "1.8", "1.12", "2.1", "2.2", "3.1.3", "3.1.4", "3.2.1", "3.2.2"}));
        lights.add(new TrafficLight("1.5", new String[]{"1.1", "1.2", "1.3", "1.6", "1.8", "1.9", "1.10", "1.11", "1.12", "2.2", "2.3", "2.4", "3.2.1", "3.2.2", "3.3.3", "3.3.4", "3.4.3", "3.4.4"}));
        lights.add(new TrafficLight("1.6", new String[]{"1.3", "1.4", "1.5", "1.7", "1.11"}));
        lights.add(new TrafficLight("1.7", new String[]{"1.3", "1.6", "1.11", "2.2", "2.3", "3.2.3", "3.2.4", "3.3.1", "3.3.2"}));
        lights.add(new TrafficLight("1.8", new String[]{"1.3", "1.4", "1.5", "1.11", "1.12", "2.1", "2.3", "3.1.3", "3.1.4", "3.3.1", "3.3.2"}));
        lights.add(new TrafficLight("1.9", new String[]{"1.1", "1.2", "1.5", "1.11", "1.12", "2.3", "2.4", "3.3.1", "3.3.2", "3.4.3", "3.4.4"}));
        lights.add(new TrafficLight("1.10", new String[]{"1.2", "1.5", "2.3", "2.4", "3.3.3", "3.3.4", "3.4.1", "3.4.2"}));
        lights.add(new TrafficLight("1.11", new String[]{"1.2", "1.3", "1.5", "1.6", "1.7", "1.8", "1.9", "2.2", "2.4", "3.2.3", "3.2.4", "3.4.1", "3.4.2"}));
        lights.add(new TrafficLight("1.12", new String[]{"1.2", "1.3", "1.4", "1.5", "1.8", "1.9", "2.1", "2.4", "3.1.3", "3.1.4", "3.4.1", "3.4.2"}));
        lights.add(new TrafficLight("1.13", new String[]{"4.1", "4.2"}));
        lights.add(new TrafficLight("2.1", new String[]{"1.1", "1.2", "1.3", "1.4", "1.8", "1.12"}));
        lights.add(new TrafficLight("2.2", new String[]{"1.3", "1.4", "1.5", "1.7", "1.11"}));
        lights.add(new TrafficLight("2.3", new String[]{"1.2", "1.5", "1.7", "1.8", "1.9", "1.10"}));
        lights.add(new TrafficLight("2.4", new String[]{"1.1", "1.5", "1.9", "1.10", "1.11", "1.12"}));
        lights.add(new TrafficLight("3.1.1", new String[]{"1.1", "1.2", "1.3"}));
        lights.add(new TrafficLight("3.1.2", new String[]{"1.1", "1.2", "1.3"}));
        lights.add(new TrafficLight("3.1.3", new String[]{"1.4", "1.8", "1.12"}));
        lights.add(new TrafficLight("3.1.4", new String[]{"1.4", "1.8", "1.12"}));
        lights.add(new TrafficLight("3.2.1", new String[]{"1.4", "1.5"}));
        lights.add(new TrafficLight("3.2.2", new String[]{"1.4", "1.5"}));
        lights.add(new TrafficLight("3.2.3", new String[]{"1.3", "1.7", "1.11"}));
        lights.add(new TrafficLight("3.2.4", new String[]{"1.3", "1.7", "1.11"}));
        lights.add(new TrafficLight("3.3.1", new String[]{"1.7", "1.8", "1.9"}));
        lights.add(new TrafficLight("3.3.2", new String[]{"1.7", "1.8", "1.9"}));
        lights.add(new TrafficLight("3.3.3", new String[]{"1.2", "1.5", "1.10"}));
        lights.add(new TrafficLight("3.3.4", new String[]{"1.2", "1.5", "1.10"}));
        lights.add(new TrafficLight("3.4.1", new String[]{"1.10", "1.11", "1.12"}));
        lights.add(new TrafficLight("3.4.2", new String[]{"1.10", "1.11", "1.12"}));
        lights.add(new TrafficLight("3.4.3", new String[]{"1.1", "1.5", "1.9"}));
        lights.add(new TrafficLight("3.4.4", new String[]{"1.1", "1.5", "1.9"}));
        lights.add(new TrafficLight("4.1", new String[]{"1.13", "4.2"}));
        lights.add(new TrafficLight("4.2", new String[]{"1.13", "4.1"}));
    }

    private class Reader implements Runnable {

        private final Socket clientSocket;
        private JSONParser parser;
        private BufferedReader in;

        private Reader(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                parser = new JSONParser();
                inStream = new InputStreamReader(clientSocket.getInputStream(), "UTF-8");
                in = new BufferedReader(inStream);
            } catch (IOException ex) {
            }
            while (true) {
                try {
                    String line = in.readLine();
                    if (line != null) {
                        try {
                            Object parsedLine = parser.parse(line);
                            JSONObject receivedObj = (JSONObject) parsedLine;

                            String type = (String) receivedObj.get("type");
                            if (type.equals("BridgeStatusData")) {
                                boolean opened = (boolean) receivedObj.get("opened");
                                isBridgeOpen = opened;
                                if (opened) { // bridge has opened
                                    for (TrafficLight tl : currentCyclus) {
                                        if (tl.getId().startsWith("4.")) { // set the light for the boat in current cyclus to green
                                            tl.setLightStatus("green");
                                            JSONArray changedLights = new JSONArray();
                                            changedLights.add(tl);
                                            sendTrafficLightData(changedLights);
                                            break;
                                        }
                                    }
                                } else { // bridge has closed, set car light near bridge to green
                                    TrafficLight tl = findTrafficLight("1.13");
                                    tl.setLightStatus("green");
                                    JSONArray changedLights = new JSONArray();
                                    changedLights.add(tl);
                                    sendTrafficLightData(changedLights);
                                }
                            } else if (type.equals("TimeScaleData")) {
                                sendTimeScaleVerifyData(false); // timescale has not been implemented (yet)
                            } else if (type.equals("SecondaryTrigger")) {
                                String lightId = (String) receivedObj.get("id");
                                TrafficLight tl = findTrafficLight(lightId);
                                boolean triggered = (boolean) receivedObj.get("triggered");
                                if (triggered) {
                                    tl.increaseVehicleCount(); // add car to the lane
                                }
                            } else if (type.equals("PrimaryTrigger")) {
                                String lightId = (String) receivedObj.get("id");
                                TrafficLight tl = findTrafficLight(lightId);
                                boolean triggered = (boolean) receivedObj.get("triggered");
                                if (!triggered && lightId.startsWith("1.")) {
                                    tl.decreaseVehicleCount(); // car leaves lane
                                }
                                if (triggered && tl.getLightStatus().equals("red")) { // check if light can be set to green
                                    boolean canGo = checkCanSetToGreen(tl);
                                    if (canGo && cyclusRunning == true) { // vehicle can go, add to current cyclus and set to green
                                        tl.setLightStatus("green");
                                        currentCyclus.add(tl);
                                        JSONArray changedLights = new JSONArray();
                                        changedLights.add(tl);
                                        sendTrafficLightData(changedLights);
                                    } else if (!checkIsInNextCyclus(tl)) { // add to next cyclus
                                        nextCyclus.add(tl);
                                    }

                                } else if (!triggered && tl.getLightStatus().equals("green")) { // vehicle goes past light
                                    if (lightId.startsWith("1.") && tl.getVehicleCount() == 0) { // car lane is empty, set to red after delay
                                        Timer timer = new Timer();
                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                if (tl.getVehicleCount() == 0 && cyclusRunning == true) { // check again if no car arrived or cyclus ended in the meantime
                                                    try {
                                                        tl.setLightStatus("red");
                                                        JSONArray changedLights = new JSONArray();
                                                        changedLights.add(tl);
                                                        sendTrafficLightData(changedLights);
                                                    } catch (IOException ex) {

                                                    }
                                                }
                                            }
                                        }, DELAY); // wait until the car has left the junction
                                    }
                                }
                            }
                        } catch (ParseException pe) {
                            System.out.println("position: " + pe.getPosition());
                            System.out.println(pe);
                        }
                    }
                    Thread.sleep(17); // 17 milliseconds = 60 fps (?)
                } catch (IOException | InterruptedException e) {

                }
            }
        }
    }
}

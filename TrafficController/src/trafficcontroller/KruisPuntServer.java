package trafficcontroller;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class KruisPuntServer {

    private final int localPort;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStreamReader inStream;
    private OutputStreamWriter outStream;
    private BufferedWriter out;
    private Bridge bridge;
    private ArrayList<TrafficLight> lights = new ArrayList<>();
    private ArrayList<TrafficLight> currentCyclus = new ArrayList<>();
    private ArrayList<TrafficLight> nextCyclus = new ArrayList<>();

    private final int CYCLUS_TIME;
    private final int DELAY; // time to wait after lights are red (in milliseconds)
    private int ticks = 0;
    private AtomicBoolean cyclusRunning = new AtomicBoolean(false);

    private Thread readerThread;
    final MessageObservable observable;

    public KruisPuntServer(int port, int cyclusTime, int delay, MessageObservable observable) {
        this.localPort = port;
        this.CYCLUS_TIME = cyclusTime;
        this.DELAY = delay;
        this.observable = observable;
        this.bridge = new Bridge();
        createTrafficLights();
        try {
            serverSocket = new ServerSocket(localPort);
            runServer();
        } catch (IOException ex) {
            observable.sendMessage("Could not open socket on port " + localPort);
        }
    }

    private void runServer() {
        try {
            clientSocket = serverSocket.accept();
            observable.sendMessage("Connected to client on " + clientSocket.getRemoteSocketAddress());

            readerThread = new Thread(new Reader(clientSocket));
            readerThread.start();

            outStream = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
            out = new BufferedWriter(outStream);

            // set bridge light to green on start
            JSONArray changedLights = new JSONArray();
            TrafficLight tl = findTrafficLight("1.13");
            tl.setLightStatus("green");
            changedLights.add(tl);
            sendTrafficLightData(changedLights);
            cyclusRunning.set(true);
        } catch (IOException ex) {

        }

        while (true) {
            try {
                if (ticks == CYCLUS_TIME) { // cyclus time is up, stop and start new cyclus
                    cyclusRunning.set(false);
                    JSONArray oldCyclusLights = new JSONArray();
                    for (TrafficLight tl : currentCyclus) {
                        if (tl.getLightStatus().equals("green")) {
                            tl.setLightStatus("red");
                            oldCyclusLights.add(tl);
                        }
                    }
                    if (oldCyclusLights.size() > 0) {
                        sendTrafficLightData(oldCyclusLights); // set all lights from previous cyclus to red
                    }

                    ticks = 0;
                    Thread.sleep(DELAY); // wait for the specified delay time so all cars can leave junction
                    currentCyclus.clear();
                    currentCyclus = new ArrayList<TrafficLight>(nextCyclus);
                    nextCyclus.clear();

                    JSONArray newCyclusLights = new JSONArray();
                    boolean doOpenBridge = false;
                    for (Iterator<TrafficLight> it = currentCyclus.iterator(); it.hasNext();) {
                        TrafficLight tl = it.next();
                        if (tl.getId().startsWith("4.")) {
                            if (!doOpenBridge && bridge.getStatus().equals("closed")) { // boat is waiting, indicate that bridge must be opened
                                doOpenBridge = true;
                                TrafficLight bridgeLight = findTrafficLight("1.13");
                                bridgeLight.setLightStatus("red");
                                newCyclusLights.add(bridgeLight);
                            } else { // bridge already opened for boat from the other side, move to next cyclus
                                if (!checkIsInNextCyclus(tl)) {
                                    nextCyclus.add(tl);
                                }
                                it.remove();
                            }
                        } else if (checkCanSetToGreen(tl)) {
                            tl.setLightStatus("green");
                            newCyclusLights.add(tl);
                        } else { // conflicting light is already green, move light to next cyclus
                            if (!checkIsInNextCyclus(tl)) {
                                nextCyclus.add(tl);
                            }
                            it.remove();
                        }
                    }
                    if (newCyclusLights.size() > 0) {
                        sendTrafficLightData(newCyclusLights); // set the lights for new cyclus to green
                    }
                    if (doOpenBridge) { // open the bridge (if not open and there is a boat)
                        bridge.setStatus("moving");
                        sendBridgeData(true);
                    } else if (bridge.getStatus().equals("open")) {
                        bridge.setStatus("moving");
                        sendBridgeData(false); // close the bridge (if open and there are no boats)
                    }
                    cyclusRunning.set(true);
                } else {
                    ticks += 1;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                try {
                    inStream.close();
                    outStream.close();
                    readerThread.interrupt();
                    serverSocket.close();
                    break;
                } catch (IOException ex1) {

                }
            } catch (IOException ex) {

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
        System.out.println("SERVER: " + obj + ".");
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
                            System.out.println("CLIENT: " + line);
                            Object parsedLine = parser.parse(line);
                            JSONObject receivedObj = (JSONObject) parsedLine;

                            String type = (String) receivedObj.get("type");
                            if (type.equals("BridgeStatusData")) {
                                boolean opened = (boolean) receivedObj.get("opened");
                                if (opened) { // bridge has opened
                                    bridge.setStatus("open");
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
                                    bridge.setStatus("closed");
                                    TrafficLight tl = findTrafficLight("1.13");
                                    tl.setLightStatus("green");
                                    JSONArray changedLights = new JSONArray();
                                    changedLights.add(tl);
                                    sendTrafficLightData(changedLights);
                                }
                            } else if (type.equals("TimeScaleData")) {
                                sendTimeScaleVerifyData(false); // timescale has not been implemented (yet)
                            } else if (type.equals("SecondaryTrigger")) {
                                boolean triggered = (boolean) receivedObj.get("triggered");
                                if (triggered) {
                                    String lightId = (String) receivedObj.get("id");
                                    TrafficLight tl = findTrafficLight(lightId);
                                    tl.increaseVehicleCount(); // add car to the lane
                                }
                            } else if (type.equals("PrimaryTrigger")) {
                                String lightId = (String) receivedObj.get("id");
                                TrafficLight tl = findTrafficLight(lightId);
                                boolean triggered = (boolean) receivedObj.get("triggered");
                                if (!triggered && lightId.startsWith("1.") && tl.getLightStatus().equals("green")) {
                                    tl.decreaseVehicleCount(); // car leaves lane
                                    if (tl.getVehicleCount() == 0) { // if lane is empty, set to red (after delay)
                                        Timer timer = new Timer();
                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                if (cyclusRunning.get() == true && tl.getVehicleCount() == 0) { // check if no cars arrived and cyclus still running
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
                                if (triggered && tl.getLightStatus().equals("red")) { // check if light can be set to green
                                    if (cyclusRunning.get() == true) {
                                        boolean canGo = checkCanSetToGreen(tl);
                                        if (canGo && (!lightId.startsWith("4.") || lightId.startsWith("4.") && bridge.getStatus().equals("open"))) { // vehicle can go, add to current cyclus and set to green
                                            tl.setLightStatus("green");
                                            currentCyclus.add(tl);
                                            JSONArray changedLights = new JSONArray();
                                            changedLights.add(tl);
                                            sendTrafficLightData(changedLights);
                                        } else if (!checkIsInNextCyclus(tl)) { // add to next cyclus
                                            nextCyclus.add(tl);
                                        }
                                    } else {
                                        Timer t = new Timer();
                                        t.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                if (!checkIsInNextCyclus(tl)) {
                                                    nextCyclus.add(tl);
                                                }
                                            }
                                        }, DELAY); // wait until cyclus is running again before adding to next
                                    }

                                }
                            }
                        } catch (ParseException pe) {
                            System.out.println("position: " + pe.getPosition());
                            System.out.println(pe);
                        }
                    } else {
                        Thread.sleep(17); // 17 milliseconds = 60 fps (?)
                    }
                } catch (IOException | InterruptedException e) {

                }
            }
        }
    }
}

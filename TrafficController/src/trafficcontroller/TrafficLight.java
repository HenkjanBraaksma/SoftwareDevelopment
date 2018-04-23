package trafficcontroller;


import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

class TrafficLight implements JSONAware {

    private final String id;
    private String lightStatus;
    private int vehicles;

    public final String[] conflicts;

    public TrafficLight(String id, String[] conflicts) {
        this.id = id;
        this.lightStatus = "red";
        this.vehicles = 0;
        this.conflicts = conflicts;
    }

    public String getId() {
        return this.id;
    }

    public String getLightStatus() {
        return this.lightStatus;
    }

    public void setLightStatus(String status) {
        this.lightStatus = status;
    }

    public int getVehicleCount() {
        return this.vehicles;
    }

    public void increaseVehicleCount() {
        this.vehicles += 1;
    }

    public void decreaseVehicleCount() {
        if(this.vehicles > 0) this.vehicles -= 1;
    }

    public String[] getConflictingLanes() {
        return this.conflicts;
    }

    @Override
    public String toJSONString() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("lightStatus", lightStatus);
        return obj.toString();
    }
}


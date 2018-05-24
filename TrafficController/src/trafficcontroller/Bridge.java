package trafficcontroller;

public class Bridge {

    private String status;

    public Bridge() {
        this.status = "closed";
    }

    public synchronized String getStatus() {
        return status;
    }

    public synchronized void setStatus(String status) {
        this.status = status;
    }
}

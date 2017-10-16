package acr.browser.lightning.domain;

/**
 * Created by roma on 11.06.2017.
 */

public class WeatherData {
    private String location;
    private int temp;
    private String text;
    private int code;
    private long lastUpdateTime;
    private boolean isCecius;
    //------------------------------------------------------

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isCecius() {
        return isCecius;
    }

    public void setCecius(boolean cecius) {
        isCecius = cecius;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "location='" + location + '\'' +
                ", temp=" + temp +
                ", text='" + text + '\'' +
                '}';
    }
}

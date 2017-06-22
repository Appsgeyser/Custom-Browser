package acr.browser.lightning.domain;

/**
 * Created by roma on 17.06.2017.
 */

public class GeoData {

    private String cityName;
    private String countryCode;
    private long lastUpdateTime;
    //------------------------------------------------------

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}

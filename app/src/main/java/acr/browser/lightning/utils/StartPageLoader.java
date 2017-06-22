package acr.browser.lightning.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import acr.browser.lightning.R;
import acr.browser.lightning.database.HistoryItem;
import acr.browser.lightning.domain.GeoData;
import acr.browser.lightning.domain.WeatherData;
import acr.browser.lightning.preference.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by roma on 11.06.2017.
 */

public class StartPageLoader {

    private static final long LOCATION_UPDATE_PERIOD = 2 * 60 * 60 * 1000;
    private static final long WEATHER_UPDATE_PERIOD = 20 * 60 * 1000;
    private static Map<Integer, Integer> weatherIconMap = new HashMap<>();

    static {
        weatherIconMap.put(0, R.drawable.simple_weather_icon_30);
        weatherIconMap.put(1, R.drawable.simple_weather_icon_23);
        weatherIconMap.put(2, R.drawable.simple_weather_icon_30);
        weatherIconMap.put(3, R.drawable.simple_weather_icon_27);
        weatherIconMap.put(4, R.drawable.simple_weather_icon_27);
        weatherIconMap.put(5, R.drawable.simple_weather_icon_26);
        weatherIconMap.put(6, R.drawable.simple_weather_icon_26);
        weatherIconMap.put(7, R.drawable.simple_weather_icon_26);
        weatherIconMap.put(8, R.drawable.simple_weather_icon_10);
        weatherIconMap.put(9, R.drawable.simple_weather_icon_10);
        weatherIconMap.put(10, R.drawable.simple_weather_icon_21);
        weatherIconMap.put(11, R.drawable.simple_weather_icon_23);
        weatherIconMap.put(12, R.drawable.simple_weather_icon_23);
        weatherIconMap.put(13, R.drawable.simple_weather_icon_25);
        weatherIconMap.put(14, R.drawable.simple_weather_icon_25);
        weatherIconMap.put(15, R.drawable.simple_weather_icon_25);
        weatherIconMap.put(16, R.drawable.simple_weather_icon_25);
        weatherIconMap.put(17, R.drawable.simple_weather_icon_28);
        weatherIconMap.put(18, R.drawable.simple_weather_icon_28);
        weatherIconMap.put(19, R.drawable.simple_weather_icon_10);
        weatherIconMap.put(20, R.drawable.simple_weather_icon_10);
        weatherIconMap.put(21, R.drawable.simple_weather_icon_10);
        weatherIconMap.put(22, R.drawable.simple_weather_icon_10);
        weatherIconMap.put(23, R.drawable.simple_weather_icon_30);
        weatherIconMap.put(24, R.drawable.simple_weather_icon_30);
        weatherIconMap.put(25, R.drawable.simple_weather_icon_04);
        weatherIconMap.put(26, R.drawable.simple_weather_icon_04);
        weatherIconMap.put(27, R.drawable.simple_weather_icon_07);
        weatherIconMap.put(28, R.drawable.simple_weather_icon_03);
        weatherIconMap.put(29, R.drawable.simple_weather_icon_07);
        weatherIconMap.put(30, R.drawable.simple_weather_icon_03);
        weatherIconMap.put(31, R.drawable.simple_weather_icon_02);
        weatherIconMap.put(32, R.drawable.simple_weather_icon_01);
        weatherIconMap.put(33, R.drawable.simple_weather_icon_02);
        weatherIconMap.put(34, R.drawable.simple_weather_icon_01);
        weatherIconMap.put(35, R.drawable.simple_weather_icon_26);
        weatherIconMap.put(36, R.drawable.simple_weather_icon_01);
        weatherIconMap.put(37, R.drawable.simple_weather_icon_27);
        weatherIconMap.put(38, R.drawable.simple_weather_icon_27);
        weatherIconMap.put(39, R.drawable.simple_weather_icon_27);
        weatherIconMap.put(40, R.drawable.simple_weather_icon_21);
        weatherIconMap.put(41, R.drawable.simple_weather_icon_25);
        weatherIconMap.put(42, R.drawable.simple_weather_icon_25);
        weatherIconMap.put(43, R.drawable.simple_weather_icon_25);
        weatherIconMap.put(44, R.drawable.simple_weather_icon_04);
        weatherIconMap.put(45, R.drawable.simple_weather_icon_27);
        weatherIconMap.put(46, R.drawable.simple_weather_icon_25);
        weatherIconMap.put(47, R.drawable.simple_weather_icon_27);
        weatherIconMap.put(3200, R.drawable.simple_weather_icon_04);
    }

    public static int getIconId(int code) {
        return weatherIconMap.containsKey(code) ? weatherIconMap.get(code) : R.drawable.simple_weather_icon_04;
    }

    public static void getWeather(final Activity activity, final PreferenceManager preferenceManager, final WeatherCallback callback, boolean force) {

        final WeatherData weatherData = preferenceManager.getWeatherData();

        if (force || weatherData == null || weatherData.getLocation().equals("") ||
                new Date().getTime() - weatherData.getLastUpdateTime() > WEATHER_UPDATE_PERIOD) {

            if (!preferenceManager.hasCity()) {

                LocationService.getLocationManager(activity).getLocation(new StartPageLoader.LocationHandler() {
                    @Override
                    public void onResult(Location location) {
                        if (location == null) {
                            StartPageLoader.requestGeoData(preferenceManager, new StartPageLoader.GeoDataHandler() {
                                @Override
                                public void onResult(GeoData geoData) {
                                    if (!geoData.getCityName().equals("")) {
                                        StartPageLoader.getWeather(preferenceManager, geoData.getCityName(), callback);
                                    } else {
                                        StartPageLoader.getWeather(preferenceManager, preferenceManager.getCity(), callback);
                                    }
                                }
                            });
                        } else {
                            StartPageLoader.getWeather(preferenceManager, location, callback);
                        }
                    }
                });
            } else {
                StartPageLoader.getWeather(preferenceManager, preferenceManager.getCity(), callback);
            }
        } else {
            callback.onWeatherResult(weatherData);
        }

    }

    public static void getWeather(final PreferenceManager preferenceManager, String locationName, final WeatherCallback callback) {
        requestWeather(preferenceManager, buildUrl(locationName), callback);
    }

    public static void getWeather(final PreferenceManager preferenceManager, Location location, final WeatherCallback callback) {
        requestWeather(preferenceManager, buildUrl(location), callback);
    }

    private static void requestWeather(final PreferenceManager preferenceManager, String url, final WeatherCallback callback) {
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onWeatherResult(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                WeatherData result = parseWeatherData(response.body().string());
                response.body().close();
                callback.onWeatherResult(result);
                if (result != null) {
                    result.setLastUpdateTime(new Date().getTime());
                }
                preferenceManager.setWeatherDataData(result);
                response.close();
            }
        });
    }

    private static String buildUrl(Location location) {
        return HttpUrl.parse("https://query.yahooapis.com/v1/public/yql?format=json&rnd=20175017&diagnostics=true&q=select%20*%20from%20" +
                "weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text=%22(" + location.getLatitude() + "," + location.getLongitude() + ")%22)"
        ).newBuilder().build().toString();
    }


    private static String buildUrl(String locationName) {
        return HttpUrl.parse("https://query.yahooapis.com/v1/public/yql?format=json&rnd=20175017&diagnostics=true&q=select%20*%20from%20" +
                "weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text=%22" + locationName + "%22)"
        ).newBuilder().build().toString();
    }

    private static WeatherData parseWeatherData(String json) {

        try {
            WeatherData weatherData = new WeatherData();
            JSONObject jsonObject = new JSONObject(json);
            JSONObject channel = jsonObject.getJSONObject("query").getJSONObject("results").getJSONObject("channel");
            weatherData.setLocation(channel.getJSONObject("location").getString("city"));
            weatherData.setTemp(channel.getJSONObject("item").getJSONObject("condition").getInt("temp"));
            weatherData.setText(channel.getJSONObject("item").getJSONObject("condition").getString("text"));
            weatherData.setCode(channel.getJSONObject("item").getJSONObject("condition").getInt("code"));

            Log.w("weather", weatherData.toString());
            return weatherData;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void requestGeoData(final PreferenceManager preferenceManager, final GeoDataHandler callback) {
        OkHttpClient client = new OkHttpClient();

        final GeoData geoData = preferenceManager.getGeoData();
        if (geoData.getCountryCode().equals("") ||
                new Date().getTime() - geoData.getLastUpdateTime() > LOCATION_UPDATE_PERIOD) {
            Request request = new Request.Builder()
                    .url("http://www.geoplugin.net/json.gp")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onResult(geoData);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        response.body().close();
                        GeoData result = new GeoData();
                        result.setCityName(jsonObject.getString("geoplugin_city"));
                        result.setCountryCode(jsonObject.getString("geoplugin_countryCode").toLowerCase());
                        result.setLastUpdateTime(new Date().getTime());
                        if (!result.getCountryCode().equals("")) {
                            preferenceManager.setGeoData(result);
                            callback.onResult(result);
                        } else {
                            callback.onResult(geoData);
                        }
                    } catch (JSONException e) {
                        callback.onResult(geoData);
                    }
                }
            });
        } else {
            callback.onResult(geoData);
        }
    }

    public static void requestBookmarks(final BoormarksHandler callback, String params) {
        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .url("http://frame.appsgeyser.com/api/bookmarks/json.php"+"?"+params)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onResult(new ArrayList<HistoryItem>());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    response.body().close();
                    JSONArray bookmarksJson = jsonObject.getJSONArray("browserLinks");
                    List<HistoryItem> historyItemList = new ArrayList<HistoryItem>();
                    for (int i = 0; i < bookmarksJson.length(); i++) {
                        HistoryItem result = new HistoryItem();
                        result.setTitle(bookmarksJson.getJSONObject(i).getString("title"));
                        result.setUrl(bookmarksJson.getJSONObject(i).getString("url"));
                        result.setImageUrl(bookmarksJson.getJSONObject(i).getString("icon"));
                        result.setShowOnMainScreen(true);
                        result.setPosition(99);
                        historyItemList.add(result);
                    }
                    callback.onResult(historyItemList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onResult(new ArrayList<HistoryItem>());
                }
            }
        });
    }

    public static void requestSearchEngine(final SearchEngineHandler callback, String params) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://frame.appsgeyser.com/api/searchengine/json.php"+"?"+params)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onResult(null, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    response.body().close();
                    callback.onResult(jsonObject.getInt("searchId"), jsonObject.getString("searchUrl"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onResult(null, null);
                }
            }
        });
    }

    public interface SearchEngineHandler {
        void onResult(Integer engineId, String url);
    }

    public interface LocationHandler {
        void onResult(Location location);
    }

    public interface GeoDataHandler {
        void onResult(GeoData geoData);
    }

    public interface BoormarksHandler {
        void onResult(List<HistoryItem> historyItem);
    }

    public interface WeatherCallback {
        void onWeatherResult(WeatherData weatherData);
    }
}

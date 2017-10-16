package acr.browser.lightning.service;

import android.app.NotificationManager;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobService;

import acr.browser.lightning.domain.WeatherData;
import acr.browser.lightning.notifiction.WeatherNotification;
import acr.browser.lightning.preference.PreferenceManager;
import acr.browser.lightning.utils.StartPageLoader;

/**
 * Created by emssika on 11.09.2017.
 */

public class WeatherJobService extends JobService {

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        final PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                StartPageLoader.WeatherCallback callback = new StartPageLoader.WeatherCallback() {
                    @Override
                    public void onWeatherResult(WeatherData weatherData) {
                        WeatherNotification weatherNotification = new WeatherNotification(getApplicationContext(), weatherData, preferenceManager);
                        weatherNotification.show();
                        if(weatherData!=null) {
                            weatherData.setCecius(preferenceManager.getWeatherData().isCecius());
                        }
                        preferenceManager.setWeatherDataData(weatherData);
                    }
                };

                StartPageLoader.requestWeather(preferenceManager, StartPageLoader.buildUrl(preferenceManager.getWeatherData().getLocation()), callback);

                return null;
            }
        };

        asyncTask.execute();

        Log.d("ProxyService", "onStart");
        return false;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        Log.d("ProxyService", "onStop");
        return false;
    }
}

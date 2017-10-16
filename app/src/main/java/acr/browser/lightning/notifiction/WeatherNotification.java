package acr.browser.lightning.notifiction;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.widget.RemoteViews;

import acr.browser.lightning.R;
import acr.browser.lightning.activity.MainActivity;
import acr.browser.lightning.domain.WeatherData;
import acr.browser.lightning.preference.PreferenceManager;
import acr.browser.lightning.utils.StartPageLoader;
import acr.browser.lightning.utils.TextDrawable;

/**
 * Created by emssika on 11.09.2017.
 */

public class WeatherNotification {

    private NotificationManager nm = null;
    private Notification notice = null;
    private int WEATHER_NOTIFICATION_ID = 1022;
    private String TAG = "WeatherNotification";

    public WeatherNotification(Context context, WeatherData weatherData, PreferenceManager preferenceManager) {
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (weatherData != null) {

            boolean celsius = preferenceManager.getWeatherData().isCecius();
            int temperatureValue = (int) (celsius ? (weatherData.getTemp() - 32) * (5. / 9.) : weatherData.getTemp());

            Drawable drawable = context.getResources().getDrawable(StartPageLoader.getIconId("simple", weatherData.getCode()));
            Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(wrappedDrawable, Color.WHITE);

            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.weather_notification);
            contentView.setImageViewResource(R.id.weatherImage, StartPageLoader.getIconId("simple", weatherData.getCode()));
            contentView.setTextViewText(R.id.title, weatherData.getText());
            contentView.setTextViewText(R.id.text, weatherData.getLocation());
            contentView.setTextViewText(R.id.countText, String.valueOf(temperatureValue) + "°");

            int numberIcon = R.drawable.notification_icon_default_white;

            if (temperatureValue > 120) {
                numberIcon = R.drawable.notification_icon_max_white;
            } else if (temperatureValue < -40) {
                numberIcon = R.drawable.notification_icon_min_white;
            } else if (temperatureValue < 0) {
                numberIcon = context.getResources().getIdentifier("notification_icon__" + Math.abs(temperatureValue) + "_white", "drawable", context.getPackageName());
            } else if (temperatureValue >= 0) {
                numberIcon = context.getResources().getIdentifier("notification_icon_" + temperatureValue + "_white", "drawable", context.getPackageName());
            }

            Intent openActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            openActivityIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            notice = new NotificationCompat.Builder(context)
                    .setContentTitle(weatherData.getText())
                    .setContentTitle(weatherData.getText())
                    .setSmallIcon(numberIcon)
                    .setContent(contentView)
                    .setContentIntent(resultPendingIntent)
                    .build();
            notice.flags |= Notification.FLAG_NO_CLEAR;

        } else {
            Log.d(TAG, "No weather data");
        }
    }

    public void show() {
        if (notice != null) {
            nm.notify(WEATHER_NOTIFICATION_ID, notice);
        }
    }

    public void remove() {
        if (notice != null) {
            nm.cancel(WEATHER_NOTIFICATION_ID);
        }
    }
}


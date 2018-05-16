package acr.browser.lightning.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.concurrent.TimeUnit;

import acr.browser.lightning.R;

public class WeatherNotificationService extends Service {
    NotificationManager nm;

    public WeatherNotificationService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendNotif();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }



    void sendNotif() {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.weather_notification);
        contentView.setImageViewResource(R.id.image, R.drawable.app_icon);
        contentView.setTextViewText(R.id.title, "Custom notification");
        contentView.setTextViewText(R.id.text, "This is a custom layout");


        Notification noti = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Ясно")
                .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                .setContent(contentView)
                .build();
        nm.notify(777, noti);
    }
}

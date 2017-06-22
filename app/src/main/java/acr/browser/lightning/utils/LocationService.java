package acr.browser.lightning.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roma on 12.06.2017.
 */

public class LocationService implements OnSuccessListener<Location> {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static LocationService instance = null;

    public Location location;
    private StartPageLoader.LocationHandler locationHandler;
    private FusedLocationProviderClient mFusedLocationClient;

    Activity activity;

    public static LocationService getLocationManager(Activity context) {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    private LocationService(Activity context) {
        this.activity = context;
        initLocationService(context);
    }

    private void requestRuntimePermissions(Activity context) {
        List<String> permissions = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(context, permissions.toArray(new String[permissions.size()]),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private boolean checkPermitions(Activity activity){
        return  !(Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Sets up location service after permissions is granted
     */
    @TargetApi(23)
    private void initLocationService(Activity context) {


        if(!checkPermitions(activity)){
            requestRuntimePermissions(activity);
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(context, this);
    }

    public void getLocation(StartPageLoader.LocationHandler locationHandler) {
        this.locationHandler = locationHandler;

        if(!checkPermitions(activity)){
            requestRuntimePermissions(activity);
            return;
        }
        if (location == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(activity, this);
        } else {
            locationHandler.onResult(location);
        }
    }

    @Override
    public void onSuccess(Location location) {
        this.location = location;
        locationHandler.onResult(location);
    }

}
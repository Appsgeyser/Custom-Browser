package acr.browser.lightning.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.anthonycr.bonsai.Schedulers;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import acr.browser.lightning.BuildConfig;
import acr.browser.lightning.R;
import acr.browser.lightning.config.Config;
import acr.browser.lightning.config.ThemeManager;
import acr.browser.lightning.database.HistoryItem;
import acr.browser.lightning.database.bookmark.BookmarkModel;
import acr.browser.lightning.database.bookmark.legacy.LegacyBookmarkManager;
import acr.browser.lightning.preference.PreferenceManager;
import acr.browser.lightning.service.WeatherJobService;
import acr.browser.lightning.utils.FileUtils;
import acr.browser.lightning.utils.MemoryLeakUtils;
import acr.browser.lightning.utils.Preconditions;
import acr.browser.lightning.utils.StartPageLoader;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BrowserApp extends MultiDexApplication {

    private static final String TAG = "BrowserApp";
    private static volatile Config config;
    private static volatile ThemeManager themeManager;
    @Nullable
    private static AppComponent sAppComponent;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT);
    }

    @Inject
    PreferenceManager mPreferenceManager;
    @Inject
    BookmarkModel mBookmarkModel;

    @NonNull
    public static AppComponent getAppComponent() {
        Preconditions.checkNonNull(sAppComponent);
        return sAppComponent;
    }

    /**
     * Determines whether this is a release build.
     *
     * @return true if this is a release build, false otherwise.
     */
    public static boolean isRelease() {
        return true;
    }

    public static void copyToClipboard(@NonNull Context context, @NonNull String string) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("URL", string);
        clipboard.setPrimaryClip(clip);
    }

    public static Config getConfig() {
        return config;
    }

    public static ThemeManager getThemeManager() {
        return themeManager;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, @NonNull Throwable ex) {

                if (BuildConfig.DEBUG) {
                    FileUtils.writeCrashToStorage(ex);
                }

                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, ex);
                } else {
                    System.exit(2);
                }
            }
        });

        sAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        sAppComponent.inject(this);
        loadConfig();

        Context context = getApplicationContext();
        final String params = "wid=" + context.getResources().getString(R.string.widgetID) + "&"
                + "advid=" + mPreferenceManager.getAdvertisingId() + "$"
                + "pn=" + context.getPackageName() + "&" + "v=" + context.getResources().getString(R.string.platformVersion)
                + "apiKey=" + context.getResources().getString(R.string.api_key);
        loadAdSettings(params);
        Schedulers.worker().execute(new Runnable() {
            @Override
            public void run() {
                List<HistoryItem> oldBookmarks = LegacyBookmarkManager.destructiveGetBookmarks(BrowserApp.this);

                if (!oldBookmarks.isEmpty()) {
                    // If there are old bookmarks, import them
                    mBookmarkModel.addBookmarkList(oldBookmarks).subscribeOn(Schedulers.io()).subscribe();
                } else if (!mPreferenceManager.getBookmarksApplies()) {
                    // If the database is empty, fill it from the assets list
                    List<HistoryItem> assetsBookmarks = config.getBookmarksList();
                    mBookmarkModel.addBookmarkList(assetsBookmarks).subscribeOn(Schedulers.io()).subscribe();
                    mPreferenceManager.setBookmarksApplied(true);
                }
                StartPageLoader.requestBookmarks(new StartPageLoader.BoormarksHandler() {
                    @Override
                    public void onResult(List<HistoryItem> historyItemList) {
                        for (HistoryItem bookmark : historyItemList) {
                            mBookmarkModel.addBookmarkIfNotExists(bookmark, false)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.main())
                                    .subscribe();
                        }
                    }
                }, params);
                if (!mPreferenceManager.getSearchEngineApplies()) {
                    StartPageLoader.requestSearchEngine(new StartPageLoader.SearchEngineHandler() {
                        @Override
                        public void onResult(Integer engineId, String url) {
                            if (engineId != null) {
                                mPreferenceManager.setSearchChoice(engineId);
                                if (engineId.equals(0) && url != null && !url.equals("")) {
                                    mPreferenceManager.setSearchUrl(url);
                                }
                            }
                        }
                    }, params);
                }
            }
        });

       /* if (mPreferenceManager.getUseLeakCanary() && !isRelease()) {
            LeakCanary.install(this);
        }
        if (!isRelease() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }*/

        registerActivityLifecycleCallbacks(new MemoryLeakUtils.LifecycleAdapter() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG, "Cleaning up after the Android framework");
                MemoryLeakUtils.clearNextServedView(activity, BrowserApp.this);
            }
        });

        themeManager = new ThemeManager(getApplicationContext());

        if (mPreferenceManager.getNotificationWeatherEnabled()) {

            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
            Job myJob = dispatcher.newJobBuilder()
                    .setService(WeatherJobService.class)
                    .setTag("proxy-weather-job")
                    .setRecurring(true)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(25 * 60 * 60, 35 * 60 * 60))
                    .setReplaceCurrent(true)
                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                    .build();
            dispatcher.mustSchedule(myJob);
        }
    }

    private void loadAdSettings(String params) {

        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .url("http://frame.appsgeyser.com/api/configuration/json.php" + "?" + params)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Request ads settings failed http://frame.appsgeyser.com/api/configuration/json.php");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonAdsSettings = new JSONObject(response.body().string());
                    response.body().close();
                    JSONObject adsSettingsJsonObject = jsonAdsSettings.getJSONObject("showAds");
                    try {
                        mPreferenceManager.setAdsNewIncognitoTab(adsSettingsJsonObject.getBoolean("newIncognitoTab"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Can't cast newIncognitoTab to boolean " + adsSettingsJsonObject);
                    }

                    try {
                        mPreferenceManager.setAdsOnFirstPageLoadFinished(adsSettingsJsonObject.getBoolean("onFirstPageFinishLoad"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Can't cast newIncognitoTab to boolean " + adsSettingsJsonObject);
                    }

                    try {
                        mPreferenceManager.setAdsOnHomePagePressed(adsSettingsJsonObject.getBoolean("onHomePagePressed"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Can't cast newIncognitoTab to boolean " + adsSettingsJsonObject);
                    }

                    try {
                        mPreferenceManager.setAdsNewTabInMinutes(adsSettingsJsonObject.getInt("newTabInMinutes"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Can't cast newIncognitoTab to int " + adsSettingsJsonObject);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Request ads settings failed http://frame.appsgeyser.com/api/configuration/json.php response = " + response);

                }
            }
        });
    }

    private void loadConfig() {
        config = new Config(getApplicationContext(), mPreferenceManager);
    }
}

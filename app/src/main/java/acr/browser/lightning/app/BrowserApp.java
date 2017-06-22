package acr.browser.lightning.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.anthonycr.bonsai.Schedulers;
import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.analytics.Analytics;
import com.appsgeyser.sdk.deviceidparser.DeviceIdParameters;
import com.appsgeyser.sdk.deviceidparser.DeviceIdParser;
import com.appsgeyser.sdk.deviceidparser.IDeviceIdParserListener;

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
import acr.browser.lightning.utils.FileUtils;
import acr.browser.lightning.utils.MemoryLeakUtils;
import acr.browser.lightning.utils.Preconditions;
import acr.browser.lightning.utils.StartPageLoader;

public class BrowserApp extends MultiDexApplication implements IDeviceIdParserListener {

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
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppsgeyserSDK.takeOff(this, getString(R.string.widgetID));
        Analytics appsgeyserAnalytics = AppsgeyserSDK.getAnalytics();
        if (appsgeyserAnalytics != null) {
            appsgeyserAnalytics.ActivityStarted();
        }
        DeviceIdParser parser = DeviceIdParser.getInstance();
        parser.rescan(getApplicationContext(), this);
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
                                mPreferenceManager.setSearchEngineApplied(true);
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
    }

    private void loadConfig() {
        config = new Config(getApplicationContext());
    }

    @Override
    public void onDeviceIdParametersObtained(DeviceIdParameters deviceIdParameters) {
        if(!deviceIdParameters.getAdvid().isEmpty()) {
            mPreferenceManager.setAdvertisingId(deviceIdParameters.getAdvid());
        }
    }
}

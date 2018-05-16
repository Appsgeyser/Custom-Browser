package acr.browser.lightning.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.anthonycr.bonsai.Completable;
import com.anthonycr.bonsai.CompletableAction;
import com.anthonycr.bonsai.CompletableSubscriber;
import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.configuration.Constants;

import acr.browser.lightning.R;
import acr.browser.lightning.app.BrowserApp;

@SuppressWarnings("deprecation")
public class MainActivity extends BrowserActivity {

    public static final int SEARCH_BAR_NOTIFICATION_ID = 8242017;
    public boolean firstLaunch = true;

    @NonNull
    @Override
    public Completable updateCookiePreference() {
        return Completable.create(new CompletableAction() {
            @Override
            public void onSubscribe(@NonNull CompletableSubscriber subscriber) {
                CookieManager cookieManager = CookieManager.getInstance();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.createInstance(MainActivity.this);
                }
                cookieManager.setAcceptCookie(mPreferences.getCookiesEnabled());
                subscriber.onComplete();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppsgeyserSDK.takeOff(this,
                getResources().getString(R.string.widgetID), getString(R.string.app_metrica_on_start_event), getString(R.string.template_version));
        AppsgeyserSDK
                .getFastTrackAdsController()
                .showFullscreen(Constants.BannerLoadTags.ON_TIMEOUT_PASSED);
        super.onCreate(savedInstanceState);

        // ---- search bar code ---
        if (mPreferences.getNotificationSearchBarEnabled()) {
            createNotice();
        }

        if (mPreferences.getNotificationSearchBarEnabled()) {
            try {

            } catch (NullPointerException e) {
                Log.e("MainActivity", e.getMessage() + "\n");
                e.printStackTrace();
            }
        }
        // ---- end search bar code ---
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        AppsgeyserSDK.isAboutDialogEnabled(this, new AppsgeyserSDK.OnAboutDialogEnableListener() {
            @Override
            public void onDialogEnableReceived(boolean enabled) {
                if (!enabled) {
                    menu.removeItem(R.id.main_menu_about_dialog);
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("focus", false)) {
            mSearch.requestFocusFromTouch();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mSearch, InputMethodManager.SHOW_IMPLICIT);
        }
        if (isPanicTrigger(intent)) {
            panicClean();
        } else {
            handleNewIntent(intent);
            super.onNewIntent(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveOpenTabs();
        AppsgeyserSDK.onPause(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppsgeyserSDK.onResume(this);
    }

    @Override
    public void updateHistory(@Nullable String title, @NonNull String url) {
        addItemToHistory(title, url);
    }

    @Override
    public boolean isIncognito() {
        return false;
    }

    @Override
    public void closeActivity() {
        closeDrawers(new Runnable() {
            @Override
            public void run() {
                performExitCleanUp();
                moveTaskToBack(true);
            }
        });
    }
}

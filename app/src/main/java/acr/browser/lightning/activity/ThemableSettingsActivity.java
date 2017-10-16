package acr.browser.lightning.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import javax.inject.Inject;

import acr.browser.lightning.R;
import acr.browser.lightning.app.BrowserApp;
import acr.browser.lightning.preference.PreferenceManager;

public abstract class ThemableSettingsActivity extends AppCompatPreferenceActivity {

    private int mTheme;

    @Inject PreferenceManager mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BrowserApp.getAppComponent().inject(this);
        mTheme = mPreferences.getUseTheme();

        // set the theme
        if (mTheme == 0 || mTheme == 1) {
            setTheme(R.style.Theme_SettingsTheme);
            this.getWindow().setBackgroundDrawable(new ColorDrawable(BrowserApp.getThemeManager().getBackgroundColor(mTheme)));
        } else if (mTheme == 2) {
            setTheme(R.style.Theme_SettingsTheme_Dark);
            this.getWindow().setBackgroundDrawable(new ColorDrawable(BrowserApp.getThemeManager().getBackgroundColor(mTheme)));
        } else if (mTheme == 3) {
            setTheme(R.style.Theme_SettingsTheme_Black);
            this.getWindow().setBackgroundDrawable(new ColorDrawable(BrowserApp.getThemeManager().getBackgroundColor(mTheme)));
        }
        super.onCreate(savedInstanceState);

        resetPreferences();
    }

    private void resetPreferences() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mPreferences.getUseBlackStatusBar()) {
                getWindow().setStatusBarColor(Color.BLACK);
            } else {
                getWindow().setStatusBarColor(BrowserApp.getThemeManager().getStatusBarColor(mPreferences.getUseTheme()));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetPreferences();
        if (mPreferences.getUseTheme() != mTheme) {
            restart();
        }
    }

    private void restart() {
        recreate();
    }
}

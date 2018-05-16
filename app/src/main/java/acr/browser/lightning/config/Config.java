package acr.browser.lightning.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acr.browser.lightning.database.HistoryItem;
import acr.browser.lightning.preference.PreferenceManager;

/**
 * Created by roma on 27.04.2017.
 */

public class Config {
    public static final String CONFIG_PREFERENCES = "ConfigPref";
    private String newsTabsPosition;
    private String toolbarPosition;
    private String startPageUrl;

    private Map<String, String> iconMap;

    private List<HistoryItem> bookmarksList;
    private String appName;
    private String homepageTabs;
    private String homePageUrl;
    private String weatherWidgetType;
    private int weatherWidgetColor;
    private int bookmarkWidgetColor;
    private int downloadsWidgetColor;
    private int historyWidgetColor;
    private int newsWidgetColor;

    private int weatherWidgetOrderId;
    private int bookmarkWidgetOrderId;
    private int downloadsWidgetOrderId;
    private int historyWidgetOrderId;
    private int newsWidgetOrderId;

    private boolean historyWidgetEnabled;
    private boolean downloadsWidgetEnabled;
    private boolean bookmarkWidgetEnabled;
    private boolean weatherWidgetEnabled;

    private String bookmarkWidgetType;
    private String downloadsWidgetType;
    private String historyWidgetType;
    private String newsWidgetType;

    private Drawable icon;
    private String backgroundUrl;
    private int primaryColor;
    private int primaryDarkColor;
    private int accentColor;
    private Context context;
    private Drawable background;
    private boolean widgetsMargins;
    private boolean searchBarNotificationEnabled;
    private boolean weatherNotificationEnabled;

    private void initIconMap() {
        iconMap = new HashMap<>();
        iconMap.put("https://www.google.ru/", "img/google-search-icon.png");
        iconMap.put("https://www.amazon.com/", "img/amazon.png");
        iconMap.put("https://www.reddit.com/", "img/reddit-icon.png");
        iconMap.put("https://vimeo.com/", "img/vimeo-icon.png");
        iconMap.put("http://coub.com/", "img/coub-icon.png");
        iconMap.put("https://www.facebook.com/", "img/facebook-icon.png");
        iconMap.put("https://twitter.com/", "img/twitter-icon.png");
        iconMap.put("https://pinterest.com/", "img/pinterest-icon.png");
        iconMap.put("http://www.tumblr.com/", "img/tumblr-icon.png");
        iconMap.put("https://www.linkedin.com/", "img/linkedin.png");
        iconMap.put("https://www.instagram.com/", "img/instagram-icon.png");
    }

    public Config(Context context, PreferenceManager mPreferenceManager) {
        this.context = context;

        initIconMap();
        requestAdsSettings();
        try {
            JSONObject settings = new JSONObject(loadSettings(context));
            appName = settings.getString("name");
            bookmarksList = new ArrayList<>();
            /*String iconUrl = settings.getString("icon");
            if (iconUrl != null && !iconUrl.equals("")) {
                Bitmap b = BitmapFactory.decodeStream(context.getAssets().open(iconUrl));
                b.setDensity(Bitmap.DENSITY_NONE);
                icon = new BitmapDrawable(context.getResources(), b);
            }*/


            JSONObject viewSettings = settings.getJSONObject("viewSettings");
            mPreferenceManager.setFullScreenEnabled(viewSettings.getBoolean("fullScreenMode"));
            mPreferenceManager.setHideStatusBarEnabled(viewSettings.getBoolean("hidedStatusBar"));
            mPreferenceManager.setBookmarkAndTabsSwapped(viewSettings.getBoolean("swapDrawers"));
            mPreferenceManager.setShowTabsInDrawer(viewSettings.getBoolean("tabsInNavigationDrawer"));
            mPreferenceManager.setToolBarStyle(viewSettings.getString("toolBarStyle").isEmpty() ? "default" : viewSettings.getString("toolBarStyle"));
            mPreferenceManager.setNotificationWeatherEnabled(viewSettings.getBoolean("weatherNotificationEnabled"));
            mPreferenceManager.setNotificationSearchEnabled(viewSettings.getBoolean("searchBarNotificationEnabled"));
            toolbarPosition = ifEmpty(viewSettings.getString("toolbarPosition"), "top");

            backgroundUrl = settings.getString("backgroundImage");

            JSONObject homepageTabsObject = settings.getJSONObject("homepageTabs");

            JSONObject widgets = homepageTabsObject.getJSONObject("widgets");

            homepageTabs = ifEmpty(homepageTabsObject.getString("homepageTabs"), "widgets");
            widgetsMargins = homepageTabsObject.getBoolean("widgetsMargins");

            if (homepageTabs.equals("custom")) {
                homePageUrl = homepageTabsObject.getString("homePageUrl");
            } else if (homepageTabs.equals("website")) {
                startPageUrl = homepageTabsObject.getString("startPageUrl");
            }

            if (startPageUrl != null && !startPageUrl.equals("") && !startPageUrl.isEmpty()) {
                mPreferenceManager.setHomepage(startPageUrl);
            }

            JSONObject weatherWidget = widgets.getJSONObject("weatherWidget");
            weatherWidgetOrderId = weatherWidget.getInt("sortPosition");
            weatherWidgetEnabled = weatherWidget.getBoolean("weatherWidgetEnabled");
            weatherWidgetType = ifEmpty(weatherWidget.getString("weatherWidgetType"), "weatherFlat");
            weatherWidgetColor = readColor(weatherWidget, "weatherWidgetColor");


            JSONObject bookmarkWidget = widgets.getJSONObject("bookmarkWidget");
            bookmarkWidgetOrderId = bookmarkWidget.getInt("sortPosition");
            bookmarkWidgetEnabled = bookmarkWidget.getBoolean("bookmarkWidgetEnabled");
            bookmarkWidgetType = ifEmpty(bookmarkWidget.getString("bookmarkWidgetType"), "bookmarkGrid");
            bookmarkWidgetColor = readColor(bookmarkWidget, "bookmarkWidgetColor");


            JSONObject downloadsWidget = widgets.getJSONObject("downloadsWidget");
            downloadsWidgetOrderId = downloadsWidget.getInt("sortPosition");
            downloadsWidgetEnabled = downloadsWidget.getBoolean("downloadsWidgetEnabled");
            downloadsWidgetType = ifEmpty(downloadsWidget.getString("downloadsWidgetType"), "downloadsGrid");
            downloadsWidgetColor = readColor(downloadsWidget, "downloadsWidgetColor");


            JSONObject historyWidget = widgets.getJSONObject("historyWidget");
            historyWidgetOrderId = historyWidget.getInt("sortPosition");
            historyWidgetEnabled = historyWidget.getBoolean("historyWidgetEnabled");
            historyWidgetType = ifEmpty(historyWidget.getString("historyWidgetType"), "historyList");
            historyWidgetColor = readColor(historyWidget, "historyWidgetColor");


            JSONObject newsWidget = widgets.getJSONObject("newsWidget");
            newsWidgetOrderId = newsWidget.getInt("sortPosition");
            newsWidgetType = ifEmpty(newsWidget.getString("newsWidgetType"), "newsList");
            if (!newsWidget.getString("newsTabsPosition").equals("widget")) {
                newsTabsPosition = viewSettings.getString("toolbarPosition");
            } else {
                newsTabsPosition = ifEmpty(newsWidget.getString("newsTabsPosition"), "top");
            }

            newsWidgetColor = readColor(newsWidget, "newsWidgetColor");


            background = createDrawable(backgroundUrl);
            bookmarksList = readBookmarksList(settings.getJSONArray("browserLinks"));
            JSONObject theme = settings.getJSONObject("themeColors");
            primaryColor = readColor(theme, "colorPrimary");
            primaryDarkColor = readColor(theme, "colorPrimaryDark");
            accentColor = readColor(theme, "colorAccent");

        } catch (JSONException e) {
            Log.e("Config", "Json parse error: " + e.getMessage());
        } catch (IOException e) {
            Log.e("Config", "Json read error: " + e.getMessage());
        }
    }

    //------------------------------------------------------

    private Drawable createDrawable(String link) {
        if (!link.equals("")) {
            Bitmap b = null;
            try {
                b = BitmapFactory.decodeStream(context.getAssets().open(link));
                b.setDensity(Bitmap.DENSITY_NONE);
                return new BitmapDrawable(context.getResources(), b);
            } catch (FileNotFoundException e) {
                Log.d("Config", "Image " + link + " not found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private List<HistoryItem> readBookmarksList(JSONArray jsonBookmarks) throws JSONException {
        List<HistoryItem> historyItemList = new ArrayList<>();
        for (int i = 0; i < jsonBookmarks.length(); i++) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.setUrl(jsonBookmarks.getJSONObject(i).getString("url"));
            historyItem.setTitle(jsonBookmarks.getJSONObject(i).getString("title"));
            historyItem.setImageUrl(jsonBookmarks.getJSONObject(i).getString("icon"));
            /*if(historyItem.getImageUrl().equals("") && iconMap.containsKey(historyItem.getUrl())){
                historyItem.setImageUrl(iconMap.get(historyItem.getUrl()));
            }*/
            historyItem.setShowOnMainScreen(true);
            historyItemList.add(historyItem);
        }
        return historyItemList;
    }

    private Integer readColor(JSONObject jsonTheme, String name) throws JSONException {
        String color = jsonTheme.getString(name);
        if (color == null || color.equals("")) {
            return null;
        }
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        return Color.parseColor(color);
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public String loadSettings(Context context) throws IOException {
        String json = null;
        try {
            InputStream is = context.getAssets().open("settings.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private String ifEmpty(String item, String defaultValue) {
        return item.isEmpty() ? defaultValue : item;
    }
    //------------------------------------------------------

    public Drawable getBackground() {
        return background;
    }

    public Integer getPrimaryDarkColor() {
        return primaryDarkColor;
    }

    public Integer getAccentColor() {
        return accentColor;
    }

    public Integer getPrimaryColor() {
        return primaryColor;
    }

    public String getHomePageUrl() {
        return homePageUrl == null ? "" : homePageUrl;
    }

    public String getAppName() {
        return appName;
    }

    public List<HistoryItem> getBookmarksList() {
        return bookmarksList;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getWeatherWidgetType() {
        return weatherWidgetType;
    }

    public int getWeatherWidgetColor() {
        return weatherWidgetColor;
    }

    public String getDownloadsWidgetType() {
        return downloadsWidgetType;
    }

    public String getBookmarkWidgetType() {
        return bookmarkWidgetType;
    }

    public String getHistoryWidgetType() {
        return historyWidgetType;
    }

    public boolean isWidgetsMargins() {
        return widgetsMargins;
    }

    private void requestAdsSettings() {

    }

    public int getWeatherWidgetOrderId() {
        return weatherWidgetOrderId;
    }

    public int getBookmarkWidgetOrderId() {
        return bookmarkWidgetOrderId;
    }

    public int getDownloadsWidgetOrderId() {
        return downloadsWidgetOrderId;
    }

    public boolean isHistoryWidgetEnabled() {
        return historyWidgetEnabled;
    }

    public boolean isDownloadsWidgetEnabled() {
        return downloadsWidgetEnabled;
    }

    public boolean isBookmarkWidgetEnabled() {
        return bookmarkWidgetEnabled;
    }

    public boolean isWeatherWidgetEnabled() {
        return weatherWidgetEnabled;
    }

    public int getHistoryWidgetOrderId() {
        return historyWidgetOrderId;
    }

    public int getBookmarkWidgetColor() {
        return bookmarkWidgetColor;
    }

    public int getDownloadsWidgetColor() {
        return downloadsWidgetColor;
    }

    public int getHistoryWidgetColor() {
        return historyWidgetColor;
    }

    public boolean isSearchBarNotificationEnabled() {
        return searchBarNotificationEnabled;
    }

    public void setSearchBarNotificationEnabled(boolean searchBarNotificationEnabled) {
        this.searchBarNotificationEnabled = searchBarNotificationEnabled;
    }

    public boolean isWeatherNotificationEnabled() {
        return weatherNotificationEnabled;
    }

    public void setWeatherNotificationEnabled(boolean weatherNotificationEnabled) {
        this.weatherNotificationEnabled = weatherNotificationEnabled;
    }

    public String getToolbarPosition() {
        return toolbarPosition;
    }

    public String getNewsTabsPosition() {
        return newsTabsPosition;
    }

    public int getNewsWidgetColor() {
        return newsWidgetColor;
    }

    public int getNewsWidgetOrderId() {
        return newsWidgetOrderId;
    }

    public String getNewsWidgetType() {
        return newsWidgetType;
    }
}

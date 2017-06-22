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

/**
 * Created by roma on 27.04.2017.
 */

public class Config {
    public static final String CONFIG_PREFERENCES = "ConfigPref";

    private Map<String, String> iconMap;

    private List<HistoryItem> bookmarksList;
    private String appName;
    private Drawable icon;
    private String backgroundUrl;
    private Integer primaryColor;
    private Integer primaryDarkColor;
    private Integer accentColor;
    private Context context;
    private Drawable background;

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

    public Config(Context context) {
        this.context = context;

        initIconMap();

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
            backgroundUrl = settings.getString("backgroundImage");
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
            if(historyItem.getImageUrl().equals("") && iconMap.containsKey(historyItem.getUrl())){
                historyItem.setImageUrl(iconMap.get(historyItem.getUrl()));
            }
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

    public String getAppName() {
        return appName;
    }

    public List<HistoryItem> getBookmarksList() {
        return bookmarksList;
    }

    public Drawable getIcon() {
        return icon;
    }
}

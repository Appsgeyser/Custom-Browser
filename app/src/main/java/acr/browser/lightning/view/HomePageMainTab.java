package acr.browser.lightning.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonycr.bonsai.CompletableOnSubscribe;
import com.anthonycr.bonsai.Schedulers;
import com.anthonycr.bonsai.SingleOnSubscribe;
import com.appsgeyser.sdk.AppsgeyserSDK;
import com.appsgeyser.sdk.ads.rewardedVideo.rewardedFacades.RewardedVideoFacade;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import acr.browser.lightning.R;
import acr.browser.lightning.activity.BrowserActivity;
import acr.browser.lightning.activity.ThemableBrowserActivity;
import acr.browser.lightning.adapter.HistoryAdapter;
import acr.browser.lightning.app.BrowserApp;
import acr.browser.lightning.config.Config;
import acr.browser.lightning.controller.UIController;
import acr.browser.lightning.database.HistoryItem;
import acr.browser.lightning.database.bookmark.BookmarkModel;
import acr.browser.lightning.database.downloads.DownloadItem;
import acr.browser.lightning.database.downloads.DownloadsModel;
import acr.browser.lightning.database.history.HistoryModel;
import acr.browser.lightning.domain.GeoData;
import acr.browser.lightning.domain.NewsCategory;
import acr.browser.lightning.domain.WeatherData;
import acr.browser.lightning.notifiction.WeatherNotification;
import acr.browser.lightning.preference.PreferenceManager;
import acr.browser.lightning.utils.BookmarkTouchHelper;
import acr.browser.lightning.utils.ColorPicker;
import acr.browser.lightning.utils.HomePageWidget;
import acr.browser.lightning.utils.ImageLoader;
import acr.browser.lightning.utils.ItemClickSupport;
import acr.browser.lightning.utils.NewsApi;
import acr.browser.lightning.utils.StartPageLoader;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roma on 17.06.2017.
 */

public class HomePageMainTab {

    private static final long WEATHER_UPDATE_PERIOD = 20 * 60 * 1000;
    @BindView(R.id.widgetsPanel)
    LinearLayout widgetsPanel;

    private View view;
    private WeatherData weatherData;
    private Activity activity;

    public HomePageMainTab(final Activity activity, final int theme, BookmarkModel bookmarkModel, DownloadsModel downloadsModel) {
        view = activity.getLayoutInflater().inflate(R.layout.home_page_main, null);
        ButterKnife.bind(this, view);
        this.activity = activity;
        final PreferenceManager preferenceManager = ((ThemableBrowserActivity) activity).getmPreferences();
        Config config = BrowserApp.getConfig();

        List<HomePageWidget> homePageWidgetList = new ArrayList<>();


        if (config.isBookmarkWidgetEnabled()) {
            final BookmarkWidget bookmarkWidget = new BookmarkWidget(activity, BrowserApp.getConfig().getBookmarkWidgetType());
            final BookmarkAdapter bookmarkAdapter = new BookmarkAdapter(activity, theme, bookmarkModel);
            bookmarkModel.getBookmarksForMainScreen()
                    .subscribeOn(Schedulers.main())
                    .subscribe(new SingleOnSubscribe<List<HistoryItem>>() {
                        @Override
                        public void onItem(@Nullable List<HistoryItem> item) {
                            if (item != null) {
                                bookmarkAdapter.setBookmarks(item);
                                bookmarkAdapter.notifyDataSetChanged();
                                ViewGroup.LayoutParams params = bookmarkWidget.bookmarksGrid.getLayoutParams();
                                params.height = calculateHeight(BrowserApp.getConfig().getBookmarkWidgetType(), item.size());
                                bookmarkWidget.bookmarksGrid.setLayoutParams(params);
                            }
                        }
                    });
            bookmarkWidget.bookmarksGrid.setLayoutManager(new GridLayoutManager(activity, calculateNoOfColumns(BrowserApp.getConfig().getBookmarkWidgetType(), activity)) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            bookmarkWidget.bookmarksGrid.setAdapter(bookmarkAdapter);
            BookmarkTouchHelper bookmarkTouchHelper = new BookmarkTouchHelper(bookmarkAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(bookmarkTouchHelper);
            touchHelper.attachToRecyclerView(bookmarkWidget.bookmarksGrid);
            ItemClickSupport.addTo(bookmarkWidget.bookmarksGrid).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    ((UIController) activity).loadUrl(bookmarkAdapter.getHistoryItemList().get(position).getUrl());
                }
            });
            homePageWidgetList.add(bookmarkWidget);
        }
        if (config.isDownloadsWidgetEnabled()) {
            final DownloadsWidget downloadsWidget = new DownloadsWidget(activity, BrowserApp.getConfig().getDownloadsWidgetType());

            downloadsWidget.downloadsCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((BrowserActivity) activity).openDownloads();
                }
            });
            downloadsWidget.title.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            final DownloadsAdapter downloadsAdapter = new DownloadsAdapter(activity, theme);
            downloadsModel.getAllDownloads()
                    .subscribeOn(Schedulers.main())
                    .subscribe(new SingleOnSubscribe<List<DownloadItem>>() {
                        @Override
                        public void onItem(@Nullable List<DownloadItem> item) {

                            if (item != null && item.size() > 0) {
                                Collections.reverse(item);
                                if (item.size() > 10) {
                                    item = item.subList(0, 10);
                                }
                                downloadsAdapter.setBookmarks(item);
                                downloadsAdapter.notifyDataSetChanged();
                                ViewGroup.LayoutParams params = downloadsWidget.downloadsGrid.getLayoutParams();
                                params.height = calculateHeight(BrowserApp.getConfig().getDownloadsWidgetType(), item.size());
                                downloadsWidget.downloadsGrid.setLayoutParams(params);
                                downloadsWidget.getView().setVisibility(View.VISIBLE);
                            } else {
                                downloadsWidget.getView().setVisibility(View.GONE);
                            }
                        }
                    });
            downloadsWidget.downloadsGrid.setLayoutManager(new GridLayoutManager(activity, calculateNoOfColumns(BrowserApp.getConfig().getDownloadsWidgetType(), activity)) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            downloadsWidget.downloadsGrid.setAdapter(downloadsAdapter);
            ItemClickSupport.addTo(downloadsWidget.downloadsGrid).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    Intent newIntent = new Intent(Intent.ACTION_VIEW);
                    StringBuilder builder = new StringBuilder();
                    builder.append("file://");
                    builder.append(preferenceManager.getDownloadDirectory());
                    builder.append("/");
                    builder.append(downloadsAdapter.getDownloadItemList().get(position).getTitle());
                    String mimeType = myMime.getMimeTypeFromExtension(fileExt(builder.toString()));
                    File temp_file = new File(preferenceManager.getDownloadDirectory() + "/" + downloadsAdapter.getDownloadItemList().get(position).getTitle());
                    newIntent.setDataAndType(Uri.fromFile(temp_file), mimeType);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        activity.startActivity(newIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(activity, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            homePageWidgetList.add(downloadsWidget);

        }
        if (config.isHistoryWidgetEnabled()) {
            final HistoryWidget historyWidget = new HistoryWidget(activity, BrowserApp.getConfig().getHistoryWidgetType(), theme);
            historyWidget.historyCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((BrowserActivity) activity).openHistory();
                }
            });
            historyWidget.title.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            final HistoryAdapter historyAdapter = new HistoryAdapter(activity, theme);
            HistoryModel.lastHundredVisitedHistoryItems()
                    .subscribeOn(Schedulers.main())
                    .subscribe(new SingleOnSubscribe<List<HistoryItem>>() {
                        @Override
                        public void onItem(@Nullable List<HistoryItem> item) {

                            if (item != null && item.size() > 0) {
                                if (item.size() > 10) {
                                    item = item.subList(0, 10);
                                }
                                historyAdapter.setBookmarks(item);
                                historyAdapter.notifyDataSetChanged();
                                ViewGroup.LayoutParams params = historyWidget.historyGrid.getLayoutParams();
                                params.height = calculateHeight(BrowserApp.getConfig().getDownloadsWidgetType(), item.size());
                                historyWidget.historyGrid.setLayoutParams(params);
                                historyWidget.getView().setVisibility(View.VISIBLE);
                            } else {
                                historyWidget.getView().setVisibility(View.GONE);
                            }
                        }
                    });
            historyWidget.historyGrid.setLayoutManager(new GridLayoutManager(activity, calculateNoOfColumns(BrowserApp.getConfig().getDownloadsWidgetType(), activity)) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            historyWidget.historyGrid.setAdapter(historyAdapter);
            ItemClickSupport.addTo(historyWidget.historyGrid).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    ((UIController) activity).loadUrl(historyAdapter.getHistoryItemList().get(position).getUrl());
                }
            });
            homePageWidgetList.add(historyWidget);

        }
        if (config.isWeatherWidgetEnabled()) {
            final WeatherWidget weatherWidget = new WeatherWidget(activity, BrowserApp.getConfig().getWeatherWidgetType(), theme);
            boolean celsius = preferenceManager.getWeatherData().isCecius();

            weatherWidget.celsiusButton.setText(celsius ? "C" : "F");

            weatherWidget.celsiusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WeatherData datas = preferenceManager.getWeatherData();
                    boolean celsius = !datas.isCecius();
                    weatherWidget.celsiusButton.setText(celsius ? "C" : "F");
                    datas.setCecius(celsius);
                    preferenceManager.setWeatherDataData(datas);
                    if (preferenceManager.getNotificationWeatherEnabled()) {
                        WeatherNotification weatherNotification = new WeatherNotification(activity, datas, preferenceManager);
                        weatherNotification.remove();
                        weatherNotification.show();
                    }
                    if (weatherData != null) {
                        if (celsius) {
                            int temperatureValue = (int) ((weatherData.getTemp() - 32) * (5. / 9.));
                            weatherWidget.temperature.setText(String.valueOf(temperatureValue) + "°");
                        } else {
                            weatherWidget.temperature.setText(String.valueOf(weatherData.getTemp()) + "°");
                        }
                    }

                }
            });

            final StartPageLoader.WeatherCallback weatherCallback = new StartPageLoader.WeatherCallback() {
                @Override
                public void onWeatherResult(final WeatherData weatherData) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (weatherData != null) {
                                weatherWidget.temperaturePanel.setVisibility(View.VISIBLE);
                                weatherWidget.imagePanel.setVisibility(View.VISIBLE);
                                HomePageMainTab.this.weatherData = weatherData;
                                boolean celsius = weatherData.isCecius();
                                weatherWidget.celsiusButton.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
                                int temperatureValue = (int) (celsius ? (weatherData.getTemp() - 32) * (5. / 9.) : weatherData.getTemp());
                                weatherWidget.temperature.setText(String.valueOf(temperatureValue) + "°");

                                Drawable drawable = activity.getResources().getDrawable(StartPageLoader.getIconId(BrowserApp.getConfig().getWeatherWidgetType(), weatherData.getCode()));
                                if (BrowserApp.getConfig().getWeatherWidgetType().equals(WeatherWidget.SIMPLE)) {
                                    drawable = DrawableCompat.wrap(drawable);
                                    DrawableCompat.setTint(drawable, BrowserApp.getThemeManager().getIconColor(theme));
                                }

                                weatherWidget.weatherIcon.setImageDrawable(drawable);

                                weatherWidget.location.setText(weatherData.getLocation());
                                weatherWidget.weatherText.setText(weatherData.getText());
                                if (preferenceManager.getNotificationWeatherEnabled()) {
                                    WeatherNotification weatherNotification = new WeatherNotification(activity.getBaseContext(), weatherData, preferenceManager);
                                    weatherNotification.show();
                                }
                            } else {
                                weatherWidget.temperaturePanel.setVisibility(View.GONE);
                                weatherWidget.imagePanel.setVisibility(View.GONE);
                                weatherWidget.location.setText(activity.getResources().getString(R.string.noWeatherData));
                            }
                            ((UIController) activity).updateProgress(100);
                        }
                    });
                }
            };
            StartPageLoader.getWeather(activity, preferenceManager, weatherCallback, false);
            weatherWidget.location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                    final EditText edittext = new EditText(activity);
                    FrameLayout container = new FrameLayout(activity);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(16, 0, 16, 0);
                    edittext.setLayoutParams(params);
                    container.addView(edittext);

                    alert.setTitle(activity.getResources().getString(R.string.enterCity));
                    alert.setView(container);

                    alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            preferenceManager.setCity(edittext.getText().toString());
                            StartPageLoader.getWeather(activity, preferenceManager, weatherCallback, true);
                        }
                    });

                    alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // what ever you want to do with No option.
                        }
                    });

                    alert.setNeutralButton(R.string.detectLocation, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            preferenceManager.removeCity();
                            StartPageLoader.getWeather(activity, preferenceManager, weatherCallback, true);
                            // what ever you want to do with No option.
                        }
                    });

                    alert.show();
                }
            });
            homePageWidgetList.add(weatherWidget);

        }
        if (config.getNewsTabsPosition().equals(BrowserActivity.TAB_POSITION_WIDGET)) {
            final NewsWidget newsWidget = new NewsWidget(activity, config.getNewsWidgetType());
            final HomepageView.NewsAdapter newsAdapter = new HomepageView.NewsAdapter(activity, theme,
                    config.getNewsWidgetType().equals(NewsWidget.LIST_REVERSED) || config.getNewsWidgetType().equals(NewsWidget.LIST_TRANSPARENT_REVERSED));
            newsWidget.newsList.setLayoutManager(new GridLayoutManager(activity, calculateNoOfColumns(BrowserApp.getConfig().getNewsWidgetType(), activity)) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            newsWidget.newsList.setAdapter(newsAdapter);
            newsWidget.newsList.addItemDecoration(new DividerItemDecoration(activity, R.drawable.divider));
            newsWidget.newsList.setHasFixedSize(true);
            newsWidget.newsList.setItemViewCacheSize(20);
            newsWidget.newsList.setDrawingCacheEnabled(true);
            newsWidget.newsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

            ItemClickSupport.addTo(newsWidget.newsList).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView newsList, int position, View v) {
                    ((UIController) activity).loadUrl(newsAdapter.getNewsList().get(position).getLink());
                }
            });


            final String query = "wid=" + activity.getResources().getString(R.string.widgetID) + "&"
                    + "advid=" + preferenceManager.getAdvertisingId() + "$"
                    + "pn=" + activity.getPackageName() + "&" + "v=" + activity.getResources().getString(R.string.platformVersion)
                    + "apiKey=" + activity.getResources().getString(R.string.api_key);

            StartPageLoader.requestGeoData(preferenceManager, new StartPageLoader.GeoDataHandler() {
                        @Override
                        public void onResult(GeoData geoData) {

                            NewsApi.getInstance().getTopStoriesNews(query, geoData.getCountryCode(), new NewsApi.NewsCallback() {
                                @Override
                                public void onNewsResult(final NewsCategory newsCategory) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            newsAdapter.setNews(newsCategory.getNewsList());
                                            newsAdapter.notifyDataSetChanged();
                                            int dps = Double.valueOf(newsAdapter.getNewsList().size()).intValue() * 100;

                                            ViewGroup.LayoutParams params = newsWidget.newsList.getLayoutParams();
                                            params.height = calculateHeight(BrowserApp.getConfig().getNewsWidgetType(), newsAdapter.getNewsList().size());
                                            newsWidget.newsList.setLayoutParams(params);

                                           /* newsCategoryList.add(newsCategory);
                                            tabNamesList.add(newsCategory.getName());
                                            tabLayout.addTab(tabLayout.newTab().setText(newsCategory.getName()));
                                            newsPageAdapter.notifyDataSetChanged();*/

                                        }
                                    });
                                }
                            });
                        }
                    }
            );
            homePageWidgetList.add(newsWidget);

        }

        Collections.sort(homePageWidgetList);
        for (HomePageWidget homePageWidget : homePageWidgetList) {
            widgetsPanel.addView(homePageWidget.getView());
            if (BrowserApp.getConfig().isWidgetsMargins()) {
                homePageWidget.setMargins(getPx(8), getPx(4));
            } else {
                homePageWidget.setMargins(0, 0);
            }
        }
    }



    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    private int calculateHeight(String widgetType, int size) {
        int elementHeight = 0;
        switch (widgetType) {
            case NewsWidget.LIST:
            case NewsWidget.LIST_TRANSPARENT:
            case NewsWidget.LIST_REVERSED:
            case NewsWidget.LIST_TRANSPARENT_REVERSED:
                elementHeight = 100;
                break;
            case BookmarkWidget.BIG_GRID:
            case BookmarkWidget.GRID:
                elementHeight = 120;
                break;
            case BookmarkWidget.LIST:
            case BookmarkWidget.LIST_2_COLUMNS:
            case DownloadsWidget.GRID:
            case DownloadsWidget.GRID_TRANSPARENT:
            case DownloadsWidget.LIST:
            case DownloadsWidget.LIST_TRANSPARENT:
                elementHeight = 56;
                break;
        }

        final float scale = activity.getResources().getDisplayMetrics().density;
        int dps = Double.valueOf(Math.ceil(size / (double) calculateNoOfColumns(widgetType, activity))).intValue() * elementHeight;
        return (int) (dps * scale + 0.5f);
    }

    private int getPx(int dp) {
        Resources r = activity.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static int calculateNoOfColumns(String widgetType, Context context) {
        switch (widgetType) {
            case BookmarkWidget.LIST:
            case DownloadsWidget.LIST:
            case DownloadsWidget.LIST_TRANSPARENT:
            case NewsWidget.LIST:
            case NewsWidget.LIST_TRANSPARENT:
            case NewsWidget.LIST_REVERSED:
            case NewsWidget.LIST_TRANSPARENT_REVERSED:
                return 1;
            case BookmarkWidget.BIG_GRID:
            case BookmarkWidget.LIST_2_COLUMNS:
            case DownloadsWidget.GRID:
            case DownloadsWidget.GRID_TRANSPARENT:
                return 2;
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 120);
    }

    public View getView() {
        return view;
    }

    public interface ItemTouchHelperAdapter {

        boolean onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }

    public static class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder>
            implements ItemTouchHelperAdapter {

        private final List<HistoryItem> historyItemList;
        BookmarkModel bookmarkModel;
        private Context context;
        private int theme;

        public BookmarkAdapter(Context context, int theme, BookmarkModel bookmarkModel) {
            this.historyItemList = new ArrayList<>();
            this.context = context;
            this.theme = theme;
            this.bookmarkModel = bookmarkModel;
        }

        public void setBookmarks(List<HistoryItem> bookmarkList) {
            historyItemList.clear();
            historyItemList.addAll(bookmarkList);
            for (int i = 0; i < historyItemList.size(); i++) {
                historyItemList.get(i).setId(i);
            }
        }

        @Override
        public void onItemDismiss(int position) {
            historyItemList.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(historyItemList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(historyItemList, i, i - 1);
                }
            }
            for (int i = 0; i < historyItemList.size(); i++) {
                HistoryItem editedItem = new HistoryItem();
                HistoryItem old = historyItemList.get(i);
                editedItem.setTitle(old.getTitle());
                editedItem.setImageUrl(old.getImageUrl());
                editedItem.setShowOnMainScreen(old.isShowOnMainScreen());
                editedItem.setUrl(old.getUrl());
                editedItem.setPosition(i);
                editedItem.setFolder(old.getFolder());
                bookmarkModel.editBookmark(old, editedItem).subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.main())
                        .subscribe(new CompletableOnSubscribe() {
                            @Override
                            public void onComplete() {

                            }
                        });
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        public List<HistoryItem> getHistoryItemList() {
            return historyItemList;
        }
        //------------------------------------------------------

        @Override
        public BookmarkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            switch (BrowserApp.getConfig().getBookmarkWidgetType()) {
                case BookmarkWidget.GRID:
                    return new ViewHolder(inflater.inflate(R.layout.bookmark_grid_item, parent, false));
                case BookmarkWidget.BIG_GRID:
                    return new ViewHolder(inflater.inflate(R.layout.bookmark_big_grid_item, parent, false));
                case BookmarkWidget.LIST:
                case BookmarkWidget.LIST_2_COLUMNS:
                    return new ViewHolder(inflater.inflate(R.layout.bookmark_list_item, parent, false));
                default:
                    return new ViewHolder(inflater.inflate(R.layout.bookmark_grid_item, parent, false));

            }
        }

        @Override
        public void onBindViewHolder(final BookmarkAdapter.ViewHolder holder, int position) {
            HistoryItem web = historyItemList.get(position);
            holder.nameBook.setText(web.getTitle());
            holder.nameBook.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            holder.letter.setVisibility(View.INVISIBLE);

            if (web.isFolder()) {

            } else if (web.getImageUrl() != null && !web.getImageUrl().equals("")) {
                if (web.getImageUrl().startsWith("http")) {
                    createIcon(holder, web);
                    ImageLoader.getInstance().loadImage(web.getImageUrl(), new ImageLoader.ImageLoadedListener() {
                        @Override
                        public void onImageLoaded(Bitmap b) {
                            holder.favicon.setImageBitmap(b);
                            holder.letter.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    Bitmap b = null;
                    try {
                        b = BitmapFactory.decodeStream(context.getAssets().open(web.getImageUrl()));
                        b.setDensity(Bitmap.DENSITY_NONE);
                        BitmapDrawable icon = new BitmapDrawable(context.getResources(), b);
                        holder.favicon.setImageDrawable(icon);
                    } catch (IOException e) {
                        Log.w("favicon", "Can't get icon");
                    }
                }
            } else if (web.getBitmap() == null) {
                if (!BrowserApp.getConfig().getBookmarkWidgetType().equals(BookmarkWidget.LIST) &&
                        !BrowserApp.getConfig().getBookmarkWidgetType().equals(BookmarkWidget.LIST_2_COLUMNS)) {
                    createIcon(holder, web);
                } else {
                    holder.favicon.setImageBitmap(BrowserApp.getThemeManager().getThemedBitmap(context, R.drawable.bookmark_outline, theme));
                }
            } else {
                holder.favicon.setImageBitmap(web.getBitmap());
            }
            if (BrowserApp.getConfig().getBookmarkWidgetType().equals(BookmarkWidget.BIG_GRID)) {
                holder.background.setBackgroundColor(ColorPicker.getColor(context.getString(R.string.widgetID).hashCode() + position));
            }
        }

        private void createIcon(ViewHolder holder, HistoryItem historyItem) {
            Resources res = context.getResources();
            TypedArray icons = res.obtainTypedArray(R.array.background);
            if (BrowserApp.getConfig().getBookmarkWidgetType().equals(BookmarkWidget.GRID)) {
                Drawable drawable = icons.getDrawable(Math.abs(historyItem.hashCode() % icons.length() - 1));
                holder.favicon.setImageDrawable(drawable);
            } else if (BrowserApp.getConfig().getBookmarkWidgetType().equals(BookmarkWidget.BIG_GRID)) {
                holder.favicon.setVisibility(View.INVISIBLE);
            }
            holder.letter.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse(historyItem.getUrl());
            if (uri != null) {
                holder.letter.setText((uri.getHost().startsWith("www.") ? uri.getHost().substring(4) : uri.getHost()).substring(0, 1).toUpperCase());
            } else {
                holder.letter.setText(historyItem.getTitle().substring(0, 1).toUpperCase());
            }
            icons.recycle();
        }

        @Override
        public int getItemCount() {
            return historyItemList.size();
        }

        @Override
        public long getItemId(int position) {
            return historyItemList.get(position).getId();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.textBookmark)
            TextView nameBook;
            @BindView(R.id.faviconBookmark)
            ImageView favicon;
            @BindView(R.id.letter)
            TextView letter;
            @BindView(R.id.background)
            ViewGroup background;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    public static class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {

        private final List<DownloadItem> downloadItemList;
        private Context context;
        private int theme;

        public DownloadsAdapter(Context context, int theme) {
            this.downloadItemList = new ArrayList<>();
            this.context = context;
            this.theme = theme;
        }

        public void setBookmarks(List<DownloadItem> bookmarkList) {
            downloadItemList.clear();
            downloadItemList.addAll(bookmarkList);
        }

        public List<DownloadItem> getDownloadItemList() {
            return downloadItemList;
        }
        //------------------------------------------------------

        @Override
        public DownloadsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            return new ViewHolder(inflater.inflate(R.layout.downloads_list_item, parent, false));

        }

        @Override
        public void onBindViewHolder(final DownloadsAdapter.ViewHolder holder, int position) {
            DownloadItem web = downloadItemList.get(position);
            holder.name.setText(web.getTitle());
            holder.name.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            holder.url.setText(web.getUrl());
            holder.url.setTextColor(BrowserApp.getThemeManager().getDisabledIconColor(theme));
        }


        static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.url)
            TextView url;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        @Override
        public int getItemCount() {
            return downloadItemList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}

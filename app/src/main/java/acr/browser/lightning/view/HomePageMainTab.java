package acr.browser.lightning.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anthonycr.bonsai.CompletableOnSubscribe;
import com.anthonycr.bonsai.Schedulers;
import com.anthonycr.bonsai.SingleOnSubscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import acr.browser.lightning.R;
import acr.browser.lightning.activity.ThemableBrowserActivity;
import acr.browser.lightning.app.BrowserApp;
import acr.browser.lightning.config.Config;
import acr.browser.lightning.controller.UIController;
import acr.browser.lightning.database.HistoryItem;
import acr.browser.lightning.database.bookmark.BookmarkModel;
import acr.browser.lightning.domain.WeatherData;
import acr.browser.lightning.preference.PreferenceManager;
import acr.browser.lightning.utils.BookmarkTouchHelper;
import acr.browser.lightning.utils.ImageLoader;
import acr.browser.lightning.utils.ItemClickSupport;
import acr.browser.lightning.utils.StartPageLoader;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roma on 17.06.2017.
 */

public class HomePageMainTab {

    private final String HOMEPAGE_PREFERENCE = "HomepageView";
    private final String DEGREE_SYSTEM = "isCelsius";

    private static final long WEATHER_UPDATE_PERIOD = 20 * 60 * 1000;


    private View view;
    @BindView(R.id.temperature)
    TextView temperature;
    @BindView(R.id.weather_icon)
    ImageView weatherIcon;
    @BindView(R.id.location)
    TextView location;
    @BindView(R.id.weatherText)
    TextView weatherText;
    @BindView(R.id.weatherWidget)
    CardView weatherWidget;
    @BindView(R.id.celsiusButton)
    TextView celsiusButton;
    @BindView(R.id.fahrenheitButton)
    TextView fahrenheitButton;
    @BindView(R.id.bookmarks_grid)
    RecyclerView bookmarksGrid;
    @BindView(R.id.bookmarksCard)
    CardView bookmarksCard;
    @BindView(R.id.temperaturePanel)
    LinearLayout temperaturePanel;
    @BindView(R.id.imagePanel)
    LinearLayout imagePanel;

    private WeatherData weatherData;

    public HomePageMainTab(final Activity activity, final int theme, BookmarkModel bookmarkModel) {
        view = activity.getLayoutInflater().inflate(R.layout.home_page_main, null);
        ButterKnife.bind(this, view);
        final PreferenceManager preferenceManager = ((ThemableBrowserActivity) activity).getmPreferences();
        Config config = BrowserApp.getConfig();
        weatherWidget.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentPrimaryColor(theme));
        bookmarksCard.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentPrimaryColor(theme));
        final BookmarkAdapter bookmarkAdapter = new BookmarkAdapter(activity, theme, bookmarkModel);
        bookmarkModel.getBookmarksForMainScreen()
                .subscribeOn(Schedulers.main())
                .subscribe(new SingleOnSubscribe<List<HistoryItem>>() {
                    @Override
                    public void onItem(@Nullable List<HistoryItem> item) {
                        if(item != null) {
                            bookmarkAdapter.setBookmarks(item);
                            bookmarkAdapter.notifyDataSetChanged();
                            ViewGroup.LayoutParams params=bookmarksGrid.getLayoutParams();
                            final float scale = activity.getResources().getDisplayMetrics().density;
                            int dps=Double.valueOf(Math.ceil(item.size() / (double)calculateNoOfColumns(activity))).intValue() * 120;;
                            params.height= (int) (dps * scale + 0.5f);
                            bookmarksGrid.setLayoutParams(params);
                        }
                    }
                });

        location.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
        temperature.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
        weatherText.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));

        bookmarksGrid.setLayoutManager(new GridLayoutManager(activity, calculateNoOfColumns(activity)){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        bookmarksGrid.setAdapter(bookmarkAdapter);
        BookmarkTouchHelper bookmarkTouchHelper = new BookmarkTouchHelper(bookmarkAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(bookmarkTouchHelper);
        touchHelper.attachToRecyclerView(bookmarksGrid);
        ItemClickSupport.addTo(bookmarksGrid).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                ((UIController)activity).loadUrl(bookmarkAdapter.getHistoryItemList().get(position).getUrl());
            }
        });
        celsiusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getSharedPreferences(HOMEPAGE_PREFERENCE, Context.MODE_PRIVATE).edit().putBoolean(DEGREE_SYSTEM, true).commit();
                if (weatherData != null) {
                    int temperatureValue = (int) ((weatherData.getTemp() - 32) * (5. / 9.));
                    temperature.setText(String.valueOf(temperatureValue) + "°");
                }
                celsiusButton.setTextColor(BrowserApp.getThemeManager().getIconColor(0));
                fahrenheitButton.setTextColor(BrowserApp.getThemeManager().getDisabledIconColor(0));
            }
        });
        fahrenheitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getSharedPreferences(HOMEPAGE_PREFERENCE, Context.MODE_PRIVATE).edit().putBoolean(DEGREE_SYSTEM, false).commit();
                if (weatherData != null) {
                    temperature.setText(String.valueOf(weatherData.getTemp()) + "°");
                }
                celsiusButton.setTextColor(BrowserApp.getThemeManager().getDisabledIconColor(0));
                fahrenheitButton.setTextColor(BrowserApp.getThemeManager().getIconColor(0));
            }
        });

        final StartPageLoader.WeatherCallback weatherCallback = new StartPageLoader.WeatherCallback() {
            @Override
            public void onWeatherResult(final WeatherData weatherData) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weatherData != null) {
                            temperaturePanel.setVisibility(View.VISIBLE);
                            imagePanel.setVisibility(View.VISIBLE);
                            HomePageMainTab.this.weatherData = weatherData;
                            boolean celsius = activity.getSharedPreferences(HOMEPAGE_PREFERENCE, Context.MODE_PRIVATE).getBoolean(DEGREE_SYSTEM, false);
                            if (celsius) {
                                celsiusButton.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
                                fahrenheitButton.setTextColor(BrowserApp.getThemeManager().getDisabledIconColor(theme));
                            } else {
                                celsiusButton.setTextColor(BrowserApp.getThemeManager().getDisabledIconColor(theme));
                                fahrenheitButton.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
                            }
                            int temperatureValue = (int) (celsius ? (weatherData.getTemp() - 32) * (5. / 9.) : weatherData.getTemp());
                            temperature.setText(String.valueOf(temperatureValue) + "°");

                            Drawable drawable = activity.getResources().getDrawable(StartPageLoader.getIconId(weatherData.getCode()));
                            Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                            DrawableCompat.setTint(wrappedDrawable, BrowserApp.getThemeManager().getIconColor(theme));

                            weatherIcon.setImageDrawable(wrappedDrawable);

                            location.setText(weatherData.getLocation());
                            weatherText.setText(weatherData.getText());
                        } else {
                            temperaturePanel.setVisibility(View.GONE);
                            imagePanel.setVisibility(View.GONE);
                            location.setText(activity.getResources().getString(R.string.noWeatherData));
                        }
                        ((UIController)activity).updateProgress(100);
                    }
                });
            }
        };
        StartPageLoader.getWeather(activity, preferenceManager, weatherCallback, false);
        location.setOnClickListener(new View.OnClickListener() {
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
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 120);
        return noOfColumns;
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
        private Context context;
        private int theme;
        BookmarkModel bookmarkModel;

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

            View contactView = inflater.inflate(R.layout.bookmark_grid_item, parent, false);

            return new ViewHolder(contactView);
        }

        @Override
        public void onBindViewHolder(final BookmarkAdapter.ViewHolder holder, int position) {
            HistoryItem web = historyItemList.get(position);
            holder.nameBook.setText(web.getTitle());
            holder.nameBook.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            holder.favicon.setImageDrawable(context.getResources().getDrawable(R.drawable.app_icon));
            holder.letter.setVisibility(View.INVISIBLE);

            if (web.isFolder()) {

            } else if (web.getImageUrl() != null && !web.getImageUrl().equals("")) {
                if (web.getImageUrl().startsWith("http")) {
                    createIcon(holder, web);
                    ImageLoader.getInstance().loadImage( web.getImageUrl(), new ImageLoader.ImageLoadedListener() {
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
                createIcon(holder, web);
            } else {
                holder.favicon.setImageBitmap(web.getBitmap());
            }
        }

        private void createIcon(ViewHolder holder, HistoryItem historyItem){
            Resources res = context.getResources();
            TypedArray icons = res.obtainTypedArray(R.array.background);
            Drawable drawable = icons.getDrawable(Math.abs(historyItem.hashCode() % icons.length() - 1));
            holder.favicon.setImageDrawable(drawable);
            holder.letter.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse(historyItem.getUrl());
            if (uri != null) {
                holder.letter.setText((uri.getHost().startsWith("www.") ? uri.getHost().substring(4) : uri.getHost()).substring(0, 1).toUpperCase());
            } else {
                holder.letter.setText(historyItem.getTitle().substring(0, 1).toUpperCase());
            }
            icons.recycle();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.textBookmark)
            TextView nameBook;
            @BindView(R.id.faviconBookmark)
            ImageView favicon;
            @BindView(R.id.letter)
            TextView letter;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        @Override
        public int getItemCount() {
            return historyItemList.size();
        }

        @Override
        public long getItemId(int position) {
            return historyItemList.get(position).getId();
        }
    }
}

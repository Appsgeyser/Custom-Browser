package acr.browser.lightning.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import acr.browser.lightning.R;
import acr.browser.lightning.activity.BrowserActivity;
import acr.browser.lightning.app.BrowserApp;
import acr.browser.lightning.config.Config;
import acr.browser.lightning.controller.UIController;
import acr.browser.lightning.database.bookmark.BookmarkModel;
import acr.browser.lightning.database.downloads.DownloadsModel;
import acr.browser.lightning.domain.GeoData;
import acr.browser.lightning.domain.News;
import acr.browser.lightning.domain.NewsCategory;
import acr.browser.lightning.preference.PreferenceManager;
import acr.browser.lightning.utils.ImageLoader;
import acr.browser.lightning.utils.ItemClickSupport;
import acr.browser.lightning.utils.NewsApi;
import acr.browser.lightning.utils.StartPageLoader;
import butterknife.BindView;
import butterknife.ButterKnife;

import static acr.browser.lightning.view.NewsWidget.LIST;
import static acr.browser.lightning.view.NewsWidget.LIST_REVERSED;
import static acr.browser.lightning.view.NewsWidget.LIST_TRANSPARENT;
import static acr.browser.lightning.view.NewsWidget.LIST_TRANSPARENT_REVERSED;

/**
 * Created by roma on 11.06.2017.
 */

public class HomepageView {

    private final NewsPageAdapter newsPageAdapter;
    @BindView(R.id.backgroundImage)
    ImageView backgroundImage;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    private String newsType = "default";
    private View view;
    private UrlClickedListener urlClickedListener;
    private List<NewsCategory> newsCategoryList;
    private List<String> tabNamesList;
    private PreferenceManager preferenceManager;
    private int theme;


    public HomepageView(final FragmentActivity activity, boolean isIncognito, PreferenceManager preferenceManager) {

        if (BrowserApp.getConfig().getNewsTabsPosition().equals(BrowserActivity.TAB_POSITION_BOTTOM)) {
            view = activity.getLayoutInflater().inflate(R.layout.home_page_bottom_tabs, null);
        } else if (BrowserApp.getConfig().getNewsTabsPosition().equals(BrowserActivity.TAB_POSITION_WIDGET)) {
            newsType = BrowserActivity.TAB_POSITION_WIDGET;
            view = activity.getLayoutInflater().inflate(R.layout.home_page, null);
        } else {
            view = activity.getLayoutInflater().inflate(R.layout.home_page, null);
        }

        ButterKnife.bind(this, view);
        Config config = BrowserApp.getConfig();

        this.preferenceManager = preferenceManager;
        if (isIncognito) {
            theme = 2;
        } else {
            theme = preferenceManager.getUseTheme();
        }

        newsCategoryList = new ArrayList<>();
        if (preferenceManager.getBackgroundUrl() != null) {

            try {
                final InputStream imageStream;
                imageStream = activity.openFileInput("back.jpg");
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                backgroundImage.setImageBitmap(selectedImage);
                backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (config.getBackground() != null) {
            backgroundImage.setImageDrawable(config.getBackground());
            backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        tabNamesList = new ArrayList<>();
        tabNamesList.add(activity.getResources().getString(R.string.express_panel));
        newsPageAdapter = new NewsPageAdapter(activity.getSupportFragmentManager());

        for (String s : tabNamesList) {
            tabLayout.addTab(tabLayout.newTab().setText(s));
        }


        pager.setAdapter(newsPageAdapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        pager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                }
        );
        tabLayout.setBackgroundColor(BrowserApp.getThemeManager().getTransparentPrimaryColor(theme));
        tabLayout.setTabTextColors(BrowserApp.getThemeManager().
                getDisabledIconColor(theme), BrowserApp
                .getThemeManager().getIconColor(theme)
        );

        if (!newsType.equals(BrowserActivity.TAB_POSITION_WIDGET)) {

            final String params = "wid=" + activity.getResources().getString(R.string.widgetID) + "&"
                    + "advid=" + preferenceManager.getAdvertisingId() + "$"
                    + "pn=" + activity.getPackageName() + "&" + "v=" + activity.getResources().getString(R.string.platformVersion)
                    + "apiKey=" + activity.getResources().getString(R.string.api_key);

            StartPageLoader.requestGeoData(preferenceManager, new StartPageLoader.GeoDataHandler() {
                        @Override
                        public void onResult(GeoData geoData) {

                            NewsApi.getInstance().getNews(params, geoData.getCountryCode(), new NewsApi.NewsCallback() {
                                @Override
                                public void onNewsResult(final NewsCategory newsCategory) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            newsCategoryList.add(newsCategory);
                                            tabNamesList.add(newsCategory.getName());
                                            tabLayout.addTab(tabLayout.newTab().setText(newsCategory.getName()));
                                            newsPageAdapter.notifyDataSetChanged();

                                        }
                                    });
                                }
                            });
                        }
                    }
            );
        } else {
            tabLayout.setVisibility(View.GONE);
        }

    }

    public void setUrlClickedListener(UrlClickedListener urlClickedListener) {
        this.urlClickedListener = urlClickedListener;
    }

    public View getView() {
        return view;
    }

    interface UrlClickedListener {
        void onUrlClicked(String url);
    }

    // Instances of this class are fragments representing a single
// object in our collection.
    public static class HomePageViewPagerFragment extends Fragment {
        public static final String ARG_NUM = "num";
        public static final String ARG_OBJECT = "object";
        public static final String ARG_THEME = "theme";
        @Inject
        BookmarkModel mBookmarkManager;
        @Inject
        DownloadsModel downloadsModel;

        private FrameLayout frameLayout;

        public HomePageViewPagerFragment() {
            BrowserApp.getAppComponent().inject(this);
        }


        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            Bundle args = getArguments();
            int i = args.getInt(ARG_NUM, 0);
            int theme = args.getInt(ARG_THEME, 0);

            if (i == 0) {
                View rootView = new HomePageMainTab(getActivity(), theme, mBookmarkManager, downloadsModel).getView();
                if (frameLayout != null) {
                    frameLayout.removeAllViews();
                    frameLayout.addView(rootView);
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView;
            Bundle args = getArguments();
            int i = args.getInt(ARG_NUM, 0);
            int theme = args.getInt(ARG_THEME, 0);
            if (i == 0) {
                rootView = new HomePageMainTab(getActivity(), theme, mBookmarkManager, downloadsModel).getView();
            } else {
                rootView = inflater.inflate(
                        R.layout.news_list, container, false);
                CardView cardView = rootView.findViewById(R.id.card);
                cardView.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentPrimaryColor(theme));
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) cardView.getLayoutParams();
                if (BrowserApp.getConfig().isWidgetsMargins()) {
                    int margin = getPx(8);
                    layoutParams.setMargins(margin, margin, margin, margin);
                    cardView.setRadius(getPx(4));
                } else {
                    layoutParams.setMargins(0, 0, 0, 0);
                    cardView.setRadius(0);
                }
                String widgetType =  BrowserApp.getConfig().getNewsWidgetType();
                switch (widgetType) {
                    case LIST:
                    case LIST_REVERSED:
                        cardView.setCardBackgroundColor(BrowserApp.getConfig().getNewsWidgetColor());
                        break;
                    case LIST_TRANSPARENT:
                    case LIST_TRANSPARENT_REVERSED:
                        cardView.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentColor(BrowserApp.getConfig().getNewsWidgetColor()));
                        break;
                }

                RecyclerView recyclerView = rootView.findViewById(R.id.news_list);
                NewsCategory newsCategory = (NewsCategory) args.getSerializable(ARG_OBJECT);
                if (newsCategory != null) {
                    final HomepageView.NewsAdapter newsAdapter = new HomepageView.NewsAdapter(getActivity(), theme, widgetType.equals(NewsWidget.LIST_REVERSED) || widgetType.equals(NewsWidget.LIST_TRANSPARENT_REVERSED));
                    RecyclerView.LayoutManager newsLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(newsLayoutManager);
                    recyclerView.setAdapter(newsAdapter);
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider));
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setItemViewCacheSize(20);
                    recyclerView.setDrawingCacheEnabled(true);
                    recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    newsAdapter.setNews(newsCategory.getNewsList());
                    newsAdapter.notifyDataSetChanged();
                    ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            ((UIController) getActivity()).loadUrl(newsAdapter.getNewsList().get(position).getLink());
                        }
                    });
                    ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                    final float scale = getActivity().getResources().getDisplayMetrics().density;
                    int dps = Double.valueOf(newsCategory.getNewsList().size()).intValue() * 100;

                    params.height = (int) (dps * scale + 0.5f);
                    recyclerView.setLayoutParams(params);
                }

            }
            frameLayout = new FrameLayout(getActivity());
            frameLayout.addView(rootView);
            return frameLayout;
        }

        private int getPx(int dp) {
            Resources r = getActivity().getResources();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        }
    }


    public static class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

        private final List<News> newsList;
        private Context context;
        private int theme;
        private boolean reversed;

        public NewsAdapter(Context context, int theme) {
            this(context, theme, false);
        }

        public NewsAdapter(Context context, int theme, boolean reversed) {
            this.newsList = new ArrayList<>();
            this.context = context;
            this.theme = theme;
            this.reversed = reversed;
        }

        public void setNews(List<News> newsList) {
            this.newsList.addAll(newsList);
            for (int i = 0; i < this.newsList.size(); i++) {
                this.newsList.get(i).setId(i);
            }
        }

        public List<News> getNewsList() {
            return newsList;
        }
        //------------------------------------------------------

        @Override
        public NewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View contactView = inflater.inflate(reversed ? R.layout.news_layout_reversed : R.layout.news_layout, parent, false);

            return new ViewHolder(contactView);
        }

        @Override
        public void onBindViewHolder(final NewsAdapter.ViewHolder holder, int position) {
            News web = newsList.get(position);
            holder.title.setText(web.getTitle());
            holder.title.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            holder.text.setText(web.getText());
            holder.text.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            holder.date.setText(web.getDate());
            holder.date.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            holder.source.setText(web.getSource());
            holder.source.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
            ImageLoader.getInstance().loadImage(web.getImageLink(), new ImageLoader.ImageLoadedListener() {
                @Override
                public void onImageLoaded(Bitmap b) {
                    holder.image.setImageBitmap(b);
                }
            });
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }

        @Override
        public long getItemId(int position) {
            return newsList.get(position).getId();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.text)
            TextView text;
            @BindView(R.id.source)
            TextView source;
            @BindView(R.id.date)
            TextView date;
            @BindView(R.id.image)
            ImageView image;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    public class NewsPageAdapter extends FragmentStatePagerAdapter {

        public NewsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new HomePageViewPagerFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            if (i > 0) {
                args.putSerializable(HomePageViewPagerFragment.ARG_OBJECT, newsCategoryList.get(i - 1));
            }
            args.putInt(HomePageViewPagerFragment.ARG_NUM, i);
            args.putInt(HomePageViewPagerFragment.ARG_THEME, theme);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return tabNamesList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabNamesList.get(position);
        }
    }
}

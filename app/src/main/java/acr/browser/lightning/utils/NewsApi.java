package acr.browser.lightning.utils;

import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.LruCache;
import android.util.Xml;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import acr.browser.lightning.R;
import acr.browser.lightning.domain.News;
import acr.browser.lightning.domain.NewsCategory;

/**
 * Created by roma on 11.06.2017.
 */

public class NewsApi {

    private static final long NEWS_UPDATE_TIME = 5 * 60 * 1000;

    private static String[] categories = new String[]{"sfy", "w", "n", "b", "t", "e", "s", "m"};
    private static NewsApi INSTANCE;
    private Random random;
    private LruCache<String, List<NewsCategory>> newsCache;
    private long lastUpdateTime;

    private NewsApi() {
        random = new Random();
        int cacheSize = 4 * 1024 * 1024; // 4MiB
        newsCache = new LruCache<String, List<NewsCategory>>(cacheSize);

    }

    public static NewsApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NewsApi();
        }
        return INSTANCE;
    }
    //------------------------------------------------------

    public void getNews(final String params, final String countryCode, final NewsCallback callback) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if (newsCache.get("news") != null && new Date().getTime() - lastUpdateTime < NEWS_UPDATE_TIME) {
                        for (NewsCategory newsCategory : newsCache.get("news")) {
                            callback.onNewsResult(newsCategory);
                        }
                    } else {
                        List<NewsCategory> newsCategoryList = new ArrayList<NewsCategory>();
                        loadTopStories(params, countryCode, newsCategoryList, callback);

                        for (String category : categories) {
                            loadCategory(params, countryCode, category, newsCategoryList, callback);
                        }
                        newsCache.put("news", newsCategoryList);
                        lastUpdateTime = new Date().getTime();
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (XmlPullParserException | IOException | ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    private void loadTopStories(String params, String countryCode, List<NewsCategory> newsCategoryList, final NewsCallback callback) throws IOException, ParseException, XmlPullParserException {
        NewsCategory newsCategory = parse(new URL("https://news.google.com/news?cf=all&hl=" + countryCode + "&pz=1&output=rss").openStream());
        NewsCategory appsgeyserNews = parse(new URL("http://frame.appsgeyser.com/api/news/rss.php" + "?" + params).openStream());

        if ((newsCategory == null || newsCategory.getNewsList().size() == 0) && appsgeyserNews != null) {
            newsCategory = appsgeyserNews;
        } else {
            if (appsgeyserNews != null) {
                for (News news : appsgeyserNews.getNewsList()) {
                    if (news.isAds()) {
                        newsCategory.getNewsList().add(Math.abs(random.nextInt()) % newsCategory.getNewsList().size(), news);
                    } else {
                        newsCategory.getNewsList().add(news);
                    }
                }
            }
        }
        newsCategoryList.add(newsCategory);
        callback.onNewsResult(newsCategory);
    }

    private void loadCategory(String params, String countryCode, String category, List<NewsCategory> newsCategoryList, final NewsCallback callback) throws IOException, ParseException, XmlPullParserException {
        NewsCategory newsCategory = parse(new URL("https://news.google.com/news?cf=all&hl=" + countryCode + "&pz=1&output=rss&topic=" + category).openStream());
        NewsCategory appsgeyserNews = parse(new URL("http://frame.appsgeyser.com/api/news/" + category + "/rss.php" + "?" + params).openStream());

        if ((newsCategory == null || newsCategory.getNewsList().size() == 0) && appsgeyserNews != null) {
            newsCategory = appsgeyserNews;
        } else {
            if (appsgeyserNews != null) {
                for (News news : appsgeyserNews.getNewsList()) {
                    if (news.isAds()) {
                        newsCategory.getNewsList().add(Math.abs(random.nextInt()) % newsCategory.getNewsList().size(), news);
                    } else {
                        newsCategory.getNewsList().add(news);
                    }
                }
            }
        }
        newsCategoryList.add(newsCategory);
        callback.onNewsResult(newsCategory);
    }

    public NewsCategory parse(InputStream in)
            throws XmlPullParserException, IOException, ParseException {
        try {
            List<News> newsList = new ArrayList<>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            int eventType = parser.getEventType();
            boolean insideItem = false;
            String categoryTitle = "";
            String title = "";
            String description = "";
            String link = "";
            String imageLink = "";
            String date = "";
            String source = "";
            boolean isAds = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if (parser.getName().equalsIgnoreCase("item")) {
                        insideItem = true;
                    } else if (parser.getName().equalsIgnoreCase("title")) {
                        if (insideItem) {
                            title = parser.nextText();
                            if (title.contains(" – ") || title.contains(" - ")) {
                                String titleArray[] = title.split(" – | - ");
                                title = titleArray[0];
                                if (titleArray.length > 1) {
                                    if (source.equals("")) {
                                        source = titleArray[1];
                                    }
                                }
                            }
                        } else {
                            categoryTitle = parser.nextText();
                            if (categoryTitle.contains(" - ")) {
                                categoryTitle = categoryTitle.split(" - ")[0];
                            } else if (categoryTitle.contains(" – ")) {
                                categoryTitle = categoryTitle.split(" – ")[0];
                            }
                        }
                    } else if (parser.getName().equalsIgnoreCase("link")) {
                        if (insideItem) {
                            link = parser.nextText();  // extract the link of article
                        }
                    } else if (parser.getName().equalsIgnoreCase("imageLink")) {
                        if (insideItem) {
                            imageLink = parser.nextText();  // extract the link of article
                        }
                    } else if (parser.getName().equalsIgnoreCase("isAds")) {
                        if (insideItem) {
                            isAds = parser.nextText().equals("true");  // extract the link of article
                        }
                    } else if (parser.getName().equalsIgnoreCase("source")) {
                        if (insideItem) {
                            source = parser.nextText();  // extract the link of article
                        }
                    } else if (parser.getName().equalsIgnoreCase("pubDate")) {
                        if (insideItem) {
                            date = parser.nextText();  // extract the link of article
                        }
                    } else if (parser.getName().equalsIgnoreCase("description")) {
                        if (insideItem) {
                            description = parser.nextText();  // extract the link of article
                            description = StringEscapeUtils.unescapeHtml4(description);
                            description = StringEscapeUtils.unescapeHtml3(description);
                            try {
                                Document doc = Jsoup.parse(description);
                                if (imageLink.equals("")) {
                                    Elements images = doc.select("img");
                                    for (Element el : images) {
                                        if (!el.attr("src").equals("")) {
                                            imageLink = "https:" + el.attr("src");
                                            break;
                                        }
                                    }
                                }
                                description = doc.text();
                            } catch (Throwable throwable) {
                                Log.w("jsoup", throwable.getMessage());
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                    insideItem = false;
                    News news = new News();
                    news.setTitle(title);
                    news.setLink(link);
                    DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                    Date d = formatter.parse(date);
                    news.setDate(DateUtils.getRelativeTimeSpanString(d.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString());
                    news.setText(description);
                    news.setSource(source);
                    news.setImageLink(imageLink);
                    news.setAds(isAds);
                    newsList.add(news);
                    title = "";
                    description = "";
                    link = "";
                    imageLink = "";
                    date = "";
                    source = "";
                    isAds = false;
                }

                eventType = parser.next(); // move to next element
            }
            NewsCategory newsCategory = new NewsCategory();
            newsCategory.setName(categoryTitle);
            newsCategory.setNewsList(newsList);
            return newsCategory;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            in.close();
        }
    }

    public interface NewsCallback {
        void onNewsResult(NewsCategory newsCategory);
    }
}

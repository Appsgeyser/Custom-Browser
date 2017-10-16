package acr.browser.lightning.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import acr.browser.lightning.R;
import acr.browser.lightning.app.BrowserApp;
import acr.browser.lightning.utils.HomePageWidget;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roma on 17.09.2017.
 */

public class NewsWidget implements HomePageWidget {

    public static final String LIST = "newsList";
    public static final String LIST_TRANSPARENT = "newsListTransparent";
    public static final String LIST_REVERSED = "newsListReversed";
    public static final String LIST_TRANSPARENT_REVERSED = "newsListTransparentReversed";

    @BindView(R.id.card)
    CardView newsCard;
    @BindView(R.id.news_list)
    RecyclerView newsList;

    private View view;

    public NewsWidget(Context context, String widgetType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.news_widget, null);
        ButterKnife.bind(this, view);
        switch (widgetType) {
            case LIST:
            case LIST_REVERSED:
                newsCard.setCardBackgroundColor(BrowserApp.getConfig().getNewsWidgetColor());
                break;
            case LIST_TRANSPARENT:
            case LIST_TRANSPARENT_REVERSED:
                newsCard.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentColor(BrowserApp.getConfig().getNewsWidgetColor()));
                break;
        }
    }

    @Override
    public Integer getOrderId() {
        return BrowserApp.getConfig().getNewsWidgetOrderId();
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void setMargins(int margins, int cornerRadius) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(margins, margins, margins, margins);
        newsCard.setRadius(cornerRadius);
    }

    @Override
    public int compareTo(@NonNull HomePageWidget homePageWidget) {
        return getOrderId().compareTo(homePageWidget.getOrderId());
    }
}

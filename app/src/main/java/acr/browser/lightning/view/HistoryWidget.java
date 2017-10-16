package acr.browser.lightning.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import acr.browser.lightning.R;
import acr.browser.lightning.app.BrowserApp;
import acr.browser.lightning.utils.HomePageWidget;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by roma on 12.09.2017.
 */

public class HistoryWidget implements HomePageWidget {

    public static final String LIST_TRANSPARENT = "historyListTransparent";
    public static final String LIST = "historyList";


    @BindView(R.id.historyGrid)
    public RecyclerView historyGrid;
    @BindView(R.id.historyCard)
    public CardView historyCard;
    @BindView(R.id.title)
    public TextView title;

    private View view;

    public HistoryWidget(Context context, String widgetType, int theme) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.history_widget, null);
        ButterKnife.bind(this, view);
        switch (widgetType){
            case HistoryWidget.LIST:
                historyCard.setCardBackgroundColor(BrowserApp.getConfig().getHistoryWidgetColor());
                break;
            case HistoryWidget.LIST_TRANSPARENT:
                historyCard.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentColor(BrowserApp.getConfig().getHistoryWidgetColor()));
                break;
        }
    }

    public View getView() {
        return view;
    }

    @Override
    public void setMargins(int margins, int cornerRadius) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(margins, margins, margins, margins);
        historyCard.setRadius(cornerRadius);
    }


    @Override
    public Integer getOrderId() {
        return BrowserApp.getConfig().getHistoryWidgetOrderId();
    }


    @Override
    public int compareTo(@NonNull HomePageWidget homePageWidget) {
        return getOrderId().compareTo(homePageWidget.getOrderId());
    }
}

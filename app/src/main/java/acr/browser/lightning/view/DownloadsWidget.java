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

public class DownloadsWidget implements HomePageWidget {

    public static final String GRID_TRANSPARENT = "downloadsGridTransparent";
    public static final String GRID = "downloadsGrid";
    public static final String LIST_TRANSPARENT = "downloadsListTransparent";
    public static final String LIST = "downloadsList";


    @BindView(R.id.downloads_grid)
    public RecyclerView downloadsGrid;
    @BindView(R.id.downloadsCard)
    public CardView downloadsCard;
    @BindView(R.id.title)
    public TextView title;

    private View view;

    public DownloadsWidget(Context context, String widgetType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.downloads_widget, null);
        ButterKnife.bind(this, view);
        switch (widgetType){
            case DownloadsWidget.GRID:
            case DownloadsWidget.LIST:
                downloadsCard.setCardBackgroundColor(BrowserApp.getConfig().getDownloadsWidgetColor());
                break;
            case DownloadsWidget.GRID_TRANSPARENT:
            case DownloadsWidget.LIST_TRANSPARENT:
                downloadsCard.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentColor(BrowserApp.getConfig().getDownloadsWidgetColor()));
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
        downloadsCard.setRadius(cornerRadius);
    }


    @Override
    public Integer getOrderId() {
        return BrowserApp.getConfig().getDownloadsWidgetOrderId();
    }


    @Override
    public int compareTo(@NonNull HomePageWidget homePageWidget) {
        return getOrderId().compareTo(homePageWidget.getOrderId());
    }
}

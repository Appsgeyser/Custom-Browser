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
 * Created by roma on 12.09.2017.
 */

public class BookmarkWidget implements HomePageWidget {

    public static final String GRID = "bookmarkGrid";
    public static final String BIG_GRID = "bookmarkFlatGrid";
    public static final String LIST = "bookmarkList";
    public static final String LIST_2_COLUMNS = "bookmarkList2Columns";

    @BindView(R.id.bookmarks_grid)
    public RecyclerView bookmarksGrid;
    @BindView(R.id.bookmarksCard)
    public CardView bookmarksCard;

    private View view;

    public BookmarkWidget(Context context, String widgetType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        switch (widgetType){
            case GRID:
            case BIG_GRID:
            case LIST:
            case LIST_2_COLUMNS:
                view = inflater.inflate(R.layout.bookmark_widget, null);
                break;
        }
        ButterKnife.bind(this, view);
        bookmarksCard.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentColor(BrowserApp.getConfig().getBookmarkWidgetColor()));
    }

    public View getView() {
        return view;
    }

    @Override
    public void setMargins(int margins, int cornerRadius) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(margins, margins, margins, margins);
        bookmarksCard.setRadius(cornerRadius);
    }

    @Override
    public Integer getOrderId() {
        return BrowserApp.getConfig().getBookmarkWidgetOrderId();
    }


    @Override
    public int compareTo(@NonNull HomePageWidget homePageWidget) {
        return getOrderId().compareTo(homePageWidget.getOrderId());
    }
}

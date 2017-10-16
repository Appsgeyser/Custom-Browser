package acr.browser.lightning.utils;

import android.support.v7.widget.CardView;
import android.view.View;

/**
 * Created by roma on 16.09.2017.
 */

public interface HomePageWidget extends Comparable<HomePageWidget>{

    Integer getOrderId();

    View getView();

    void setMargins(int margins, int cornerRadius);
}

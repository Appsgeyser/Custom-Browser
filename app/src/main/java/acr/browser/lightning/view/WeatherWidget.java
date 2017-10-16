package acr.browser.lightning.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class WeatherWidget implements HomePageWidget {

    public static final String SIMPLE = "weatherSimple";
    public static final String PICTURE = "weatherDetailed";
    public static final String THIN = "weatherFlat";

    @BindView(R.id.temperature)
    public TextView temperature;
    @BindView(R.id.weather_icon)
    public ImageView weatherIcon;
    @BindView(R.id.location)
    public TextView location;
    @BindView(R.id.weatherText)
    public TextView weatherText;
    @BindView(R.id.weatherWidget)
    public CardView weatherWidget;
    @BindView(R.id.celsiusButton)
    public TextView celsiusButton;
    @BindView(R.id.temperaturePanel)
    public LinearLayout temperaturePanel;
    @BindView(R.id.imagePanel)
    public ViewGroup imagePanel;

    private View view;

    public WeatherWidget(Context context, String widgetType, int theme) {

        LayoutInflater inflater = LayoutInflater.from(context);
        switch (widgetType){
            case WeatherWidget.SIMPLE:{
                view = inflater.inflate(R.layout.weather_widget, null);

                break;
            }
            case WeatherWidget.PICTURE:{
                view = inflater.inflate(R.layout.weather_widget_2, null);
                break;
            }
            case WeatherWidget.THIN:{
                view = inflater.inflate(R.layout.weather_widget_3, null);
                break;
            }
        }
        ButterKnife.bind(this, view);

        if (widgetType.equals(WeatherWidget.SIMPLE)) {
            weatherWidget.setCardBackgroundColor(BrowserApp.getThemeManager().getTransparentColor(BrowserApp.getConfig().getWeatherWidgetColor()));
        } else {
            weatherWidget.setCardBackgroundColor(BrowserApp.getConfig().getWeatherWidgetColor());
        }
        location.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
        temperature.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
        weatherText.setTextColor(BrowserApp.getThemeManager().getIconColor(theme));
    }

    public View getView() {
        return view;
    }

    @Override
    public void setMargins(int margins, int cornerRadius) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(margins, margins, margins, margins);
        weatherWidget.setRadius(cornerRadius);
    }

    public TextView getTemperature() {
        return temperature;
    }

    public ImageView getWeatherIcon() {
        return weatherIcon;
    }

    public TextView getLocation() {
        return location;
    }

    public TextView getWeatherText() {
        return weatherText;
    }

    public CardView getWeatherWidget() {
        return weatherWidget;
    }

    public TextView getCelsiusButton() {
        return celsiusButton;
    }

    public LinearLayout getTemperaturePanel() {
        return temperaturePanel;
    }

    public ViewGroup getImagePanel() {
        return imagePanel;
    }


    @Override
    public Integer getOrderId() {
        return BrowserApp.getConfig().getWeatherWidgetOrderId();
    }


    @Override
    public int compareTo(@NonNull HomePageWidget homePageWidget) {
        return getOrderId().compareTo(homePageWidget.getOrderId());
    }
}

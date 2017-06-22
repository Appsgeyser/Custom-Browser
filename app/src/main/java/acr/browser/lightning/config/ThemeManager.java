package acr.browser.lightning.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.List;

import acr.browser.lightning.R;
import acr.browser.lightning.app.BrowserApp;
import acr.browser.lightning.utils.Preconditions;

/**
 * Created by roma on 29.04.2017.
 */

public class ThemeManager {

    private List<Theme> themeList;

    public ThemeManager(Context context) {
        Config config = BrowserApp.getConfig();
        themeList = new ArrayList<>();
        //default
        themeList.add(new Theme(config.getPrimaryColor(),
                config.getPrimaryDarkColor(),
                config.getAccentColor(),
                context.getResources().getColor(R.color.drawer_background),
                context.getResources().getColor(R.color.divider_light),
                config.getPrimaryDarkColor(),
                context.getResources().getColor(R.color.primary_color),
                context.getResources().getColor(R.color.icon_dark_theme),
                context.getResources().getColor(R.color.icon_dark_theme_disabled)));
        //light
        themeList.add(new Theme(context.getResources().getColor(R.color.primary_color),
                context.getResources().getColor(R.color.transparent),
                context.getResources().getColor(R.color.accent_color),
                context.getResources().getColor(R.color.drawer_background),
                context.getResources().getColor(R.color.divider_light),
                context.getResources().getColor(R.color.black),
                context.getResources().getColor(R.color.primary_color),
                context.getResources().getColor(R.color.icon_light_theme),
                context.getResources().getColor(R.color.icon_light_theme_disabled)));
        //dark
        themeList.add(new Theme(context.getResources().getColor(R.color.primary_color_dark),
                context.getResources().getColor(R.color.transparent),
                context.getResources().getColor(R.color.accent_color),
                context.getResources().getColor(R.color.drawer_background_dark),
                context.getResources().getColor(R.color.divider_dark),
                context.getResources().getColor(R.color.black),
                context.getResources().getColor(R.color.primary_color_dark),
                context.getResources().getColor(R.color.icon_dark_theme),
                context.getResources().getColor(R.color.icon_dark_theme_disabled)
        ));
        //black
        themeList.add(new Theme(context.getResources().getColor(R.color.black),
                context.getResources().getColor(R.color.black),
                context.getResources().getColor(R.color.accent_color),
                context.getResources().getColor(R.color.black),
                context.getResources().getColor(R.color.black),
                context.getResources().getColor(R.color.black),
                context.getResources().getColor(R.color.primary_color_dark),
                context.getResources().getColor(R.color.icon_dark_theme),
                context.getResources().getColor(R.color.icon_dark_theme_disabled)
        ));
    }

    public int getPrimaryColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getColorPrimary();
    }

    public int getPrimarydarkColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getColorPrimaryDark();
    }

    public int getAccentColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getColorAccent();
    }

    public int getDrawerBackgroundColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getColorPrimary();
    }

    public int getDividerColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getDividerColor();
    }

    public int getStatusBarColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getStatusBarColor();
    }

    public int getBackgroundColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getBackgroundColor();
    }

    public int getIconColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getIconColor();
    }

    public int getDisabledIconColor(int theme) {
        if(theme > themeList.size()){
            return 0;
        }
        return themeList.get(theme).getDisabledIconColor();
    }

    public int getTransparentPrimaryColor(int theme){
        return adjustAlpha(getPrimaryColor(theme), 0.7f);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public Bitmap getThemedBitmap(@NonNull Context context, @DrawableRes int res, int theme) {
        int color = themeList.get(theme).getIconColor();

        Bitmap sourceBitmap = getBitmapFromVectorDrawable(context, res);
        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Paint p = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(sourceBitmap, 0, 0, p);
        sourceBitmap.recycle();
        return resultBitmap;
    }

    private static Bitmap getBitmapFromVectorDrawable(@NonNull Context context, int drawableId) {
        Drawable drawable = getVectorDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private static Drawable getVectorDrawable(@NonNull Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Preconditions.checkNonNull(drawable);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        return drawable;
    }

    public Drawable getThemedDrawable(@NonNull Context context, @DrawableRes int res, int theme) {
        int color = themeList.get(theme).getIconColor();

        final Drawable drawable = getVectorDrawable(context, res);
        drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }
}

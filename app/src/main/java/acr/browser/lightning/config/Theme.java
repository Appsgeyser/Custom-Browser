package acr.browser.lightning.config;

/**
 * Created by roma on 10.06.2017.
 */

public class Theme {
    private int colorPrimary;
    private int colorPrimaryDark;
    private int colorAccent;
    private int drawerBackground;
    private int dividerColor;
    private int statusBarColor;
    private  int backgroundColor;
    private  int iconColor;
    private  int disabledIconColor;

    public Theme(int colorPrimary, int colorPrimaryDark, int colorAccent, int drawerBackground, int dividerColor, int statusBarColor, int backgroundColor, int iconColor,int disabledIconColor) {
        this.colorPrimary = colorPrimary;
        this.colorPrimaryDark = colorPrimaryDark;
        this.colorAccent = colorAccent;
        this.drawerBackground = drawerBackground;
        this.dividerColor = dividerColor;
        this.statusBarColor = statusBarColor;
        this.backgroundColor = backgroundColor;
        this.iconColor = iconColor;
        this.disabledIconColor = disabledIconColor;
    }
    //------------------------------------------------------

    public int getColorPrimary() {
        return colorPrimary;
    }

    public void setColorPrimary(int colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public int getColorPrimaryDark() {
        return colorPrimaryDark;
    }

    public void setColorPrimaryDark(int colorPrimaryDark) {
        this.colorPrimaryDark = colorPrimaryDark;
    }

    public int getColorAccent() {
        return colorAccent;
    }

    public void setColorAccent(int colorAccent) {
        this.colorAccent = colorAccent;
    }

    public int getDrawerBackground() {
        return drawerBackground;
    }

    public void setDrawerBackground(int drawerBackground) {
        this.drawerBackground = drawerBackground;
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public void setStatusBarColor(int statusBarColor) {
        this.statusBarColor = statusBarColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
    }

    public int getDisabledIconColor() {
        return disabledIconColor;
    }

    public void setDisabledIconColor(int disabledIconColor) {
        this.disabledIconColor = disabledIconColor;
    }
}

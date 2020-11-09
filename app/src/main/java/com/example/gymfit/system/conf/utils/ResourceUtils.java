package com.example.gymfit.system.conf.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.gymfit.R;
import com.example.gymfit.system.main.App;

public final class ResourceUtils {

    /**
     * This method get from project folder the Uri from resource using respective ID
     *
     * @param resourceId origin ID resource file
     * @return String Uri path
     */
    public static String getURIForResource(int resourceId) {
        return Uri.parse("android.resource://"+ R.class.getPackage().getName()+ "/" + resourceId).toString();
    }

    /**
     * This method get from project folder the String from resource using respective ID
     *
     * @param resourceId origin ID resource file
     * @return origin ID resource file
     */
    public static String getStringFromID(int resourceId) {
        return App.getResource().getString(resourceId);
    }

    /**
     * This method get from project folder the String Array from resource using respective ID
     *
     * @param resourceId origin ID resource file
     * @return origin ID resource file
     */
    public static String[] getStringArrayFromID(int resourceId) {
        return App.getResource().getStringArray(resourceId);
    }

    /**
     * This method get from project folder the Color from resource using respective ID
     *
     * @param resourceId origin ID resource file
     * @return origin ID resource file
     */
    public static int getColorFromID(int resourceId) {
        return ResourcesCompat.getColor(App.getResource(), resourceId, null);
    }

    /**
     * This method get from project folder the Drawable from resource using respective ID
     *
     * @param resourceId origin ID resource file
     * @return origin Drawable resource file
     */
    public static Drawable getDrawableFromID(int resourceId) {
        return ResourcesCompat.getDrawable(App.getResource(), resourceId, null);
    }

}

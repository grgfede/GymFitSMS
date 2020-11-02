package com.example.gymfit.system.conf.utils;

import android.content.res.Resources;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

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

}

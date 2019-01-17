package com.mafdy.onthemove.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.location.places.Place;

/**
 * Created by SBP on 7/18/2018.
 */

public class Preferencemanager {

    public static boolean savePlaceInPref(Context c, Place p)
    {
        SharedPreferences manager = c.getApplicationContext().getSharedPreferences("onthemove",Context.MODE_PRIVATE);
        manager.edit().putString("placename",p.getName().toString()).apply();

        try {
            manager.edit().putString("placeaddress", p.getAddress().toString()).apply();
        } catch (Exception e)
        { e.printStackTrace();}
        manager.edit().putString("placeid",p.getId()).apply();

        return true;

    }

    public static String getPlaceName(Context c)
    {

        SharedPreferences manager = c.getApplicationContext().getSharedPreferences("onthemove",Context.MODE_PRIVATE);
        return manager.getString("placename","");
    }

    public static String getPlaceId(Context c)
    {
        return c.getApplicationContext().getSharedPreferences("onthemove",Context.MODE_PRIVATE).getString("placeid","");
    }

    public static String getPlaceAddress(Context c)
    {
        return c.getApplicationContext().getSharedPreferences("onthemove",Context.MODE_PRIVATE).getString("placeaddress","");
    }

    public static boolean deletePlace(Context c)
    {
        SharedPreferences manager = c.getApplicationContext().getSharedPreferences("onthemove",Context.MODE_PRIVATE);
        manager.edit().remove("placeid").apply();
        manager.edit().remove("placename").apply();

        return true;
    }

    public static boolean getIsFirstOpen(Context c)
    {
        SharedPreferences sp = c.getApplicationContext().getSharedPreferences("onthemove", Context.MODE_PRIVATE);
        if (!sp.getBoolean("first", false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("first", true);
            editor.apply();
            return true;

        }

        return false;
    }
}

package com.mafdy.onthemove.database;

import android.arch.persistence.room.TypeConverter;

import com.crashlytics.android.Crashlytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by SBP on 7/6/2018.
 */

public class DatetimeTypeConverter {

    @TypeConverter
    public static Calendar toCalendar(String value) {
        if (value == null)
            return null;
        else {
            Calendar c = Calendar.getInstance();

            SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

            try {
                c.setTime(n.parse(value));
            } catch (ParseException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                return null;
            }

            return c;
        }
    }

    @TypeConverter
    public static String toString(Calendar value) {

        SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

        String s = n.format(value.getTime());

        return s;
    }

}

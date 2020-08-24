package com.mafdy.onthemove.database;

import androidx.room.TypeConverter;

import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by SBP on 7/13/2018.
 */

public class DatetimeTypeConverter2 {

    @TypeConverter
    public static Calendar toCalendar(long value) {
        if (value == 0)
            return Calendar.getInstance();
        else {
            Calendar c = Calendar.getInstance();

            SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

            try {
                c.setTimeInMillis(value);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                return Calendar.getInstance();
            }

            return c;
        }
    }

    @TypeConverter
    public static long toLong(Calendar value) {

        //SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

        //String s = n.format(value.getTime());

        return value.getTimeInMillis();
    }

}

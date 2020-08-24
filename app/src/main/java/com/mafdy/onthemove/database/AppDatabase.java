package com.mafdy.onthemove.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

/**
 * Created by SBP on 7/6/2018.
 */

@Database(entities = {Status.class}, version = 1)
@TypeConverters({DatetimeTypeConverter2.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase appDatabase;
    public abstract StatusDAO status();
    private Context context;
    public static AppDatabase getInstance(Context context){
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "Status-database")
                 //   .allowMainThreadQueries()
                    .build();
        }
        return appDatabase;
    }

    public static void destroyInstance() {
        appDatabase = null;
    }
}
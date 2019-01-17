package com.mafdy.onthemove.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.mafdy.onthemove.MainActivity;
import com.mafdy.onthemove.R;
import com.mafdy.onthemove.database.Status;

import java.text.SimpleDateFormat;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);


        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("Share_Latest",true);


        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static final String widgetupdateaction = "ACTION_UPDATE_WIDGET";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
       /* if(intent.getAction().equals(widgetupdateaction))
        {

           final Status status = intent.getParcelableExtra("Status");

            SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
                views.setTextViewText(R.id.appwidget_text, "Currently " + status.getActivity() + " Time: " + n.format(status.getDatetime()) + " \nTap to Share now");


            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.putExtra("Share_Latest",true);


            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

            views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

                ComponentName appWidget = new ComponentName(context, NewAppWidget.class);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidget, views);


        }*/
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


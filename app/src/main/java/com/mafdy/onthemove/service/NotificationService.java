package com.mafdy.onthemove.service;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mafdy.onthemove.MainActivity;
import com.mafdy.onthemove.R;
import com.mafdy.onthemove.database.AppDatabase;
import com.mafdy.onthemove.database.Status;
import com.mafdy.onthemove.utils.Preferencemanager;
import com.mafdy.onthemove.widget.NewAppWidget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class NotificationService extends Service
        implements ActivityLocationIntentService.ServiceToService {


    private static final String TAG = NotificationService.class.getSimpleName();


    private static final String CHANNEL_ID = "channel_01";

    public static final String BROADCAST_ACTION = "Broadcast_Action";
    public static final String EXTRA_STATUS = "Extra_Status";


    private final IBinder mBinder = new LocalBinder();


    private static final int NOTIFICATION_ID = 881;


    private NotificationManager mNotificationManager;

    private boolean mChangingConfiguration = false;

    private Handler mServiceHandler;
    private FusedLocationProviderClient mFusedLocationClient;

    public static ActivityLocationIntentService.ServiceToService serviceToService;


    public NotificationService() {
    }

    @Override
    public void onCreate() {

        serviceToService = this;

        mFusedLocationClient = new FusedLocationProviderClient(this.getApplicationContext());
        mActivityRecognitionClient = ActivityRecognition.getClient(this);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getText(R.string.app_name);

            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);


            mNotificationManager.createNotificationChannel(mChannel);
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("Service started");


        if (intent != null) {

            if (intent.getBooleanExtra("From_Notification", false)) {
                mActivityRecognitionClient.removeActivityTransitionUpdates(getIntentServicePendingIntent());
                mFusedLocationClient.removeLocationUpdates(callback);
                stopSelf();
            }
            if (intent.getBooleanExtra("Share_Latest", false)) {
              /*  Status s = intent.getParcelableExtra("Status");

                if (s != null)
                    shareLatestStatus(s);
                else {
                    new AsyncTask<Void, Void, Status>() {
                        @Override
                        protected com.mafdy.onthemove.database.Status doInBackground(Void... voids) {

                            return AppDatabase.getInstance(NotificationService.this).status().getLatestStatus_Service();
                        }

                        @Override
                        protected void onPostExecute(com.mafdy.onthemove.database.Status status) {
                            super.onPostExecute(status);
                            shareLatestStatus(status);
                        }
                    }.execute();
                }
*/

            }
        }


        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {

        Timber.i("in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {

        Timber.i("in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.i("Last client unbound from service");
        Timber.i("Starting foreground service");
        if (!mChangingConfiguration && islocationon) {/*

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                mNotificationManager.startServiceInForeground(new Intent(this,
                        NotificationService.class), NOTIFICATION_ID, getNotification());
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }
             */


            new AsyncTask<Void, Void, Status>() {
                @Override
                protected com.mafdy.onthemove.database.Status doInBackground(Void... voids) {


                    return AppDatabase.getInstance(NotificationService.this).status().getLatestStatus_Service();
                }

                @Override
                protected void onPostExecute(com.mafdy.onthemove.database.Status status) {
                    super.onPostExecute(status);

                    if (status != null) {
                        startForeground(NOTIFICATION_ID, getNotification(status));
                        updateWidgetWithStatusUpdate(status);
                    }

                }
            }.execute();

        }


        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }


    private Notification getNotification(final Status s) {
        Intent intent = new Intent(this, NotificationService.class);

        intent.putExtra("From_Notification", true);

        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent activityintent = new Intent(this, MainActivity.class);
        activityintent.putExtra("Share_Latest", true);
        // activityintent.putExtra("Status", s);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                activityintent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_launcher_foreground, "Share now",
                        activityPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Stop updates",
                        servicePendingIntent)
                .setContentText("Currently " + s.getActivity() + (s.getLocationaddress() == null || s.getLocationaddress().equals("null") ? "" : " at " + s.getLocationaddress()))
                .setContentTitle("Latest Status update")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Currently " + s.getActivity() + " at " + s.getLocationaddress())
                .setWhen(System.currentTimeMillis());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();

    }


    public void onNewStatus(Status s) {


        /*Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(EXTRA_STATUS, s);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
*/

        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification(s));
        }

        updateWidgetWithStatusUpdate(s);

    }

    @Override
    public void getLocation(final List<ActivityTransitionEvent> activities, DetectedActivityResponse response, final boolean hasdestination) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        final Task<LocationAvailability> t2 = mFusedLocationClient.getLocationAvailability();
        t2.addOnCompleteListener(new OnCompleteListener<LocationAvailability>() {
            @Override
            public void onComplete(@NonNull Task<LocationAvailability> task) {
                if (task.getResult().isLocationAvailable()) {

                    if (ActivityCompat.checkSelfPermission(NotificationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NotificationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    final Task<Location> t = mFusedLocationClient.getLastLocation();

                    t.addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location c = task.getResult();

                            if (activities != null && c != null) {
                                String activity = "";
                                String type = "";

                                for (ActivityTransitionEvent event : activities) {

                                    if (event.getActivityType() == DetectedActivity.IN_VEHICLE)
                                        activity = "In Vehicle";
                                    else if (event.getActivityType() == DetectedActivity.ON_BICYCLE)
                                        activity = "On Bicycle";
                                    else if (event.getActivityType() == DetectedActivity.ON_FOOT)
                                        activity = "On Foot";
                                    else if (event.getActivityType() == DetectedActivity.RUNNING)
                                        activity = "Running";
                                    else if (event.getActivityType() == DetectedActivity.STILL)
                                        activity = "Still";
                                    else if (event.getActivityType() == DetectedActivity.WALKING)
                                        activity = "Walking";

                                    if (event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                                        type = "Enter";
                                    else
                                        type = "Exit";
                                }


                                final Status s = new Status();
                                s.setActivity(activity);
                                s.setDatetime(Calendar.getInstance());
                                s.setDestinationname("");
                                s.setLatitude(c.getLatitude());
                                s.setLongitude(c.getLongitude());
                                s.setLocationaccuracy(c.getAccuracy());
                                s.setTransition(type);
                                try {
                                    if (hasdestination) {
                                        s.setDestinationname(Preferencemanager.getPlaceName(NotificationService.this));
                                        s.setDestinationaddress(Preferencemanager.getPlaceAddress(NotificationService.this));
                                        s.setDestinationid(Preferencemanager.getPlaceId(NotificationService.this));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //updateUIafterStatus(s); send broadcast to activity
                                onNewStatus(s);

                                if (MainActivity.mStatusViewModel != null) {
                                    MainActivity.mStatusViewModel.insert(s);
                                } else {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... voids) {

                                            AppDatabase.getInstance(NotificationService.this).status().insertStatus(s);

                                            return null;
                                        }
                                    }.execute();
                                }


                                startGeocoderIntentService();

                                if (s.getActivity().equals("Still")) {
                                    mLocReq.setFastestInterval(30000);
                                    mLocReq.setInterval(60000);
                                } else {
                                    mLocReq.setFastestInterval(10000);
                                    mLocReq.setInterval(20000);
                                }


                            }

                        }
                    });


                }
            }
        });

    }


    public class LocalBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }

    }


    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }


    LocationCallback callback = new LocationCallback() {
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            mActivityRecognitionClient.removeActivityTransitionUpdates(getIntentServicePendingIntent());
            initializeActivityTransitionClient();


        }
    };

    public void startService() {

        startService(new Intent(getApplicationContext(), NotificationService.class));
        initializeActivityTransitionClient();
        initializeFusedLocationClient();

        islocationon = true;

    }

    public void stopService() {

        mFusedLocationClient.removeLocationUpdates(callback);
        mActivityRecognitionClient.removeActivityTransitionUpdates(getIntentServicePendingIntent());

        stopSelf();

        islocationon = false;

    }

    public void initializeFusedLocationClient() {

           /*  mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mActivityRecognitionClient.requestActivityUpdates(
                5000,getActivityDetectionPendingIntent());

        LocationRequest mLocReq = new LocationRequest();
        mLocReq.setInterval(5000);
        mLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocReq.setFastestInterval(3000);*/


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationSettingsRequest.Builder b = new LocationSettingsRequest.Builder();
        b.addLocationRequest(getLocationRequest());
        //b.setNeedBle(true);
        //b.setAlwaysShow(true);
        b.build();

        mFusedLocationClient.setMockMode(false);
        mFusedLocationClient.requestLocationUpdates(getLocationRequest(), callback, this.getMainLooper());


    }

    public static boolean islocationon = false;

    ActivityRecognitionClient mActivityRecognitionClient;

    public void initializeActivityTransitionClient() {

        List<ActivityTransition> activityTransitionList = getTransitionActivityList();

        ActivityTransitionRequest request = new ActivityTransitionRequest(activityTransitionList);


        Task<Void> task = mActivityRecognitionClient.requestActivityTransitionUpdates(request, getIntentServicePendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Timber.d("Sucess for activitiy recognition");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.d("Fail for activity recognition");
            }
        });

    }

    public void startGeocoderIntentService() {
        Intent intent = new Intent(this, GeocoderAddressService.class);

        startService(intent);
    }


    public PendingIntent getIntentServicePendingIntent() {
        Intent intent = new Intent(this, ActivityLocationIntentService.class);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    int[] detectedActivity = new int[]{
            DetectedActivity.IN_VEHICLE,
            //DetectedActivity.ON_BICYCLE,
            DetectedActivity.ON_FOOT,
            DetectedActivity.RUNNING,
            DetectedActivity.STILL,
            // DetectedActivity.TILTING,
            // DetectedActivity.UNKNOWN,
            DetectedActivity.WALKING};

    public List<ActivityTransition> getTransitionActivityList() {
        List<ActivityTransition> transitions = new ArrayList<>();
        for (int activity : detectedActivity) {
            transitions.add(
                    new ActivityTransition.Builder()
                            .setActivityType(activity)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                            .build());
            transitions.add(
                    new ActivityTransition.Builder()
                            .setActivityType(activity)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                            .build());
        }
        return transitions;
    }

    LocationRequest mLocReq;

    public LocationRequest getLocationRequest() {


        mLocReq = new LocationRequest();
        mLocReq.setInterval(20000);
        mLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocReq.setFastestInterval(10000);
        return mLocReq;
    }


    private int handleActivityRecognition(DetectedActivityResponse response) {

        ActivityRecognitionResult result = response.getActivityRecognitionResult();

        int detectedActivity = result.getMostProbableActivity().getType();
        int detectedActivityConfidence = result.getMostProbableActivity().getConfidence();


        List<DetectedActivity> activities = result.getProbableActivities();

        int footConfidence = 0;
        int bicycleConfidence = 0;
        int unknownConfidence = 0;
        int vehicleConfidence = 0;
        int runningConfidence = 0;
        int walkingConfidence = 0;


        StringBuffer probableActivitiesString = new StringBuffer();
        for (DetectedActivity activity : activities) {
            if (activity.getType() == DetectedActivity.ON_BICYCLE) {
                bicycleConfidence = activity.getConfidence();
                probableActivitiesString.append("ON_BICYCLE " + bicycleConfidence + " ");

            } else if (activity.getType() == DetectedActivity.ON_FOOT) {
                footConfidence = activity.getConfidence();
                probableActivitiesString.append("ON_FOOT " + footConfidence + " ");

            } else if (activity.getType() == DetectedActivity.UNKNOWN) {
                unknownConfidence = activity.getConfidence();
                probableActivitiesString.append("UNKNOWN " + unknownConfidence + " ");

            } else if (activity.getType() == DetectedActivity.IN_VEHICLE) {
                vehicleConfidence = activity.getConfidence();
                probableActivitiesString.append("IN_VEHICLE " + vehicleConfidence + " ");
            } else if (activity.getType() == DetectedActivity.RUNNING) {
                vehicleConfidence = activity.getConfidence();
                probableActivitiesString.append("IN_VEHICLE " + vehicleConfidence + " ");
            } else if (activity.getType() == DetectedActivity.WALKING) {
                vehicleConfidence = activity.getConfidence();
                probableActivitiesString.append("IN_VEHICLE " + vehicleConfidence + " ");
            }
        }


        if (detectedActivity == DetectedActivity.ON_FOOT || detectedActivity == DetectedActivity.ON_BICYCLE
                || detectedActivity == DetectedActivity.IN_VEHICLE) {


            // Rejected: ON_FOOT 42  Probable - [UNKNOWN 25 ON_BICYCLE 20 IN_VEHICLE 12]
            if (detectedActivity == DetectedActivity.ON_FOOT) {
                if (detectedActivityConfidence < 50 || unknownConfidence > 30 ||
                        bicycleConfidence > 10 || vehicleConfidence > 10) {
                    //Mostly likely not reliable to assume ON_FOOT

                    return -1;
                }
            }
            // IN_VEHICLE & ON_BICYCLE activity needs more strict check on probable activities
            // confidences as well.
            else if (detectedActivity == DetectedActivity.ON_BICYCLE) {
                // Accepted: ON_BICYCLE 46 - Probable [UNKNOWN 29 ON_FOOT 2]
                if (detectedActivityConfidence < 80 || unknownConfidence > 30 ||
                        footConfidence > 2 || vehicleConfidence > 10) {
                    //Mostly likely not reliable to assume ON_BICYCLE

                    return -1;
                }

            } else if (detectedActivity == DetectedActivity.IN_VEHICLE) {
                // Accepted: IN_VEHICLE 46 - Probable [UNKNOWN 29 ON_FOOT 2]
                // Rejected: IN_VEHICLE 54 - Sometimes no probable activities obtained.
                if (detectedActivityConfidence < 80 || unknownConfidence > 30 ||
                        footConfidence > 2 || bicycleConfidence > 10) {
                    //Mostly likely not reliable to assume IN_VEHICLE

                    return -1;
                }
            }


        } else {
            //Ignore UNKNOWN, TILTING activity
            if (detectedActivity == DetectedActivity.STILL) {
                //Ignore if other probable activity confidences are found.
                // Accept UNKNOWN if its confidence is less than 30.
                if (bicycleConfidence > 0 || vehicleConfidence > 0 || footConfidence > 0 ||
                        unknownConfidence > 30) {

                    return -1;
                }


            }

            if (detectedActivity == DetectedActivity.RUNNING || detectedActivity == DetectedActivity.WALKING) {
                if (unknownConfidence > 30) {
                    return -1;
                }
            }
        }

        return detectedActivity;


    }

    private void shareLatestStatus(Status s) {


        Double latitude = s.getLatitude();
        Double longitude = s.getLongitude();

        String uri = "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;

        /*String uri = "geo:" + s.getlatitude(0) + ","
                + sydney.longitude + "?q=" + sydney.latitude
                + "," + sydney.longitude;*/

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String ShareSub = "Here is my location, i am currently " + s.getActivity() + " at " + s.getLocationaddress() + " \n Created using On the move ";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, ShareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri + "\n\n" + ShareSub);
        sharingIntent.putExtra(Intent.EXTRA_TITLE, ShareSub);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }


    public void updateWidgetWithStatusUpdate(Status status) {
        SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.appwidget_text, getText(R.string.currently_java_service) + status.getActivity() + getText(R.string.time_java_service) + n.format(status.getDatetime().getTime()) + " \n" + getText(R.string.taptosharenow_java_service));


        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.putExtra("Share_Latest", true);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        ComponentName appWidget = new ComponentName(this, NewAppWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidget, views);
    }


}
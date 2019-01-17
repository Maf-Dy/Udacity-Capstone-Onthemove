package com.mafdy.onthemove.service;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.SnapshotClient;
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.awareness.snapshot.PlacesResponse;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mafdy.onthemove.MainActivity;
import com.mafdy.onthemove.utils.Preferencemanager;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by SBP on 7/6/2018.
 */

public class ActivityLocationIntentService extends IntentService {

    protected static final String TAG = "ActivityLocationIS";


    public ActivityLocationIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {


        if (ActivityTransitionResult.hasResult(intent)) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }


            final ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);


            List<ActivityTransitionEvent> detectedActivities = null;
            if (result != null) {
                detectedActivities = result.getTransitionEvents();
            }

            final SnapshotClient client = Awareness.getSnapshotClient(this);

            client.getDetectedActivity().addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                @Override
                public void onSuccess(final DetectedActivityResponse detectedActivityResponse) {

                    // MainActivity.serviceToActivity.getLocation(result.getTransitionEvents(), detectedActivityResponse);


                    if (ActivityCompat.checkSelfPermission(ActivityLocationIntentService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    if (!Preferencemanager.getPlaceId(ActivityLocationIntentService.this).equals("")) {

                        Task<PlacesResponse> taskPlaces = client.getPlaces();

                        taskPlaces.addOnCompleteListener(new OnCompleteListener<PlacesResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlacesResponse> task) {

                                try {

                                    if(task.isSuccessful()) {
                                        PlacesResponse response = task.getResult();

                                        boolean found = false;

                                        if (response != null) {
                                            for (PlaceLikelihood i : response.getPlaceLikelihoods()) {

                                                if (i.getLikelihood() >= 0.4) {
                                                    if (i.getPlace().getId().equals(Preferencemanager.getPlaceId(ActivityLocationIntentService.this))) {
                                                        found = true;
                                                    }
                                                }
                                            }
                                        }

                                        if (found) {
                                            Preferencemanager.deletePlace(ActivityLocationIntentService.this);
                                            NotificationService.serviceToService.getLocation(result.getTransitionEvents(), detectedActivityResponse, false);
                                        } else {
                                            NotificationService.serviceToService.getLocation(result.getTransitionEvents(), detectedActivityResponse, true);
                                        }
                                    }
                                    else
                                    {
                                        Crashlytics.log("get result of get places task is not successful");
                                        Timber.v("get result of get places task is not successful");
                                        NotificationService.serviceToService.getLocation(result.getTransitionEvents(), detectedActivityResponse, true);
                                    }
                                } catch (Exception e)
                                {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    Timber.v(e);
                                    NotificationService.serviceToService.getLocation(result.getTransitionEvents(), detectedActivityResponse, true);
                                }
                            }
                        });
                    } else {
                        NotificationService.serviceToService.getLocation(result.getTransitionEvents(), detectedActivityResponse, false);
                    }


                }
            });


            Timber.i("activities detected");

           /* for (DetectedActivity da : detectedActivities) {
                Log.i(TAG,
                        da.getType() + " " + da.getConfidence() + "%"
                );
            }*/

            for (ActivityTransitionEvent event : result.getTransitionEvents()) {

            }

        }


    }

    public interface ServiceToActivity {
        public void getLocation(List<ActivityTransitionEvent> activities, DetectedActivityResponse response);
    }

    public interface ServiceToService {
        public void getLocation(List<ActivityTransitionEvent> activities, DetectedActivityResponse response, boolean hasdestination);
    }

}
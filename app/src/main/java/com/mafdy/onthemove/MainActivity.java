package com.mafdy.onthemove;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.amalbit.trail.RouteOverlayView;
import com.amalbit.trail.TrailSupportMapFragment;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.SnapshotClient;
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.mafdy.onthemove.database.AppDatabase;
import com.mafdy.onthemove.database.Status;
import com.mafdy.onthemove.database.StatusViewModel;
import com.mafdy.onthemove.mapsanimator.MapAnimator;

import com.mafdy.onthemove.recyclerview.RecyclerViewAdapter;
import com.mafdy.onthemove.service.ActivityLocationIntentService;
import com.mafdy.onthemove.service.GeocoderAddressService;
import com.mafdy.onthemove.service.NotificationService;
import com.mafdy.onthemove.utils.Preferencemanager;
import com.mafdy.onthemove.utils.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import io.fabric.sdk.android.Fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityLocationIntentService.ServiceToActivity,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener, RecyclerViewAdapter.OnCheckedListener, RecyclerViewAdapter.OnItemClickListener {

    private GoogleMap mMap;
    TrailSupportMapFragment mapFragment;

    FusedLocationProviderClient mFusedLocationClient;

    SnapshotClient mAwarenessSnapchotClient;

    public static ActivityLocationIntentService.ServiceToActivity serviceToActivity;

    private BottomSheetBehavior mBottomSheetBehavior1, mBottomSheetBehavior_share;
    LinearLayout tapactionlayout, tapaction_destination, tapaction_share;
    View bottomSheet, bottomSheet2;
    TextView textView_info, textView_destination, textView_share;
    FloatingActionButton fab_gotolocation;
    RecyclerView mRecyclerView_history;
    public static StatusViewModel mStatusViewModel;

    private int AUTO_COMP_REQ_CODE = 882;
    private boolean mIsBound = false;

    private BroadcastReceiver myBroadcastReceiver;

    ImageButton mDeleteWithFromTo;
    SearchView searchbar;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (EasyPermissions.hasPermissions(this, "android.permission.ACCESS_FINE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE")) {


            if (mBoundService != null) {
                mBoundService.initializeActivityTransitionClient();
                mBoundService.initializeFusedLocationClient();

                if (mMap != null) {
                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {

                            mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                                @Override
                                public void onCameraMove() {
                                    mapFragment.onCameraMove(mMap);
                                }
                            });


                        }
                    });
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }

        } else {

            Toast.makeText(this, R.string.pleaseallowpermissions_java_mainactivity, Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Preferencemanager.getIsFirstOpen(this)) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
            return;
        } else {

        }

        EasyPermissions.requestPermissions(this, "", 11, "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE");

        Fabric.with(this, new Crashlytics());

        serviceToActivity = this;

        //initializeFusedLocationClient();

        //initializeActivityTransitionClient();

        myBroadcastReceiver = new BroadcastReceiver();


        mAwarenessSnapchotClient = Awareness.getSnapshotClient(this.getApplicationContext());


        mapFragment = (TrailSupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tapactionlayout = (LinearLayout) findViewById(R.id.tap_action_layout);
        bottomSheet = findViewById(R.id.bottom_sheet1);
        bottomSheet2 = findViewById(R.id.bottom_sheet2);
        textView_info = findViewById(R.id.infotext);

        tapaction_destination = (LinearLayout) findViewById(R.id.tap_action_layout_destination);

        textView_destination = (TextView) findViewById(R.id.destination_textview);
        textView_share = (TextView) findViewById(R.id.share_textview);
        fab_gotolocation = (FloatingActionButton) findViewById(R.id.fab_gotolocation);
        mRecyclerView_history = (RecyclerView) findViewById(R.id.recyclerview_history);

        mDeleteWithFromTo = (ImageButton) findViewById(R.id.imageButton_deletewithfromto);

        mStatusViewModel = ViewModelProviders.of(this).get(StatusViewModel.class);


        if (savedInstanceState != null) {
            int id = savedInstanceState.getInt("bottomsheetid");
            if (id == bottomSheet2.getId())
                setSwitchBottomSheet(bottomSheet2);
            else if (id == bottomSheet.getId())
                setSwitchBottomSheet(bottomSheet);
        } else {
            setSwitchBottomSheet(bottomSheet);
        }

        //searchbar = (SearchView) findViewById(R.id.toolbar);

       /* searchbar.setIconified(true);
        searchbar.setQueryHint("Search for Place");
        searchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        null;
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MainActivity.this);

                    startActivityForResult(intent, AUTO_COMP_REQ_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Utils.displayAlertDialog1(MainActivity.this, getText(R.string.googleplayserviceserror_java_mainactivity).toString(), getText(R.string.googleservices_wontfunctionproperly_java_mainactivity).toString() + e.getConnectionStatusCode());

                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Utils.displayAlertDialog1(MainActivity.this, getText(R.string.googleplayserviceserror_java_mainactivity).toString(), getText(R.string.googleservicesnotavailable_java_mainactivity).toString() + e.errorCode);


                }

            }
        });
        searchbar.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        null;
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MainActivity.this);

                    startActivityForResult(intent, AUTO_COMP_REQ_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Utils.displayAlertDialog1(MainActivity.this, "Google Play Services Error", "Google Play Services Error, the app won't function properly, check that it is installed, enabled and updated, connection status code: " + e.getConnectionStatusCode());

                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Utils.displayAlertDialog1(MainActivity.this, "Google Play Services Error", "Google Play Services not available, the app won't function properly, error code: " + e.errorCode);


                }
            }
        });*/


        mStatusViewModel.getLatestStatus().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                if (status != null) {

                    SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

                    textView_info.setText(getText(R.string.activity_java_mainactivity) + status.getActivity() + "\n" + getText(R.string.time_java_mainactivity) + n.format(status.getDatetime().getTime()) + "\n" + getText(R.string.address_java_mainactivity) + (status.getLocationaddress() == null || status.getLocationaddress().equals("null") ? getText(R.string.notfound_java_mainactivity) : status.getLocationaddress()));


                    updateUIafterStatus(status);

                    if (getIntent().getIntExtra("bottomsheet", 0) == 2) {

                        shareLatestStatus(status);

                    } else {
                        if (mBoundService != null) {
                            mBoundService.onNewStatus(status);
                        }
                    }

                    if (status.getDestinationname() != null) {
                        if (!status.getDestinationname().equals("")) {
                            textView_destination.setText(getText(R.string.destination_java_mainactivity) + status.getDestinationname());
                        } else {
                            textView_destination.setText(R.string.destinationnotset_java_mainactivity);
                        }
                    } else {
                        textView_destination.setText(R.string.destinationnotset_java_mainactivity);
                    }


                }
            }
        });

        if (getIntent().getBooleanExtra("Share_Latest", false)) {
            //  Status s = getIntent().getParcelableExtra("Status");

            // if (s != null)
            //       shareLatestStatus(s);
            //  else {
            new AsyncTask<Void, Void, Status>() {
                @Override
                protected com.mafdy.onthemove.database.Status doInBackground(Void... voids) {

                    return AppDatabase.getInstance(MainActivity.this).status().getLatestStatus_Service();
                }

                @Override
                protected void onPostExecute(com.mafdy.onthemove.database.Status status) {
                    super.onPostExecute(status);
                    if (status != null)
                        shareLatestStatus(status);
                }
            }.execute();
            // }

        }


        if (savedInstanceState != null) {
            mSavedInstanceState = savedInstanceState;
            double[] latlng = savedInstanceState.getDoubleArray("markerLatLng");
            if (latlng != null) {
                if (latlng.length > 0) {
                    userMarker.position(new LatLng(latlng[0], latlng[1]));

                    if (mStatusViewModel.getLatestStatus().getValue() != null) {
                        updateUIafterStatus(mStatusViewModel.getLatestStatus().getValue());
                    }

                }
            }
        }


    }

    static Bundle mSavedInstanceState = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchbar = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();

        searchbar.setIconified(true);
        searchbar.setQueryHint("Search for Place");
        searchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        null;
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MainActivity.this);

                    startActivityForResult(intent, AUTO_COMP_REQ_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Utils.displayAlertDialog1(MainActivity.this, getText(R.string.googleplayserviceserror_java_mainactivity).toString(), getText(R.string.googleservices_wontfunctionproperly_java_mainactivity).toString() + e.getConnectionStatusCode());

                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Utils.displayAlertDialog1(MainActivity.this, getText(R.string.googleplayserviceserror_java_mainactivity).toString(), getText(R.string.googleservicesnotavailable_java_mainactivity).toString() + e.errorCode);


                }

            }
        });
        searchbar.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        null;
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(MainActivity.this);

                    startActivityForResult(intent, AUTO_COMP_REQ_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Utils.displayAlertDialog1(MainActivity.this, "Google Play Services Error", "Google Play Services Error, the app won't function properly, check that it is installed, enabled and updated, connection status code: " + e.getConnectionStatusCode());

                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Utils.displayAlertDialog1(MainActivity.this, "Google Play Services Error", "Google Play Services not available, the app won't function properly, error code: " + e.errorCode);


                }
            }
        });

        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("Share_Latest", false)) {
            Status s = intent.getParcelableExtra("Status");

            if (s != null)
                shareLatestStatus(s);
            else {
                AsyncTask<Void, Void, Status> execute = new AsyncTask<Void, Void, Status>() {
                    @Override
                    protected com.mafdy.onthemove.database.Status doInBackground(Void... voids) {

                        return AppDatabase.getInstance(MainActivity.this).status().getLatestStatus_Service();
                    }

                    @Override
                    protected void onPostExecute(com.mafdy.onthemove.database.Status status) {
                        super.onPostExecute(status);
                        if (status != null)
                            shareLatestStatus(status);
                    }
                }.execute();
            }

        }

    }

    final Calendar from = Calendar.getInstance();

    final Calendar to = Calendar.getInstance();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTO_COMP_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                Preferencemanager.savePlaceInPref(MainActivity.this, place);

                Toast.makeText(MainActivity.this, R.string.destinationset_java_mainactivity, Toast.LENGTH_SHORT).show();

                try {
                    searchbar.setEnabled(false);
                    searchbar.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    Timber.i(e);
                }

            }
        }
    }

    static int bottomSheetID = 0;

    private void setSwitchBottomSheet(View v) {

        bottomSheetID = v.getId();

        tapaction_share = (LinearLayout) v.findViewById(R.id.tap_action_layout_share);
        textView_share = (TextView) v.findViewById(R.id.share_textview);

        final View fv = v;

        if (fv.getId() == bottomSheet.getId()) {
            ViewTreeObserver viewTreeObserver = tapactionlayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        tapactionlayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        mBottomSheetBehavior1.setPeekHeight(tapactionlayout.getHeight());
                    }
                });
            }
        } else {
            ViewTreeObserver viewTreeObserver = tapaction_share.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        tapaction_share.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        mBottomSheetBehavior1.setPeekHeight(tapaction_share.getHeight());
                    }
                });
            }
        }


        mBottomSheetBehavior1 = BottomSheetBehavior.from(v);
        if (fv.getId() == bottomSheet2.getId())
            mBottomSheetBehavior1.setPeekHeight(tapaction_share.getHeight());
        else
            mBottomSheetBehavior1.setPeekHeight(tapactionlayout.getHeight());
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior1.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    tapactionlayout.setVisibility(View.VISIBLE);
                }

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // tapactionlayout.setVisibility(View.GONE);
                }

                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    // tapactionlayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
        });

        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab_gotolocation.getLayoutParams();
        p.setAnchorId(v.getId());

        fab_gotolocation.setLayoutParams(p);


        if (v.getId() == bottomSheet.getId()) {

            bottomSheet2.setVisibility(View.GONE);
            bottomSheet.setVisibility(View.VISIBLE);

            tapactionlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }

                    if (mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            });

            try {
                // mapFragment.setUpPath(new ArrayList<LatLng>(), mMap, RouteOverlayView.AnimType.PATH);
                MapAnimator.getInstance().animateRoute(mMap, new ArrayList<LatLng>());


            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        } else {

            bottomSheet.setVisibility(View.GONE);
            bottomSheet2.setVisibility(View.VISIBLE);


            TextView fromtv = findViewById(R.id.fromdatetime);
            TextView totv = findViewById(R.id.todatetime);

            mDeleteWithFromTo = (ImageButton) findViewById(R.id.imageButton_deletewithfromto);


            fromtv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

                                    from.set(Calendar.YEAR, year);
                                    from.set(Calendar.MONTH, monthOfYear);
                                    from.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    setShareListenerAfterFilter(from, to);

                                    TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                                                                                            @Override
                                                                                            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

                                                                                                from.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                                                                from.set(Calendar.MINUTE, minute);
                                                                                                from.set(Calendar.SECOND, second);

                                                                                                setShareListenerAfterFilter(from, to);

                                                                                            }
                                                                                        },
                                            from.get(Calendar.HOUR_OF_DAY),
                                            from.get(Calendar.MINUTE),
                                            from.get(Calendar.SECOND), false);
                                    tpd.show(getFragmentManager(), "Timepickerdialog");


                                }
                            },
                            from.get(Calendar.YEAR),
                            from.get(Calendar.MONTH),
                            from.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.show(getFragmentManager(), "Datepickerdialog");
                }
            });

            totv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

                                    to.set(Calendar.YEAR, year);
                                    to.set(Calendar.MONTH, monthOfYear);
                                    to.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    setShareListenerAfterFilter(from, to);

                                    TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                                                                                            @Override
                                                                                            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

                                                                                                to.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                                                                to.set(Calendar.MINUTE, minute);
                                                                                                to.set(Calendar.SECOND, second);

                                                                                                setShareListenerAfterFilter(from, to);

                                                                                            }
                                                                                        },
                                            to.get(Calendar.HOUR_OF_DAY),
                                            to.get(Calendar.MINUTE),
                                            to.get(Calendar.SECOND), false);
                                    tpd.show(getFragmentManager(), "Timepickerdialog");


                                }
                            },
                            to.get(Calendar.YEAR),
                            to.get(Calendar.MONTH),
                            to.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.show(getFragmentManager(), "Datepickerdialog");
                }
            });

            textView_share.setText(R.string.share_current);
            tapaction_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AsyncTask<Void, Void, Status> execute = new AsyncTask<Void, Void, Status>() {
                        @Override
                        protected com.mafdy.onthemove.database.Status doInBackground(Void... voids) {

                            return AppDatabase.getInstance(MainActivity.this).status().getLatestStatus_Service();
                        }

                        @Override
                        protected void onPostExecute(com.mafdy.onthemove.database.Status status) {
                            super.onPostExecute(status);
                            if (status != null)
                                shareLatestStatus(status);
                        }
                    };
                    execute.execute();
                }
            });

        }

    }

    public void setShareListenerAfterFilter(final Calendar from, final Calendar to) {

        try {
            mStatusViewModel.getCurrentAllStatusOrderByDateTime().removeObservers(this);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        LiveData<List<Status>> listLiveDataStatus = mStatusViewModel.getAllStatusOrderByDateTime(from, to);


        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(MainActivity.this, new ArrayList<Status>());

        listLiveDataStatus.observe(this, new Observer<List<Status>>() {
            @Override
            public void onChanged(@Nullable final List<Status> statuses) {


                RecyclerViewSwipeManager swipeManager = new RecyclerViewSwipeManager();
                mRecyclerView_history.setLayoutManager(new LinearLayoutManager(MainActivity.this));


                adapter.updateList(statuses);
                adapter.notifyDataSetChanged();

                adapter.SetOnCheckedListener(MainActivity.this);
                adapter.SetOnItemClickListener(MainActivity.this);

                RecyclerView.Adapter wrappedAdapter = swipeManager.createWrappedAdapter(adapter);


                mRecyclerView_history.setAdapter(wrappedAdapter);

                // disable change animations
                ((SimpleItemAnimator) mRecyclerView_history.getItemAnimator()).setSupportsChangeAnimations(true);
                swipeManager.attachRecyclerView(mRecyclerView_history);


                final List<LatLng> latLngList = new ArrayList<LatLng>();
                int i = 0;
                for (Status ss : statuses) {

                    if (i != 0) {
                        Location l = new Location(""), l2 = new Location("");
                        l.setLatitude(ss.getLatitude());
                        l.setLongitude(ss.getLongitude());
                        l2.setLatitude(statuses.get(i - 1).getLatitude());
                        l2.setLongitude(statuses.get(i - 1).getLongitude());

                        if (l.distanceTo(l2) < 5)
                            continue;

                    }
                    latLngList.add(new LatLng(ss.getLatitude(), ss.getLongitude()));
                    i++;
                }


                try {
                    if (latLngList.size() > 1
                            ) {
                        if ((mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_COLLAPSED ||
                                mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_HIDDEN)) {
                            LatLngBounds bounds = new LatLngBounds(latLngList.get(0), latLngList.get(latLngList.size() - 1));

                            // mapFragment.setUpPath(latLngList, mMap, RouteOverlayView.AnimType.PATH);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
                        }

                        MapAnimator.getInstance().animateRoute(mMap, latLngList);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    if (latLngList.size() > 1
                            ) {

                        if ((mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_COLLAPSED ||
                                mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_HIDDEN)) {
                            LatLngBounds bounds = new LatLngBounds(latLngList.get(latLngList.size() - 1), latLngList.get(0));


                            // mapFragment.setUpPath(latLngList, mMap, RouteOverlayView.AnimType.PATH);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
                        }

                        MapAnimator.getInstance().animateRoute(mMap, latLngList);
                    }


                }

                if (statuses.size() > 0) {
                    textView_share.setText(R.string.Share_History);
                    tapaction_share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (bottomSheet2.getVisibility() == View.GONE) {
                                setSwitchBottomSheet(bottomSheet2);
                            } else {


                                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                                    @Override
                                    public void onSnapshotReady(Bitmap bitmap) {
                                        shareLatestStatus_list(statuses, bitmap);
                                    }
                                });


                            }
                        }
                    });

                    mDeleteWithFromTo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.deletehistory_java_mainactivity)
                                    .setMessage(R.string.deletehistory_areusure_java_mainactivity)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mStatusViewModel.delete(from, to);
                                        }
                                    });
                            build.show();
                        }
                    });
                } else {
                    textView_share.setText(R.string.Share_current);
                    tapaction_share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AsyncTask<Void, Void, Status> execute = new AsyncTask<Void, Void, Status>() {
                                @Override
                                protected com.mafdy.onthemove.database.Status doInBackground(Void... voids) {

                                    return AppDatabase.getInstance(MainActivity.this).status().getLatestStatus_Service();
                                }

                                @Override
                                protected void onPostExecute(com.mafdy.onthemove.database.Status status) {
                                    super.onPostExecute(status);
                                    if (status != null)
                                        shareLatestStatus(status);
                                }
                            };
                            execute.execute();
                        }
                    });
                    mDeleteWithFromTo.setOnClickListener(null);
                }

            }
        });


    }

    @Override
    public void onBackPressed() {


        if (bottomSheet2.getVisibility() == View.VISIBLE) {
            setSwitchBottomSheet(bottomSheet);

            mStatusViewModel.getCurrentAllStatusOrderByDateTime().removeObservers(this);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng sydney = new LatLng(-34, 151);

        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);

        mMap.setTrafficEnabled(false);

        mMap.setOnMapLongClickListener(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
                else{
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        mapFragment.onCameraMove(mMap);
                    }
                });


            }
        });
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        if (mSavedInstanceState != null) {
            double[] latlng = mSavedInstanceState.getDoubleArray("markerLatLng");
            if (latlng != null) {
                if (latlng.length > 0) {
                    userMarker.position(new LatLng(latlng[0], latlng[1]));

                    if (mStatusViewModel.getLatestStatus().getValue() != null) {
                        updateUIafterStatus(mStatusViewModel.getLatestStatus().getValue());
                    }

                }
            }
            mSavedInstanceState = null;
        }

    }

    @Override
    public void getLocation(final List<ActivityTransitionEvent> activities, final DetectedActivityResponse response) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        final Task<LocationAvailability> t2 = mFusedLocationClient.getLocationAvailability();
        t2.addOnCompleteListener(new OnCompleteListener<LocationAvailability>() {
            @Override
            public void onComplete(@NonNull Task<LocationAvailability> task) {
                if (task.getResult().isLocationAvailable()) {

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

                               /* String activityRecognitionType = "";

                                int activityRecognitionTypeint = handleActivityRecognition(response);
                                if (activityRecognitionTypeint != -1) {
                                    if (activityRecognitionTypeint == DetectedActivity.IN_VEHICLE)
                                        activityRecognitionType = "In Vehicle";
                                    else if (activityRecognitionTypeint == DetectedActivity.ON_BICYCLE)
                                        activityRecognitionType = "On Bicycle";
                                    else if (activityRecognitionTypeint == DetectedActivity.ON_FOOT)
                                        activityRecognitionType = "On Foot";
                                    else if (activityRecognitionTypeint == DetectedActivity.RUNNING)
                                        activityRecognitionType = "Running";
                                    else if (activityRecognitionTypeint == DetectedActivity.STILL)
                                        activityRecognitionType = "Still";
                                    else if (activityRecognitionTypeint == DetectedActivity.WALKING)
                                        activityRecognitionType = "Walking";
                                }

                                if (activityRecognitionType.equals(activity)) {

                                }*/


                                Status s = new Status();
                                s.setActivity(activity);
                                s.setDatetime(Calendar.getInstance());
                                s.setDestinationname("");
                                s.setLatitude(c.getLatitude());
                                s.setLongitude(c.getLongitude());
                                s.setLocationaccuracy(c.getAccuracy());
                                s.setTransition(type);

                                updateUIafterStatus(s);

                                mStatusViewModel.insert(s);

                                /*startGeocoderIntentService();

                                if (s.getActivity().equals("Still")) {
                                    mLocReq.setFastestInterval(30000);
                                    mLocReq.setInterval(60000);
                                } else {
                                    mLocReq.setFastestInterval(10000);
                                    mLocReq.setInterval(20000);
                                }*/


                            }

                        }
                    });


                }
            }
        });


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        doBindService();

        //initializeActivityTransitionClient();
        //initializeFusedLocationClient();

        // TODO call request location and activity from service here and update ui status as well according to service


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mIsBound) {
            doUnbindService();
        }


        // mActivityRecognitionClient.removeActivityTransitionUpdates(getIntentServicePendingIntent());
        // mFusedLocationClient.removeLocationUpdates(callback);

    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver,
                new IntentFilter(NotificationService.BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {

            if (userMarker.getPosition().latitude != 0)
                outState.putDoubleArray("markerLatLng", new double[]{userMarker.getPosition().latitude, userMarker.getPosition().longitude});

            if (bottomSheetID != 0)
                outState.putInt("bottomsheetid", bottomSheetID);

        } catch (Exception e)
        { e.printStackTrace();}

    }

    final MarkerOptions userMarker = new MarkerOptions();

    public void updateUIafterStatus(final Status s) {
        final LatLng sydney = new LatLng(s.getLatitude(), s.getLongitude());
        if (mMap != null && s != null) {
            mMap.clear();

            userMarker.position(sydney);
            userMarker.title("you");

            mMap.addMarker(userMarker);


            SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

            final String finalActivity = s.getActivity();


            fab_gotolocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));
                }
            });

            tapaction_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bottomSheet2.getVisibility() == View.GONE) {
                        setSwitchBottomSheet(bottomSheet2);
                    } else {

                        shareLatestStatus(s);

                    }
                }
            });
        }


    }

    public void updateUIafterConfigurationChange() {

        if (mMap != null && userMarker != null) {
            mMap.clear();

            userMarker.title("you");

            mMap.addMarker(userMarker);


            fab_gotolocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 12));
                }
            });


        }


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

        String activity = s.getActivity();
        String locationaddress = s.getLocationaddress() == null ? "" : (s.getLocationaddress().equals("") ? "" : s.getLocationaddress());
        String destinationname = s.getDestinationname() == null ? "" : (s.getDestinationname().equals("") ? "" : s.getDestinationname());

        String ShareSub = "Here is my location, i am currently " + activity + (locationaddress.equals("") ? " " : " at " + locationaddress)
                + (destinationname.equals("") ? " " : " going to: " + destinationname) + " \n Created using On the move ";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, ShareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri + "\n\n" + ShareSub);
        sharingIntent.putExtra(Intent.EXTRA_TITLE, ShareSub);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }

    private void shareLatestStatus_list(List<Status> statusList, Bitmap bitmap) {


        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "my path", null);
        Uri bitmapUri = Uri.parse(bitmapPath);


        String fullstring = "";

        String activity_iterator = "";

        for (Status s : statusList) {


            if (activity_iterator.equals(s.getActivity()))
                continue;
            else {

                Double latitude = s.getLatitude();
                Double longitude = s.getLongitude();

                String uri = "http://maps.google.com/maps?saddr=" + latitude + "," + longitude;

                String ShareSub = "";

                if (activity_iterator.equals(""))
                    ShareSub = "Started " + s.getActivity() + " at " + s.getLocationaddress();
                else {
                    ShareSub = "Stopped " + activity_iterator + " and Started " + s.getActivity() + " at " + s.getLocationaddress();
                }

                fullstring += uri + "\n" + ShareSub + "\n\n";

                activity_iterator = s.getActivity();

            }


        }

        fullstring += "  Created using On the move ";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        //sharingIntent.setType("text/plain");
        sharingIntent.setType("*/*");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "History");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, fullstring);
        sharingIntent.putExtra(Intent.EXTRA_TITLE, "History");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }


    @Override
    public void onChecked(View view, boolean isChecked, int position, Status model) {

    }

    @Override
    public void onItemClick(View view, int position, Status model) {

    }


    private NotificationService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            NotificationService.LocalBinder binder = (NotificationService.LocalBinder) service;
            mBoundService = binder.getService();

            if (mBoundService != null) {
                mBoundService.startService();
            }
            mIsBound = true;

            // Tell the user about this for our demo.
            Timber.d("service connected");
        }

        public void onServiceDisconnected(ComponentName className) {

            mBoundService = null;
            mIsBound = false;
            Timber.d("service disconnected");
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation
        // that we know will be running in our own process (and thus
        // won't be supporting component replacement by other
        // applications).
        boolean chkbind = bindService(new Intent(MainActivity.this, NotificationService.class),
                mConnection,
                Context.BIND_AUTO_CREATE);

        Timber.d(chkbind + "result of binding");
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }




    public class BroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get some results from service here

            if (intent != null) {
                Status status = intent.getParcelableExtra(NotificationService.EXTRA_STATUS);
                if (status != null) {

                    SimpleDateFormat n = new SimpleDateFormat("hh:mm:ss aa , dd-MM-yyyy");

                    textView_info.setText(getText(R.string.activity_java_mainactivity) + status.getActivity() + "\n" + getText(R.string.time_java_mainactivity) + n.format(status.getDatetime().getTime()) + "\n" + getText(R.string.address_java_mainactivity) + (status.getLocationaddress() == null || status.getLocationaddress().equals("null") ? getText(R.string.notfound_java_mainactivity) : status.getLocationaddress()));


                    updateUIafterStatus(status);
                }
            }
        }
    }
}

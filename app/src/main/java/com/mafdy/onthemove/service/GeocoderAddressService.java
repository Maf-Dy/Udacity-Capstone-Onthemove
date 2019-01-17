package com.mafdy.onthemove.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.mafdy.onthemove.R;
import com.mafdy.onthemove.database.AppDatabase;
import com.mafdy.onthemove.database.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class GeocoderAddressService extends IntentService {


    public GeocoderAddressService() {
        super("GeocoderAddressService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {


            List<Status> statusList = AppDatabase.getInstance(this).status().getStatusWithEmptyAddress();

            for(Status s : statusList) {


                Geocoder geocoder = new Geocoder(this, Locale.getDefault());



                List<Address> addresses = null;

                try {
                    addresses = geocoder.getFromLocation(
                            s.getLatitude(),
                            s.getLongitude(),
                            1);
                } catch (IOException ioException) {

                    Crashlytics.logException(ioException);

                } catch (IllegalArgumentException illegalArgumentException) {
                    Crashlytics.logException(illegalArgumentException);

                }


                if (addresses == null || addresses.size() == 0) {

                    s.setLocationaddress("Not found");
                    AppDatabase.getInstance(this).status().updateStatus(s);


                } else {
                    Address address = addresses.get(0);
                    ArrayList<String> addressFragments = new ArrayList<String>();
                    String finaladdress = "";

                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));

                        finaladdress += address.getAddressLine(i) + " , ";

                    }

                    s.setLocationaddress(finaladdress);
                    AppDatabase.getInstance(this).status().updateStatus(s);


                }

            }


        }
    }

}

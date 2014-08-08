/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geofence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.geofence.GeofenceRequester.REQUEST_TYPE;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;

/**
 * UI handler for the Location Services Geofence sample app. When registering
 * geofences, send the geofence to Location Services. Also allow removing the
 * geofence. The menu allows you to delete the geofences stored in persistent
 * memory.
 */
public class MainActivity extends Activity {

    private REQUEST_TYPE mRequestType;

    // Store a list of geofences to add
    List<Geofence> mCurrentGeofences;

    private GeofenceRequester mGeofenceRequester;
    private Geofence mCurrentGeofence;

    // Store the list of geofences to remove
    private List<String> mGeofenceIdsToRemove;

    // Geofence stats
    public String GEOFENCE_ID = "1";
    private Double GEOFENCE_LAT = 37.786564;
    private Double GEOFENCE_LONG = -122.405494;
    private Float GEOFENCE_RADIUS = 5000f;

    public static final String APPTAG = "Geofence Detection";

    /*
     * Define a request code to send to Google Play services. This code is
     * returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentGeofences = new ArrayList<Geofence>();
        mGeofenceRequester = new GeofenceRequester(this);
        setContentView(R.layout.activity_main);
    }

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed()
     * in GeofenceRemover and GeofenceRequester may call
     * startResolutionForResult() to start an Activity that handles Google Play
     * services problems.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
        case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            switch (resultCode) {
            // If Google Play services resolved the problem
            case Activity.RESULT_OK:
                if (REQUEST_TYPE.ADD == mRequestType) {
                    mGeofenceRequester.setInProgressFlag(false);
                    mGeofenceRequester.addGeofences(mCurrentGeofences);
                } else if (REQUEST_TYPE.REMOVE == mRequestType) {
                    mGeofenceRequester.setInProgressFlag(false);
                    mGeofenceRequester.removeGeofencesById(mGeofenceIdsToRemove);
                }
                break;
            default:
                // Handle bad resolution
            }
        default:
            break;
        }
    }

    /**
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            // Handle success
            return true;
        } else {
            // Handle the error
            return false;
        }
    }

    /**
     * Called when the user clicks the "Remove geofence" button. Remove the
     * geofence by creating a List of geofences to remove and sending it to
     * Location Services. The List contains the id of geofence 1 ("1"). The
     * removal happens asynchronously; Location Services calls
     * onRemoveGeofencesByPendingIntentResult() (implemented in the current
     * Activity) when the removal is done.
     */
    public void onUnregisterClicked(View view) {

        // Don't remove the geofence is Google Play is unavailabe, or if there
        // are none already registered
        if (!servicesConnected() || mCurrentGeofences.isEmpty()) {
            return;
        }

        mGeofenceIdsToRemove = Collections.singletonList(GEOFENCE_ID);
        mCurrentGeofences.clear();
        mCurrentGeofence = null;

        // Start the request. Fail if there's already a request in progress
        try {
            mGeofenceRequester.removeGeofencesById(mGeofenceIdsToRemove);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            // Handle that previous request hasn't finished.
        }
    }

    /**
     * Called when the user clicks the "Register geofences" button. Get the
     * geofence parameters for each geofence and add them to a List. Create the
     * PendingIntent containing an Intent that Location Services sends to this
     * app's broadcast receiver when Location Services detects a geofence
     * transition. Send the List and the PendingIntent to Location Services.
     */
    public void onRegisterClicked(View view) {

        // Don't add the geofence is Google Play is unavailabe, or if there is
        // already one registered
        if (!servicesConnected() || !mCurrentGeofences.isEmpty()) {
            return;
        }

        mCurrentGeofence = new Geofence.Builder().setRequestId(GEOFENCE_ID)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(GEOFENCE_LAT, GEOFENCE_LONG, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE).build();
        mCurrentGeofences.add(mCurrentGeofence);

        try {
            mGeofenceRequester.addGeofences(mCurrentGeofences);
        } catch (UnsupportedOperationException e) {
            // Handle that previous request hasn't finished.
        }
    }

}

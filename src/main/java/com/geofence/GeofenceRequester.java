package com.geofence;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

/**
 * Class for connecting to Location Services and requesting geofences. <b> Note:
 * Clients must ensure that Google Play services is available before requesting
 * geofences. </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to
 * check.
 * 
 * 
 * To use a GeofenceRequester, instantiate it and call AddGeofence(). Everything
 * else is done automatically.
 * 
 */
public class GeofenceRequester implements OnAddGeofencesResultListener, OnRemoveGeofencesResultListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    public enum REQUEST_TYPE {
        ADD, REMOVE
    }

    private final Activity mActivity;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mCurrentGeofences;
    private List<String> mCurrentGeofenceIds;
    private LocationClient mLocationClient;
    private REQUEST_TYPE mRequestType;
    private boolean mInProgress;

    public GeofenceRequester(Activity activityContext) {
        mActivity = activityContext;
        mGeofencePendingIntent = null;
        mCurrentGeofenceIds = null;
        mLocationClient = null;
        mRequestType = null;
        mInProgress = false;
    }

    public void setInProgressFlag(boolean flag) {
        mInProgress = flag;
    }

    public boolean getInProgressFlag() {
        return mInProgress;
    }

    public void addGeofences(List<Geofence> geofences) throws UnsupportedOperationException {
        mCurrentGeofences = (ArrayList<Geofence>) geofences;

        if (!mInProgress) {
            mInProgress = true;
            /*
             * If a failure occurs, onActivityResult is eventually called, and
             * it needs to know what type of request was in progress.
             */
            mRequestType = REQUEST_TYPE.ADD;
            getLocationClient().connect();

        } else {
            throw new UnsupportedOperationException();
        }
    }

    private GooglePlayServicesClient getLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(mActivity, this, this);
        }
        return mLocationClient;
    }

    /**
     * Remove the geofences in a list of geofence IDs. To remove all current
     * geofences associated with a request, you can also call
     * removeGeofencesByIntent.
     * 
     * @param geofenceIds
     *            A List of geofence IDs
     */
    public void removeGeofencesById(List<String> geofenceIds) throws IllegalArgumentException,
            UnsupportedOperationException {
        if ((null == geofenceIds) || (geofenceIds.size() == 0)) {
            throw new IllegalArgumentException();
        } else {
            // If a removal request is not already in progress, continue
            if (!mInProgress) {
                mCurrentGeofenceIds = geofenceIds;
                mRequestType = REQUEST_TYPE.REMOVE;
                // The request is not complete until onConnected() or
                // onConnectionFailure() is called
                getLocationClient().connect();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        if (LocationStatusCodes.SUCCESS == statusCode) {
            // Handle success
        } else {
            // Handle error
        }
        mInProgress = false;
        getLocationClient().disconnect();
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
        if (LocationStatusCodes.SUCCESS == statusCode) {
            // Handle success
        } else {
            // Handle error
        }
        mInProgress = false;
        getLocationClient().disconnect();
    }

    /*
     * Called by Location Services once the location client is connected.
     */
    @Override
    public void onConnected(Bundle arg0) {
        Log.d(MainActivity.APPTAG, mActivity.getString(R.string.connected));
        switch (mRequestType) {
        case ADD:
            mGeofencePendingIntent = createRequestPendingIntent();
            mLocationClient.addGeofences(mCurrentGeofences, mGeofencePendingIntent, this);
            break;
        case REMOVE:
            mLocationClient.removeGeofences(mCurrentGeofenceIds, this);
            break;
        default:
            break;

        }
    }

    @Override
    public void onDisconnected() {
        mInProgress = false;
        Log.d(MainActivity.APPTAG, mActivity.getString(R.string.disconnected));
        mLocationClient = null;
    }

    /**
     * @return A PendingIntent for the IntentService that handles geofence
     *         transitions.
     */
    private PendingIntent createRequestPendingIntent() {
        if (null != mGeofencePendingIntent) {
            return mGeofencePendingIntent;
        } else {
            // Create an Intent pointing to the ReceiveTransitionsIntentService
            Intent intent = new Intent(mActivity, ReceiveTransitionsIntentService.class);
            return PendingIntent.getService(mActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
        if (connectionResult.hasResolution()) {
            try {
                // This will be handled by Activity.onActivityResult
                connectionResult
                        .startResolutionForResult(mActivity, MainActivity.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            // Handle error
        }
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int arg0, PendingIntent arg1) {
        // TODO Auto-generated method stub
    }
}

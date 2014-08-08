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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Storage for geofence values, implemented in SharedPreferences. For a
 * production app, use a content provider that's synced to the web or loads
 * geofence data based on current location.
 */
public class SimpleGeofenceStore {
    // Keys for flattened geofences stored in SharedPreferences
    public static final String KEY_LATITUDE = "com.example.android.geofence.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "com.example.android.geofence.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "com.example.android.geofence.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION = "com.example.android.geofence.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE = "com.example.android.geofence.KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys
    public static final String KEY_PREFIX = "com.example.android.geofence.KEY";
    /*
     * Invalid values, used to test geofence storage when retrieving geofences
     */
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;
    // The SharedPreferences object in which geofences are stored
    private final SharedPreferences mPrefs;

    private static final String SHARED_PREFERENCE_NAME = MainActivity.class.getSimpleName();

    public SimpleGeofenceStore(Context context) {
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public SimpleGeofence getGeofence(String id) {
        double lat = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LATITUDE), INVALID_FLOAT_VALUE);
        double lng = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), INVALID_FLOAT_VALUE);
        float radius = mPrefs.getFloat(getGeofenceFieldKey(id, KEY_RADIUS), INVALID_FLOAT_VALUE);
        long expirationDuration = mPrefs.getLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), INVALID_LONG_VALUE);
        int transitionType = mPrefs.getInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), INVALID_INT_VALUE);

        // If none of the values is incorrect, return the object
        if (lat != INVALID_FLOAT_VALUE && lng != INVALID_FLOAT_VALUE && radius != INVALID_FLOAT_VALUE
                && expirationDuration != INVALID_LONG_VALUE && transitionType != INVALID_INT_VALUE) {
            return new SimpleGeofence(id, lat, lng, radius, expirationDuration, transitionType);
        } else {
            return null;
        }
    }

    public void setGeofence(String id, SimpleGeofence geofence) {

        /*
         * Get a SharedPreferences editor instance. Among other things,
         * SharedPreferences ensures that updates are atomic and non-concurrent
         */
        Editor editor = mPrefs.edit();

        editor.putFloat(getGeofenceFieldKey(id, KEY_LATITUDE), (float) geofence.getLatitude());
        editor.putFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), (float) geofence.getLongitude());
        editor.putFloat(getGeofenceFieldKey(id, KEY_RADIUS), geofence.getRadius());
        editor.putLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), geofence.getExpirationDuration());
        editor.putInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), geofence.getTransitionType());
        editor.commit();
    }

    public void clearGeofence(String id) {
        Editor editor = mPrefs.edit();
        editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
        editor.remove(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION));
        editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
        editor.commit();
    }

    /**
     * Create a field key composed of the Geofence id and given fieldname. This
     * allows for storing multiple geofences in the future.
     * 
     * @return The full key name of a value in SharedPreferences
     */
    private String getGeofenceFieldKey(String id, String fieldName) {

        return KEY_PREFIX + id + "_" + fieldName;
    }
}

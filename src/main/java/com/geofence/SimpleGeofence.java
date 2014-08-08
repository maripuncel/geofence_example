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

import com.google.android.gms.location.Geofence;

/**
 * A single Geofence object, defined by its center (latitude and longitude
 * position) and radius. This is based off the example given in the Android
 * developer training materials.
 */
public class SimpleGeofence {
    private final String mId;
    private final double mLatitude;
    private final double mLongitude;
    private final float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;

    public SimpleGeofence(String geofenceId, double latitude, double longitude, float radius, long expiration,
            int transition) {
        this.mId = geofenceId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
    }

    public String getId() {
        return mId;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public float getRadius() {
        return mRadius;
    }

    public long getExpirationDuration() {
        return mExpirationDuration;
    }

    public int getTransitionType() {
        return mTransitionType;
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     * 
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        return new Geofence.Builder().setRequestId(getId()).setTransitionTypes(mTransitionType)
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(mExpirationDuration).build();
    }
}

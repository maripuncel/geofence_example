This guide walks you through the process of adding geofencing to your Android application. I found other examples to have too many things built on top, so the core geofencing functionality was interwoven with extras. This is a *very* barebones implementation, and much easier to build off of.

# What this builds

This is an application that sends a user push notifications upon exiting and entering a predefined [Geofence](http://developer.android.com/reference/com/google/android/gms/location/Geofence.html). Geofences use Android Location Services to define an area around which to monitor a mobile device. According to Google:

> For each geofence, you can ask [LocationServices](https://developer.android.com/reference/com/google/android/gms/location/LocationServices.html) to send you entrance events or exit events or both. You can also limit the duration of a geofence by specifying an expiration duration in milliseconds. After the geofence expires, LocationServices automatically removes it.

The UI provides buttons to start and stop geofence monitoring. 

# Build the apk

To create the apk, run `mvn package` inside your repository directory. The apk will be created in `/target/geofence-example-0.1.0.apk`.

# Let's do this

## Adding permissions

Android geofencing requires a certain level of permissions, so to your `AndroidManifest.xml` add:
```xml
<manifest>
	...
	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
	...
	<application>
		...
		<meta-data			
		android:name="com.google.android.gms.version"
		android:value="4242000" />
		...
	</application>
	...
</manifest>
```

## Checking for Google Play Services

The Geofence API is only available if Google Play is installed. When processing register and unregister button clicks, have this check in place before trying to make any calls to add or a remove a geofence.
```java
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
``` 

## Storing geofences using SharedPreferences
I used an identical storage system as in the Geofencing developer docs, two classes [`SimpleGeofence`](src/main/java/com/geofence/SimpleGeofence.java), which really just wrapper for the Android Geofence, and [SimpleGeofenceStore](src/main/java/com/geofence/SimpleGeofenceStore.java), which interfaces with [SharedPreferences](http://developer.android.com/reference/android/content/SharedPreferences.html) to store geofence attributes. You can't use Geofences for persistant storage of geofence data - this is a lightweight workaround. A more sophisticated implementation would use a content provider to load geofence data based on current location.

## Create a Geofence

SimpleGeofence objects are created in `MainActivity.java` after a user clicks the register button. `SimpleGeofence` in turn uses the Geofence Builder.
```java
public void onRegisterClicked(View view) {

    if (!servicesConnected()) {
        return;
    }

    mUIGeofence = new SimpleGeofence(GEOFENCE_ID, GEOFENCE_LAT, GEOFENCE_LONG, GEOFENCE_RADIUS, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

    mPrefs.setGeofence(GEOFENCE_ID, mUIGeofence);

    mCurrentGeofences.add(mUIGeofence.toGeofence());

    try {
        mGeofenceRequester.addGeofences(mCurrentGeofences);
    } catch (UnsupportedOperationException e) {
        // Handle that previous request hasn't finished.
    }
}
```
['GeofenceRequester'](src/main/java/com/geofence/GeofenceRequester.java), which defines the addGeofences method, handles connecting to the Android LocationClient and requests to add and remove geofences. Once a connection has been established, a geofence is added, along with the [PendingIntent](http://developer.android.com/reference/android/app/PendingIntent.html) that should receive geofence transitions.
```java
@Override
public void onConnected(Bundle arg0) {
    Log.d(GeofenceUtils.APPTAG, mActivity.getString(R.string.connected));
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

private PendingIntent createRequestPendingIntent() {
    if (null != mGeofencePendingIntent) {
        return mGeofencePendingIntent;
    } else {
        // Create an Intent pointing to the ReceiveTransitionsIntentService
        Intent intent = new Intent(mActivity, ReceiveTransitionsIntentService.class);
        return PendingIntent.getService(mActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
```

## Monitor when a device enters or exits a geofence

The [`ReceiveTransitionsIntentService`]((src/main/java/com/geofence/ReceiveTransitionsIntentService) is used to monitor geofence transitions. You will need to declare this in your manifest like so:
```xml
<application>
	...
	<service
		android:name=".ReceiveTransitionsIntentService"
		android:exported="false" >
	</service>
	...
</application>
```
The service is responsible for handling all Intents related to registered Geofences. You will receive Intents only for those transitions that you specified when declaring the Geofence - in this case for entering and exiting. Here is the onHandleIntent method:
```java
@Override
protected void onHandleIntent(Intent intent) {
    if (LocationClient.hasError(intent)) {
        // Handle error
    } else {
        int transition = LocationClient.getGeofenceTransition(intent);

        if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
            String transitionType = getTransitionString(transition);

            // Send a notification containing the transition type
            NotificationUtils.sendNotification(this, 1, R.drawable.ic_notification, transitionType, transitionType,
                    transitionType);

            Log.d(MainActivity.APPTAG, getString(R.string.geofence_transition_notification_title, transitionType));
        } else {
            // Handle invalid transition
        }
    }
}
```

## Stop monitoring a Geofence

To stop monitoring a Geofence, an identical flow is followed as with adding a Geofence. The onUnregisterClicked method is called in `MainActivity.java`, which deletes the geofence from storage and calls on the GeofenceRequester to unregister. The GeofenceRequester will connect to the LocationClient and remove the geofence.
```java
public void onUnregisterClicked(View view) {

    if (!servicesConnected()) {
        return;
    }

    mGeofenceIdsToRemove = Collections.singletonList(GEOFENCE_ID);

    mPrefs.clearGeofence(GEOFENCE_ID);

    mCurrentGeofences.clear();

    // Start the request. Fail if there's already a request in progress
    try {
        mGeofenceRequester.removeGeofencesById(mGeofenceIdsToRemove);
    } catch (IllegalArgumentException e) {
        e.printStackTrace();
    } catch (UnsupportedOperationException e) {
        // Handle that previous request hasn't finished.
    }
}
```

# Other resources

The Google developer site has an [imperfect example](http://developer.android.com/training/location/geofencing.html). I found [Advanced Android Application Development](http://my.safaribooksonline.com/book/programming/android/9780133892420) to be much more helpful.
package com.geofence;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that
 * triggered the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    /**
     * Handles incoming intents
     * 
     * @param intent
     *            The Intent sent by Location Services. This Intent is provided
     *            to Location Services (inside a PendingIntent) when you call
     *            addGeofences()
     */
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

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * 
     * @param transitionType
     *            A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

        case Geofence.GEOFENCE_TRANSITION_ENTER:
            return getString(R.string.geofence_transition_entered);

        case Geofence.GEOFENCE_TRANSITION_EXIT:
            return getString(R.string.geofence_transition_exited);

        default:
            return getString(R.string.geofence_transition_unknown);
        }
    }
}

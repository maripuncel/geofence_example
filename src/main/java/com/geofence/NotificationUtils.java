package com.geofence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * This class sends basic notifications.
 * 
 * @author mari.miyachi
 * 
 */
public final class NotificationUtils {

    public static void sendNotification(Context context, int id, int icon, String tickerText, String contentTitle,
            String contentText, Class<?> clazz, long when, Bundle notificationBundle) {

        Notification notification = createNotification(context, id, icon, tickerText, clazz, when, notificationBundle);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.setLatestEventInfo(context, contentTitle, contentText, notification.contentIntent);

        sendNotification(context, id, notification);
    }

    private static Notification createNotification(Context context, int id, int statusBarIcon, String tickerText,
            Class<?> clazz, long when, Bundle notificationBundle) {
        Notification notification = new Notification(statusBarIcon, tickerText, when);

        Intent notificationIntent = clazz != null ? new Intent(context, clazz) : new Intent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (notificationBundle != null) {
            notificationIntent.replaceExtras(notificationBundle);
        }
        notification.contentIntent = PendingIntent.getActivity(context, id, notificationIntent, 0);

        return notification;
    }

    public static void sendNotification(Context context, int id, Notification notification) {
        NotificationManager NOTIFICATION_MANAGER = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NOTIFICATION_MANAGER.notify(id, notification);
    }
}

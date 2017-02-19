package com.josephplattenberger.jotit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Bundle bundle = intent.getExtras();
        int noteID = bundle.getInt("note_id");
        String subject = bundle.getString("note_subject");
        String mNoteBody = bundle.getString("note_body");
        int mBackColor = bundle.getInt("backColor");
        int mTextColor = bundle.getInt("textColor");
        int mHighColor = bundle.getInt("highColor");

        Intent myIntent = new Intent(context, EditNoteActivity.class);
        myIntent.putExtra("note_subject", subject);
        myIntent.putExtra("backColor", mBackColor);
        myIntent.putExtra("textColor", mTextColor);
        myIntent.putExtra("highColor", mHighColor);

        int requestID = (int) System.currentTimeMillis();

        PendingIntent contentIntent = PendingIntent.getActivity(context, requestID, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification mNotification = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(subject)
                .setContentText(mNoteBody)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        mNotificationManager.notify(requestID, mNotification);
    }
}
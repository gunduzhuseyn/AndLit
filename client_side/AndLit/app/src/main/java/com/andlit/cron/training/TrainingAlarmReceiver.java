package com.andlit.cron.training;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.andlit.R;
import com.andlit.face.FaceRecognizerSingleton;
import com.andlit.settings.SettingsDefinedKeys;

import java.util.Calendar;

import static com.andlit.cron.CronMaster.TRAINING_CODE;

public class TrainingAlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire(30*60*1000L /* operation shouldn't last more than 30 minutes*/);
        NotificationCompat.Builder mBuilder = new
                NotificationCompat.Builder(context)
                .setContentTitle("AndLit app training")
                .setContentText("Training of AndLit face recognizer started!")
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.icon);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        int currentApiVersion = Build.VERSION.SDK_INT;

        Notification notification;
        if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = mBuilder.build();
        } else {
            notification = mBuilder.getNotification();
        }
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.FLAG_AUTO_CANCEL;
        notification.flags = Notification.DEFAULT_LIGHTS
                | Notification.FLAG_AUTO_CANCEL;

        if(notificationManager != null)
            notificationManager.notify(0x1111, notification);
        FaceRecognizerSingleton frs = new FaceRecognizerSingleton(context);
        if(frs.mustTrainConditions()) {
            mBuilder = new
                    NotificationCompat.Builder(context)
                    .setContentTitle("AndLit training finished!")
                    .setContentText("AndLit face recognizer trained successfully!")
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.icon);
            frs.trainModel();
        }
        else {
            mBuilder = new
                    NotificationCompat.Builder(context)
                    .setContentTitle("AndLit training aborted!")
                    .setContentText("Training wouldn\'t be helpful to increase accuracy!")
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.icon);
        }

        currentApiVersion = Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = mBuilder.build();
        } else {
            notification = mBuilder.getNotification();
        }
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.FLAG_AUTO_CANCEL;
        notification.flags = Notification.DEFAULT_LIGHTS
                | Notification.FLAG_AUTO_CANCEL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notification.category = Notification.CATEGORY_EVENT;
        if(notificationManager != null)
            notificationManager.notify(0x1111, notification);
        wl.release();
    }

    public boolean setAlarm(Context context) {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TrainingAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, TRAINING_CODE, i, 0);
        if(am == null)
            return false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String trainingValue = sharedPreferences.getString(SettingsDefinedKeys.TRAINING_FREQUENCY, "No Selection");
        String [] entries = context.getResources().getStringArray(R.array.TrainingFrequencyValues);
        if (trainingValue.equals(entries[0]))
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pi);
        else if (trainingValue.equals(entries[1]))
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY * 7, pi);
        else
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY * 30, pi);
        return true;
    }

    public boolean cancelAlarm(Context context) {
        Intent intent = new Intent(context, TrainingAlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, TRAINING_CODE, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if( alarmManager == null )
            return false;
        alarmManager.cancel(sender);
        return true;
    }
}

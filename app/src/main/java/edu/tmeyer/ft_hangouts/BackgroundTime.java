package edu.tmeyer.ft_hangouts;

import android.app.Activity;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class BackgroundTime {

    private        Timestamp timestamp;

    private static BackgroundTime instance;

    public static BackgroundTime getInstance() {
        if (instance == null) {
            instance = new BackgroundTime();
        }
        return instance;
    }

    public void onStopped(Activity activity) {
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public void onStart(Activity activity) {
        if (this.timestamp == null) return;

        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm", activity.getResources().getConfiguration().locale);
        String toPrint = activity.getResources().getString(R.string.background_time) + sdf1.format(this.timestamp);

        Snackbar.make(activity.findViewById(android.R.id.content), toPrint, Snackbar.LENGTH_SHORT).show();
    }
}

package edu.tmeyer.ft_hangouts.activity;

import android.app.Activity;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import edu.tmeyer.ft_hangouts.R;

public class BackgroundTime {

    private        Timestamp timestamp;

    private static BackgroundTime instance;

    public static BackgroundTime getInstance() {
        if (instance == null) {
            instance = new BackgroundTime();
        }
        return instance;
    }

    public void onPause(Activity activity) {
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public void onResume(Activity activity) {
        if (this.timestamp == null) return;

        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm", activity.getResources().getConfiguration().locale);
        String toPrint = activity.getResources().getString(R.string.background_time) + sdf1.format(this.timestamp);

        Snackbar.make(activity.getWindow().getDecorView().getRootView(), toPrint, Snackbar.LENGTH_SHORT).show();
    }
}

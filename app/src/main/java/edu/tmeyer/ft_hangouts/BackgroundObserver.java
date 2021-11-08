package edu.tmeyer.ft_hangouts;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BackgroundObserver extends Application implements Application.ActivityLifecycleCallbacks {

    private int         numberOfActivities = 0;
    private Activity    currentActivity;

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (this.numberOfActivities == 0) {
            BackgroundTime.getInstance().onStart(activity);
        }
        this.numberOfActivities++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        clearReferences(activity);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (this.numberOfActivities == 1) {
            BackgroundTime.getInstance().onStopped(activity);
        }
        this.numberOfActivities--;
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        clearReferences(activity);
    }

    private void clearReferences(Activity activity) {
        if (this.currentActivity.equals(activity)) {
            setCurrentActivity(null);
        }
    }
}

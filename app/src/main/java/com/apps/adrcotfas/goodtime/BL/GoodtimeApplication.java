package com.apps.adrcotfas.goodtime.BL;
import android.app.Application;
import android.support.v7.preference.PreferenceManager;

import java.util.concurrent.TimeUnit;
import static com.apps.adrcotfas.goodtime.BL.PreferencesManager.*;

/**
 * Maintains a global state of the app and stores the event bus ({@link EventBus})
 * and the {@link CurrentSession}
 */
public class GoodtimeApplication extends Application {

    private static volatile GoodtimeApplication INSTANCE;
    private static EventBus mBus;
    private static CurrentSessionManager mCurrentSessionManager;

    public static GoodtimeApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        mCurrentSessionManager = new CurrentSessionManager(new CurrentSession(TimeUnit.MINUTES.toMillis(
                Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(WORK_DURATION, "25")))));
        mBus = new EventBus();
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSessionManager.getCurrentSession();
    }

    public EventBus getBus() {
        return mBus;
    }

    public CurrentSessionManager getCurrentSessionManager() {
        return mCurrentSessionManager;
    }
}
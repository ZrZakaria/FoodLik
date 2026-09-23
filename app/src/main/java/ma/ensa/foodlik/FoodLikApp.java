package ma.ensa.foodlik;

import android.app.Application;

import ma.ensa.foodlik.util.NotificationHelper;

public class FoodLikApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannels(this);
    }
}

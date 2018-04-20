package tmap.iuh.personalexpenses;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class AppDatabaseOffline extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

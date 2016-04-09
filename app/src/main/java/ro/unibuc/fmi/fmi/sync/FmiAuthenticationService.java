package ro.unibuc.fmi.fmi.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by alexandru on 09.04.2016
 */
public class FmiAuthenticationService extends Service {
    private FmiAuthenticator fmiAuthenticator;
    @Override
    public void onCreate() {
        super.onCreate();
        fmiAuthenticator = new FmiAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return fmiAuthenticator.getIBinder();
    }
}

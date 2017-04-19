package callscreenoff.rejh.com.callscreenoff;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

public class CsoHsObserver extends Service {

    // ===================================================================
    // Objects and variables..

    private String APPTAG = "CallScreenOff";

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    private Context context;


    // ===================================================================
    // Lifecycle

    public CsoHsObserver() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Create
    @Override
    public void onCreate() {

        super.onCreate();

        Log.i(APPTAG, "CsoHsObserver.onCreate()");

        // Context
        context = (Context) this;

        // Settings
        sett = getSharedPreferences(APPTAG,2);
        settEditor = sett.edit();

        // Make sticky
        try {
            PackageManager packMgr = context.getPackageManager();
            ComponentName thisComponent = new ComponentName(context, CsoHsObserver.class);
            packMgr.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
        catch(Exception e) { Log.e(APPTAG," -> MakeSticky Exception: "+e); }

        // Register receiver..
        CsoHsStateRecv csoHsStateRecv = new CsoHsStateRecv();
        IntentFilter csoHsStateIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(csoHsStateRecv, csoHsStateIntentFilter);

    }

    // Destroy
    @Override
    public void onDestroy() {

        super.onDestroy();

        Log.i(APPTAG, "CsoHsObserver.onDestroy()");

        // Register receiver..
        CsoHsStateRecv csoHsStateRecv = new CsoHsStateRecv();
        IntentFilter csoHsStateIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.unregisterReceiver(csoHsStateRecv);

    }
}

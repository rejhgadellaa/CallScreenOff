package callscreenoff.rejh.com.callscreenoff;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.util.Log;

public class CsoOnBootRecv extends BroadcastReceiver {

    // ===================================================================
    // Objects and variables..

    private String APPTAG = "CallScreenOff";

    Context context;


    // ===================================================================
    // Lifecycle

    public CsoOnBootRecv() {
    }

    @Override
    public void onReceive(Context _context, Intent _intent) {

        Log.i(APPTAG, "CsoOnBootRecv.onReceive()");

        context = _context;

        // Update check..
        boolean isUpdate = false;
        boolean isUpdateAndRun = false;
        try {
            String dataString = _intent.getDataString();
            String action = _intent.getAction();
            if (dataString!=null && action.equals("android.intent.action.PACKAGE_REPLACED")) {
                Log.d(APPTAG," -> Datastring: "+ dataString);
                isUpdate = true;
                if (dataString.contains("callscreenoff.rejh.com.callscreenoff")){
                    Log.d(APPTAG," --> Is Update");
                    isUpdate = true;
                    isUpdateAndRun = true;
                }
            }
        } catch(Exception e) {
            Log.w(APPTAG," > Exception when trying to check if update");
            Log.w(APPTAG,e);
        }

        if (isUpdate && !isUpdateAndRun){
            Log.d(APPTAG, " -> Update but not callscreenoff, do nothing?");
            return;
        }

        DevicePolicyManager deviceManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(context, CsoAdminRecv.class);

        if (deviceManger.isAdminActive(compName)) {

            Log.d(APPTAG, " -> Enable BT/HS state receiver");

            startCsoHsObserver();

        } else {

            Log.d(APPTAG," -> No admin permission");

            stopCsoHsObserver();

        }

    }

    private void startCsoHsObserver() {
        Intent serviceIntent = new Intent(context, CsoHsObserver.class);
        context.startService(serviceIntent);
    }

    private void stopCsoHsObserver() {
        Intent serviceIntent = new Intent(context, CsoHsObserver.class);
        context.stopService(serviceIntent);
    }

    private void startCsoService() {
        Intent serviceIntent = new Intent(context, CsoService.class);
        context.startService(serviceIntent);
    }

}

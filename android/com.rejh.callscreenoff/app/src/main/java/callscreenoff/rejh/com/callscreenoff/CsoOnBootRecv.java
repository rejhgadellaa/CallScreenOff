package callscreenoff.rejh.com.callscreenoff;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
            Log.d(APPTAG, " -> Enable BT state receiver");
            registerBtReceiver(true);
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            boolean btConnected = am.isBluetoothA2dpOn();
            if (btConnected) {
                startCsoService();
            }
        } else {
            Log.d(APPTAG," -> No admin permission");
            registerBtReceiver(false);
        }

    }

    private void startCsoService() {
        Intent serviceIntent = new Intent(context, CsoService.class);
        context.startService(serviceIntent);
    }

    private void registerBtReceiver(boolean turnOn) {
        Log.d(APPTAG,"CsoOnBootRecv.registerBtReceiver(): "+ turnOn);
        int flag=(turnOn ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        ComponentName component=new ComponentName(context, CsoBtStateRecv.class);
        context.getPackageManager().setComponentEnabledSetting(component, flag, PackageManager.DONT_KILL_APP);
        int compEnabledState = context.getPackageManager().getComponentEnabledSetting(component);
        Log.d(APPTAG, " -> Comp_enabled_state: " + compEnabledState);
    }

}

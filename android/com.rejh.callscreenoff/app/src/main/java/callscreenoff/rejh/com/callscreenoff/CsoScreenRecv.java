package callscreenoff.rejh.com.callscreenoff;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CsoScreenRecv extends BroadcastReceiver {

    // ===================================================================
    // Objects and variables..

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    private String APPTAG = "CallScreenOff";


    // ===================================================================
    // Lifecycle

    public CsoScreenRecv() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(APPTAG, "CsoScreenRecv.onReceive()");

        // Settings
        sett = context.getSharedPreferences(APPTAG, 2);
        settEditor = sett.edit();

        // Get settings
        boolean btConnected = sett.getBoolean("btConnected",false);
        int lastState = sett.getInt("lastState", TelephonyManager.CALL_STATE_IDLE);
        String lastNumber = sett.getString("lastNumber",null);

        // btConnected AND in_call? TURN OFF THAT SCREEN DAMMIT
        if (!btConnected || lastState!=TelephonyManager.CALL_STATE_OFFHOOK) {
            Log.d(APPTAG," -> Not in call or bt not connected, do nothing");
            return;
        }

        // Keyguard (lockscreen)
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Activity.KEYGUARD_SERVICE);

        lock.disableKeyguard();
        lock.reenableKeyguard();

    }
}

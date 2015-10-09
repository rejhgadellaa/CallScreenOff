package callscreenoff.rejh.com.callscreenoff;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CsoService extends Service {

    // ===================================================================
    // Objects and variables..

    private TelephonyManager telephonyMgr;
    private PhoneStateListener phoneListener;

    private CsoScreenRecv csoScreenRecv;

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    private String APPTAG = "CallScreenOff";


    // ===================================================================
    // Lifecycle

    public CsoService() {
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

        Log.d(APPTAG, "CsoService.onCreate()");

        // Settings
        sett = getSharedPreferences(APPTAG,2);
        settEditor = sett.edit();

        // TelephonyManager ++ Listener
        telephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        setupTelephonyListener();

        // Prep receiver..
        csoScreenRecv = new CsoScreenRecv();

        // Toast it!
        Toast.makeText(CsoService.this, "CallScreenOff Service Active", Toast.LENGTH_SHORT).show();

    }

    // Destroy
    @Override
    public void onDestroy() {

        super.onDestroy();

        Log.d(APPTAG, "CsoService.onDestroy()");

    }

    // ===================================================================
    // Methods

    // --- SETUP

    // setupTelephonyListener
    private void setupTelephonyListener() {

        Log.d(APPTAG, "CsoService.setupTelephonyListener()");

        phoneListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        handlePhoneCall(state,incomingNumber);
                    case TelephonyManager.CALL_STATE_RINGING:
                        handlePhoneCall(state,incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle
                        break;
                }
            }
        };
        telephonyMgr.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    // --- HANDLE CALLS

    private void handlePhoneCall(int state, String incomingNumber) {

        Log.d(APPTAG, "CsoService.handlePhoneCall(): "+ state);

        // Headset connected?
        boolean btConnected = false;
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(am.isBluetoothA2dpOn()) {
            btConnected = true;
        }

        // Store..

        settEditor.putBoolean("btConnected", btConnected);
        settEditor.putInt("lastState", state);
        settEditor.putString("lastNumber", incomingNumber);
        settEditor.commit();

        // (Un)register screen listener..

        if (btConnected && state==TelephonyManager.CALL_STATE_OFFHOOK) {

            // Keyguard (lockscreen)
            KeyguardManager keyguardManager = (KeyguardManager) CsoService.this.getSystemService(Activity.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Activity.KEYGUARD_SERVICE);

            lock.disableKeyguard();
            lock.reenableKeyguard();

            // Set up receiver
            IntentFilter filterScreen = new IntentFilter(Intent.ACTION_SCREEN_ON);
            registerReceiver(csoScreenRecv, filterScreen);

        } else if (btConnected && state==TelephonyManager.CALL_STATE_IDLE) {

            // Keyguard (lockscreen)
            KeyguardManager keyguardManager = (KeyguardManager) CsoService.this.getSystemService(Activity.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(Activity.KEYGUARD_SERVICE);

            lock.disableKeyguard();
            lock.reenableKeyguard();

            // Unreg listener..
            try {
                unregisterReceiver(csoScreenRecv);
            } catch(IllegalArgumentException e) {
                Log.e(APPTAG," -> Error: "+ e);
                Log.e(APPTAG,e.getStackTrace().toString());
            }

        } else {
            // Unreg listener..
            try {
                unregisterReceiver(csoScreenRecv);
            } catch(IllegalArgumentException e) {
                Log.e(APPTAG," -> Error: "+ e);
                Log.e(APPTAG,e.getStackTrace().toString());
            }
        }

    }


}

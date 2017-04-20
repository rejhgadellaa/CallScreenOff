package callscreenoff.rejh.com.callscreenoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

public class CsoHsStateRecv extends BroadcastReceiver {

    // ===================================================================
    // Objects and variables..

    private String APPTAG = "CallScreenOff";

    Context context;

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    // ===================================================================
    // Lifecycle

    public CsoHsStateRecv() {
    }

    @Override
    public void onReceive(Context _context, Intent _intent) {

        Log.i(APPTAG, "CsoHsStateRecv.onReceive()");
        Log.d(APPTAG, " -> Action: " + _intent.getAction());

        context = _context;

        // Settings
        sett = context.getSharedPreferences(APPTAG,2);
        settEditor = sett.edit();

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //boolean hsConnected = am.isWiredHeadsetOn();
        int hsState = _intent.getIntExtra("state", 0);
        int hsMicrophone = _intent.getIntExtra("microphone ", 0);
        String hsName = _intent.getStringExtra("name");
        boolean hsConnected = hsState==1; //&& hsMicrophone==1;

        Log.d(APPTAG, " -> Headset name: "+ hsName);
        Log.d(APPTAG, " -> Headset state: " + hsState);
        Log.d(APPTAG, " -> Headset microphone: " + hsMicrophone);
        Log.d(APPTAG, " -> Headset connected: " + hsConnected);

        settEditor.putBoolean("onDestroyed", true);
        settEditor.commit();
        Intent serviceIntent = new Intent(context, CsoService.class);
        if (hsConnected) {
            context.startService(serviceIntent);
        } else {
            context.stopService(serviceIntent);
        }

    }
}

package callscreenoff.rejh.com.callscreenoff;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

public class CsoBtStateRecv extends BroadcastReceiver {

    // ===================================================================
    // Objects and variables..

    private String APPTAG = "CallScreenOff";

    Context context;

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    // ===================================================================
    // Lifecycle

    public CsoBtStateRecv() {
    }

    @Override
    public void onReceive(Context _context, Intent _intent) {

        Log.i(APPTAG, "CsoBtStateRecv.onReceive()");
        Log.d(APPTAG, " -> Action: " + _intent.getAction());

        context = _context;

        // Settings
        sett = context.getSharedPreferences(APPTAG,2);
        settEditor = sett.edit();

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        boolean btConnected = am.isBluetoothA2dpOn();

        Log.d(APPTAG, " -> BluetoothA2dpOn: " + btConnected);

        settEditor.putBoolean("onDestroyed", true);
        settEditor.commit();
        Intent serviceIntent = new Intent(context, CsoService.class);
        if (btConnected) {
            context.startService(serviceIntent);
        } else {
            context.stopService(serviceIntent);
        }
    }
}

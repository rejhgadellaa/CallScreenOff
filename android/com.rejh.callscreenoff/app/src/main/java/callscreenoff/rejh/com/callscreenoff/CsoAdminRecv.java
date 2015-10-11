package callscreenoff.rejh.com.callscreenoff;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class CsoAdminRecv extends DeviceAdminReceiver{

    // ===================================================================
    // Objects and variables..

    private String APPTAG = "CallScreenOff";

    Context context;

    // ===================================================================
    // Lifecycle

    void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceive(Context _context, Intent intent) {
        context = _context;
        if (intent.getAction() == ACTION_DEVICE_ADMIN_DISABLE_REQUESTED) {
            abortBroadcast();
        }
        super.onReceive(_context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "CallScreenOff: Device Manager enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context,CsoService.class);
        context.stopService(serviceIntent);
        return "CallScreenOff has been disabled";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "CallScreenOff: Device Manager disabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        showToast(context, "CsoAdminRecv onPasswordChanged");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        showToast(context, "CsoAdminRecv onPasswordFailed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        showToast(context, "CsoAdminRecv onPasswordSucceeded");
    }

    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        long expr = dpm.getPasswordExpiration(
                new ComponentName(context, CsoAdminRecv.class));
        long delta = expr - System.currentTimeMillis();
        boolean expired = delta < 0L;
        String message = expired ? "CsoAdminRecv onPasswordExpiring status_past" : "CsoAdminRecv onPasswordExpiring status_future";
        showToast(context, message);
        Log.v(APPTAG, message);
    }

}

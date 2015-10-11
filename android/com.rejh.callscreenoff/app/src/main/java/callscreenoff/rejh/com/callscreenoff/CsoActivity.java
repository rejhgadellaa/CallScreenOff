package callscreenoff.rejh.com.callscreenoff;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CsoActivity extends AppCompatActivity implements View.OnClickListener {

    private String APPTAG = "CallScreenOff";

    private Context context;

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    private DevicePolicyManager deviceManger;
    private ActivityManager activityManager;
    private ComponentName compName;

    static final int RESULT_ENABLE = 1;

    private Button button;

    private long newIntentTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cso);

        Log.i(APPTAG, "CsoActivity.onCreate()");

        // Context
        context = (Context) this;

        // Settings
        sett = context.getSharedPreferences(APPTAG, 2);
        settEditor = sett.edit();

        // Managers and stuff
        deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, CsoAdminRecv.class);

        // GUI
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);

    }

    @Override
    public void onResume() {

        super.onResume();

        Log.i(APPTAG, "CsoActivity.onResume()");

        // Service?
        if (deviceManger.isAdminActive(compName)) {
            Log.d(APPTAG," -> Start service");
            startCsoService();
        } else {
            Log.d(APPTAG," -> Can't start service without admin permission");
        }

        // CMD?
        Intent intent = getIntent();
        long newIntentTimeDiff = System.currentTimeMillis() - newIntentTime;
        if (newIntentTimeDiff<1000) {

            // CMD_LOCK_DEVICE
            if (intent != null && intent.hasExtra("cmd_lock_device") && intent.getBooleanExtra("cmd_lock_device", false)) {
                if (deviceManger.isAdminActive(compName)) {
                    Log.d(APPTAG, " -> Lock device NOW");
                    deviceManger.lockNow();
                } else {
                    Log.w(APPTAG, " -> Cannot lock device without device admin permission");
                }
            }

            // CMD_MOVE_TO_BACK
            if (intent != null && intent.hasExtra("cmd_finish") && intent.getBooleanExtra("cmd_finish", false)) {
                Log.d(APPTAG," -> Finish activity");
                finish();
            }



        }

    }

    @Override
    public void onNewIntent (Intent _intent) {

        super.onNewIntent(_intent);

        Log.d(APPTAG, "CsoActivity.onNewIntent()");

        newIntentTime = System.currentTimeMillis();
        setIntent(_intent);

    }

    @Override
    public void onClick(View v) {

        // Enable Device Admin
        if (v==button) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "CallScreenOff needs to be a Device Administrator so it can lock your phone.");
            startActivityForResult(intent, RESULT_ENABLE);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(APPTAG,"CsoActivity.onActivityResult()");
        Log.d(APPTAG," -> Resultcode: "+ resultCode);
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(APPTAG, " -> Admin enabled!");
                    settEditor.putBoolean("deviceAdminEnabled",true);
                    settEditor.commit();
                    startCsoService();
                } else {
                    Log.i(APPTAG, " -> Admin enable FAILED!");
                    settEditor.putBoolean("deviceAdminEnabled",false);
                    settEditor.commit();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startCsoService() {
        Intent serviceIntent = new Intent(CsoActivity.this, CsoService.class);
        startService(serviceIntent);
    }

}

package callscreenoff.rejh.com.callscreenoff;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CsoActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private String APPTAG = "CallScreenOff";

    private Context context;

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    private PowerManager powerMgr;
    private DevicePolicyManager deviceManger;
    private ActivityManager activityManager;
    private ComponentName compName;

    static final int RESULT_ENABLE = 1;

    private Button button_enable;
    private ImageView button_info;
    private ImageView button_feedback;
    private TextView textview_nothingtosee;

    private long newIntentTime = 0;

    private boolean dialogActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cso_intro);

        Log.i(APPTAG, "CsoActivity.onCreate()");

        // Context
        context = (Context) this;

        // Settings
        sett = context.getSharedPreferences(APPTAG, 2);
        settEditor = sett.edit();

        // Managers and stuff
        powerMgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, CsoAdminRecv.class);

        // GUI
        button_enable = (Button) findViewById(R.id.button_enable);
        button_enable.setOnClickListener(this);
        button_info = (ImageView) findViewById(R.id.button_info);
        button_info.setOnClickListener(this);
        button_info.setOnLongClickListener(this);
        button_feedback = (ImageView) findViewById(R.id.button_feedback);
        button_feedback.setOnClickListener(this);
        button_feedback.setOnLongClickListener(this);
        textview_nothingtosee = (TextView) findViewById(R.id.textview_nothingtosee);

        // Invoke onNewIntent
        Log.d(APPTAG," -> Invoke onNewIntent");
        onNewIntent(getIntent());

    }

    @Override
    public void onResume() {

        super.onResume();

        Log.i(APPTAG, "CsoActivity.onResume()");

        // Service?
        if (handleMarshMallowInactiveApp() && deviceManger.isAdminActive(compName)) {
            // Hide button_enable
            // button_enable.setVisibility(View.GONE);
            button_enable.setText("Disable");
            // Show nothingtosee
            textview_nothingtosee.setText("( Nothing to see here )");
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(1000);
            anim.setStartOffset(5000);
            anim.setRepeatCount(0);
            textview_nothingtosee.setVisibility(View.VISIBLE);
            textview_nothingtosee.startAnimation(anim);
            // Start service
            Log.d(APPTAG," -> Start service");
            startCsoService();
        } else {
            Log.d(APPTAG, " -> Can't start service without admin permission");
            button_enable.setText("Enable");
            // Show nothingtosee -> info
            textview_nothingtosee.setVisibility(View.VISIBLE);
            textview_nothingtosee.setText("CSO is currently disabled");
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
                    /*
                    if (powerMgr.isScreenOn()) {
                        WindowManager.LayoutParams params = getWindow().getAttributes();
                        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                        params.screenBrightness = 0;
                        getWindow().setAttributes(params);
                    }
                    /**/
                } else {
                    Log.w(APPTAG, " -> Cannot lock device without device admin permission");
                }
            }

            // CMD_MOVE_TO_BACK
            if (intent != null && intent.hasExtra("cmd_finish") && intent.getBooleanExtra("cmd_finish", false)) {
                Log.d(APPTAG, " -> Finish activity");
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
        if (v==button_enable) {
            if (!deviceManger.isAdminActive(compName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "CallScreenOff needs to be a Device Administrator so it can lock your phone.");
                startActivityForResult(intent, RESULT_ENABLE);
            } else {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.DeviceAdminAdd"));
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,compName);
                startActivity(intent);
            }
        }

        // Info
        if (v==button_info) {
            // For now just open a browser and point it to github..
            String weburl = "http://www.rejh.nl/callscreenoff/";
            Uri weburi = Uri.parse(weburl);
            Intent intent = new Intent(Intent.ACTION_VIEW, weburi);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        // Feedback
        if (v==button_feedback) {
            sendFeedback();
        }

    }

    @Override
    public boolean onLongClick(View v) {

        // Info
        if (v==button_info) {
            Toast.makeText(context, "Tap for info", Toast.LENGTH_SHORT).show();
            return true;
        }

        // Feedback
        if (v==button_feedback) {
            Toast.makeText(context, "Send feedback", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(APPTAG,"CsoActivity.onActivityResult()");
        Log.d(APPTAG, " -> Resultcode: " + resultCode);
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
        if (!isServiceRunning(CsoService.class)) {
            settEditor.putBoolean("onDestroyed", true);
            settEditor.commit();
            Intent serviceIntent = new Intent(CsoActivity.this, CsoService.class);
            startService(serviceIntent);
        }
    }

    private void sendFeedback() {

        String logstr = getlog();
        File file = new File(Environment.getExternalStorageDirectory().toString(), "callscreenoff-logcat.txt");
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(logstr.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"droidapps@rejh.nl"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"CallScreenOff Feedback");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
        startActivity(Intent.createChooser(emailIntent, "Send feedback..."));

    }

    private String getlog() {
        StringBuilder log=new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("logcat -v time -d ^(?!chromium)");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line+"\n");
            }
        } catch (IOException e) {
            Log.e(APPTAG,"CsoActivity: getlog.IOException: "+e.toString());
            e.printStackTrace();
        }
        return log.toString();
    }

    private boolean handleMarshMallowInactiveApp() {
        Log.d(APPTAG," -> handleMarshMallowInactiveApp()");
        Log.d(APPTAG," --> SDK: "+ Build.VERSION.SDK_INT);
        if(Build.VERSION.SDK_INT >= 23) {
            // Marshmallow detected
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            Log.d(APPTAG," --> Ignore batt optimizations: "+ pm.isIgnoringBatteryOptimizations(context.getPackageName()));
            if (!pm.isIgnoringBatteryOptimizations(context.getPackageName()) && !dialogActive) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("We've detected you're running Android 6.0 Marshmallow (or higher). Congrats! "
                        + "However, this means a new feature called Inactive Apps may get in the way of CallScreenOff's ability to operate properly. "
                        + "\n\nIf you tap OK you'll be taken to the appropriate settings screen where you can make an exception.")
                        .setTitle("Action required");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent inactiveSettIntent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        context.startActivity(inactiveSettIntent);
                        dialogActive = false;
                    }
                });
                builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                        dialogActive = false;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        }
        return true;
    }

    // --- Service running
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

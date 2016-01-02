package callscreenoff.rejh.com.callscreenoff;

import android.app.Activity;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import callscreenoff.rejh.com.callscreenoff.callscreenoff.rejh.com.callscreenoff.helpers.NotifBuilder;

public class CsoService extends Service
        implements SensorEventListener
{

    // ===================================================================
    // Objects and variables..

    private String APPTAG = "CallScreenOff";

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    private Context context;

    private TelephonyManager telephonyMgr;
    private PhoneStateListener phoneListener;

    private DevicePolicyManager deviceManger;

    private PowerManager powerMgr;
    private PowerManager.WakeLock wakeLock;

    private SensorManager sensorManager;
    private Sensor proxSensor;

    private NotifBuilder notifBuilder;
    private final static int NOTIF_FOREGROUND_ID = 1;

    private CsoUnlockRecv csoUnlockRecv;
    private IntentFilter csoUnlockRecvIntentFilter;

    private boolean btConnected;
    private boolean inCall = false;
    private boolean hasHungUp = false;

    private int lastPhoneState = -1;
    private float lastProxValue = -1.0f;

    private long hangupTimeInMillis = 0;

    private boolean skipHandleProxValueOnce = false;
    private boolean justRegisteredTelephonyListener = false;


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

        Log.i(APPTAG, "CsoService.onCreate()");

        // Context
        context = (Context) this;

        // Settings
        sett = getSharedPreferences(APPTAG,2);
        settEditor = sett.edit();

        // Make sticky
        try {
            PackageManager packMgr = context.getPackageManager();
            ComponentName thisComponent = new ComponentName(context, CsoService.class);
            packMgr.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
        catch(Exception e) { Log.e(APPTAG," -> MakeSticky Exception: "+e); }

        // Grrrr. android killed the service?
        if (!sett.getBoolean("onDestroyed",false)) {
            Log.w(APPTAG," -> Grrrr.. android killed the service :(");
            // TODO: do we need to act on this?
        }
        // Reset value
        settEditor.putBoolean("onDestroyed", false);
        settEditor.commit();

        // Just Started..
        skipHandleProxValueOnce = true;

        // Restore values..
        btConnected = sett.getBoolean("csoService_btConnected", false);
        lastPhoneState = sett.getInt("csoService_lastPhoneState", -1);
        lastProxValue = sett.getFloat("csoService_lastProxValue", -1.0f);
        hangupTimeInMillis = sett.getLong("csoService_hangupTimeInMillis",0);

        // TelephonyManager ++ Listener
        justRegisteredTelephonyListener = true;
        telephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        setupTelephonyListener();

        // Device Admin
        deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(this, CsoAdminRecv.class);
        if (!deviceManger.isAdminActive(compName)) {
            Log.w(APPTAG," -> No Device Admin permission, shutdown");
            stopSelf();
            return;
        }

        // Power Manager
        powerMgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        // Sensor..
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // notifBuilder
        notifBuilder = new NotifBuilder(context);

        // Prep receiver..
        csoUnlockRecv = new CsoUnlockRecv();
        csoUnlockRecvIntentFilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        csoUnlockRecvIntentFilter.addAction(Intent.ACTION_SCREEN_ON);

        // Set up receiver
        registerReceiver(csoUnlockRecv, csoUnlockRecvIntentFilter);

        // Toast it!
        Toast.makeText(CsoService.this, "CallScreenOff Service Active", Toast.LENGTH_SHORT).show();

        // Listen for values once to get things started..
        regProxListener();

        // Go foreground
        goForeground();

    }

    // Destroy
    @Override
    public void onDestroy() {

        super.onDestroy();

        Log.i(APPTAG, "CsoService.onDestroy()");

        // Store boolean so we know when we've been killed by Android :S
        settEditor.putBoolean("onDestroyed",true);
        settEditor.commit();

        // Unregister listeners..
        unregProxListener();
        telephonyMgr.listen(phoneListener, PhoneStateListener.LISTEN_NONE);

        leaveForeground();

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
                handlePhoneCall(state,incomingNumber);
            }
        };
        telephonyMgr.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    // --- SENSOR

    private boolean proxSensorActive = false;

    private void regProxListener() {
        Log.d(APPTAG, "CsoService.regProxListener()");
        if (proxSensorActive) {
            Log.d(APPTAG," -> ProxSensor active, unreg first..");
            unregProxListener();
        }
        proxSensorActive = true;
        handleProxValue(lastProxValue); // run once in case sensor listener already active
        sensorManager.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregProxListener() {
        Log.d(APPTAG, "CsoService.unregProxListener()");
        proxSensorActive = false;
        try {
            sensorManager.unregisterListener(this);
        } catch (Exception e) {
            Log.e(APPTAG, " --> Unreg sensor listener ERROR: " + e.toString());
            e.printStackTrace();
        }
    }

    // --- HANDLE CALLS

    private void handlePhoneCall(int state, String incomingNumber) {

        Log.d(APPTAG, "CsoService.handlePhoneCall(): "+ state);

        if (wakeLock!=null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        wakeLock = powerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,APPTAG);
        wakeLock.acquire(10*1000);

        // Headset connected?
        btConnected = false;
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(am.isBluetoothA2dpOn()) {
            btConnected = true;
        }
        //btConnected = true; // TODO: FOR TESTING

        if (btConnected && state==TelephonyManager.CALL_STATE_OFFHOOK) {

            Log.d(APPTAG, " -> BT && offhook, reg listener");

            goForeground();

            // Init prox sensor
            inCall = true;
            hasHungUp = false;
            regProxListener();


        } else if (btConnected && state==TelephonyManager.CALL_STATE_IDLE) {

            Log.d(APPTAG, " -> BT && idle, unreg listener");

            // TODO: deprecated
            //leaveForeground();

            // Reset some values, stop prox sensor
            if (inCall) { // if we were in a call than we hung up, right?
                hasHungUp = true;
            }
            inCall = false;
            unregProxListener();

            // This event just fired because the service registered the listener...
            /*
            // TODO: Not needed anymore (?)
            if (lastPhoneState<0) {
                Log.d(APPTAG," --> lastPhoneState: "+ lastPhoneState +", do nothing");
                lastPhoneState = state;
                return;
            }
            /**/
            // Same here..
            if (justRegisteredTelephonyListener) {
                Log.d(APPTAG," --> lastPhoneState: "+ lastPhoneState +", justRegisteredTelephonyListener, do nothing");
                justRegisteredTelephonyListener = false;
                lastPhoneState = state;
                return;
            }

            // Store time
            hangupTimeInMillis = System.currentTimeMillis();

            // Close activity
            Log.d(APPTAG," -> Send cmd_finish to activity from handlePhoneCall -> idle");
            Intent activityIntent = new Intent(context, CsoActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activityIntent.putExtra("cmd_finish", true);
            if (lastProxValue<5) {
                Log.d(APPTAG," --> Also cmd_lock_device");
                activityIntent.putExtra("cmd_lock_device", true);
            }
            context.startActivity(activityIntent);

        } else if (btConnected && state==TelephonyManager.CALL_STATE_RINGING) {

            Log.d(APPTAG, " -> BT && ringing, go foreground..");

            goForeground();
            inCall = false;
            hasHungUp = false;

        } else {

            Log.d(APPTAG," -> No BT, unreg listener");

            // Stop prox sensor
            inCall = false;
            hasHungUp = false;
            unregProxListener();
            // TODO: deprecated
            //leaveForeground();

        }

        lastPhoneState = state;

        storeValues();

    }

    // ===================================================================
    // Sensor

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing..
        if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
            Log.d(APPTAG," --> Accuracy: "+ accuracy);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            getProximity(event);
        }

    }

    private void getProximity(SensorEvent event) {

        // Values
        float[] values = event.values;
        float proxcm = values[0];

        handleProxValue(proxcm);

        storeValues();

    }

    private void handleProxValue(float proxcm) {

        Log.d(APPTAG,"CsoService.handleProxValue(): "+ proxcm);

        if (skipHandleProxValueOnce) {
            Log.w(APPTAG," --> skipHandleProxValueOnce==true, do nothing");
            skipHandleProxValueOnce = false;
            return;
        }
        skipHandleProxValueOnce = false;

        if (!proxSensorActive) {
            Log.w(APPTAG," --> Huh? !proxSensorActive");
            unregProxListener();
            return;
        }

        if (proxcm<0.0f) {
            Log.d(APPTAG," --> proxcm: "+ proxcm +", do nothing");
            return;
        }

        if (proxcm<5.0f) {

            // Check bt before taking action..
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            btConnected = am.isBluetoothA2dpOn();
            //btConnected = true;  // TODO: FOR TESTING
            if (btConnected) {

                // Bring other app to front because dialer app will keep unlocking screen if it's active :S
                // -- Who built that thing?!
                Log.d(APPTAG," -> Send cmd_lock_device to activity from handleProxValue");
                Intent activityIntent = new Intent(context, CsoActivity.class);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activityIntent.putExtra("cmd_lock_device", true);
                if (!inCall) {
                    Log.d(APPTAG," --> Also cmd_finish");
                    activityIntent.putExtra("cmd_finish", true);
                }
                context.startActivity(activityIntent);

            } else {

                // BT disconnected but keep proxListener on..
                Log.d(APPTAG, " --> btConnected == false?");

            }

        }

        // Prox listener..
        long hangupTimeMillisAgo = System.currentTimeMillis() - hangupTimeInMillis;
        Log.d(APPTAG, " -> HangupTimeMillisAgo: " + hangupTimeMillisAgo);
        if (!inCall && hangupTimeMillisAgo > 2500 || !btConnected) {
            Log.d(APPTAG, " --> && unregProxListener");
            unregProxListener();
        }

        // Store value..
        lastProxValue = proxcm;

    }


    // ===================================================================
    // Receiver

    public class CsoUnlockRecv extends BroadcastReceiver {

        @Override
        public void onReceive(Context _context, Intent _intent) {

            Log.d(APPTAG,"CsoService.onReceive() -> Unlock");

            String action = _intent.getAction();
            Log.d(APPTAG," -> Action: "+ action);

            long hangupTimeMillisAgo = System.currentTimeMillis() - hangupTimeInMillis;
            Log.d(APPTAG," -> inCall: "+ inCall +", hasHungUp: "+ hasHungUp +", btConnected: "+ btConnected);
            Log.d(APPTAG," -> HangupTimeMillisAgo: " + hangupTimeMillisAgo);
            if (!inCall && !hasHungUp && hangupTimeMillisAgo > 5000 || !btConnected) {
                Log.d(APPTAG," -> Not in call, do nothing..");
                unregProxListener();
                return;
            }

            // Handle hasHungUp
            if (!inCall && hasHungUp) {
                Log.d(APPTAG," -> hasHungUp = true, set false");
                hasHungUp = false;
            }

            // Reg listener
            regProxListener();

        }

    }


    // ===================================================================
    // Notifications / foreground service

    private void goForeground() {

        Log.d(APPTAG,"CsoService.goForeground()");

        try {

            JSONObject opts = new JSONObject();
            opts.put("title","CallScreenOff");
            opts.put("message","The service is active");
            opts.put("smallicon","ic_stat_notification_phone_locked");
            opts.put("color","#009688");
            opts.put("priority","MIN");
            opts.put("ongoing",true);
            opts.put("alertOnce",true);

            JSONObject intentopt = new JSONObject();
            intentopt.put("type","activity");
            intentopt.put("package","callscreenoff.rejh.com.callscreenoff");
            intentopt.put("classname","callscreenoff.rejh.com.callscreenoff.CsoActivity");
            opts.put("intent", intentopt);

            Notification foregroundNotifObj = notifBuilder.build(NOTIF_FOREGROUND_ID, opts);

            startForeground(NOTIF_FOREGROUND_ID,foregroundNotifObj);

        } catch(JSONException e) {
            Log.e(APPTAG,"CsoService.goForeground().JSONException: "+e,e);
        }

    }

    private void leaveForeground() {

        Log.d(APPTAG,"CsoService.leaveForeground()");

        try {

            stopForeground(true);

        } catch(Exception e) {
            Log.w(APPTAG, "CsoService.leaveForeground().Exception: "+e,e);
        }

    }


    // ===================================================================
    // Helpers

    public void storeValues() {
        Log.d(APPTAG,"CsoService.storeValues()");
        settEditor.putBoolean("csoService_btConnected",btConnected);
        settEditor.putInt("csoService_lastPhoneState", lastPhoneState);
        settEditor.putFloat("csoService_lastProxValue", lastProxValue);
        settEditor.putLong("csoService_hangupTimeInMillis",hangupTimeInMillis);
        settEditor.commit();
    }


}

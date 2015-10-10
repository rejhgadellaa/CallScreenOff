package callscreenoff.rejh.com.callscreenoff;

import android.app.Activity;
import android.app.IntentService;
import android.app.KeyguardManager;
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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CsoService extends Service
        implements SensorEventListener
{

    // ===================================================================
    // Objects and variables..

    private Context context;

    private TelephonyManager telephonyMgr;
    private PhoneStateListener phoneListener;

    private DevicePolicyManager deviceManger;

    private SensorManager sensorManager;
    private Sensor proxSensor;

    private CsoUnlockRecv csoUnlockRecv;
    private IntentFilter csoUnlockRecvIntentFilter;

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    private String APPTAG = "CallScreenOff";

    private boolean btConnected;
    private boolean inCall = false;
    private int nrOfSensorSamples = 0;


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

        // TelephonyManager ++ Listener
        telephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        setupTelephonyListener();

        // Device Admin
        deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        // Sensor..
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Prep receiver..
        csoUnlockRecv = new CsoUnlockRecv();
        csoUnlockRecvIntentFilter = new IntentFilter(Intent.ACTION_USER_PRESENT);

        // Set up receiver
        registerReceiver(csoUnlockRecv, csoUnlockRecvIntentFilter);

        // Toast it!
        Toast.makeText(CsoService.this, "CallScreenOff Service Active", Toast.LENGTH_SHORT).show();

    }

    // Destroy
    @Override
    public void onDestroy() {

        super.onDestroy();

        Log.i(APPTAG, "CsoService.onDestroy()");

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

    private void regProxListener() {
        Log.d(APPTAG,"CsoService.regProxListener()");
        nrOfSensorSamples = 0;
        sensorManager.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregProxListener() {
        Log.d(APPTAG,"CsoService.unregProxListener()");
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

        // Headset connected?
        btConnected = false;
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(am.isBluetoothA2dpOn()) {
            btConnected = true;
        }
        // btConnected = true; // TODO: FOR TESTING

        if (btConnected && state==TelephonyManager.CALL_STATE_OFFHOOK) {

            Log.d(APPTAG," -> BT && ofhook, reg listener");

            // Init prox sensor
            inCall = true;
            regProxListener();


        } else if (btConnected && state==TelephonyManager.CALL_STATE_IDLE) {

            Log.d(APPTAG," -> BT && idle, unreg listener");

            // Stop prox sensor
            inCall = false;
            unregProxListener();

        } else {

            Log.d(APPTAG," -> No BT || ringing, unreg listener");

            // Stop prox sensor
            inCall = false;
            unregProxListener();

        }

        // Store..
        settEditor.putBoolean("btConnected", btConnected);
        settEditor.putInt("lastState", state);
        settEditor.putString("lastNumber", incomingNumber);
        settEditor.commit();

    }

    // ===================================================================
    // Sensor

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing..
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

        Log.d(APPTAG," --> Proximity: "+values[0]);

        nrOfSensorSamples++;

        if (proxcm<5) {
            deviceManger.lockNow();
        }

        if (nrOfSensorSamples>1 || proxcm<5) {

        }

    }


    // ===================================================================
    // Receiver

    public class CsoUnlockRecv extends BroadcastReceiver {

        @Override
        public void onReceive(Context _context, Intent _intent) {

            Log.d(APPTAG,"CsoService.onReceive() -> Unlock");

            if (!inCall || !btConnected) {
                Log.d(APPTAG," -> Not in call, do nothing..");
                unregProxListener();
                return;
            }

            // Reg listener
            regProxListener();

        }

    }


}

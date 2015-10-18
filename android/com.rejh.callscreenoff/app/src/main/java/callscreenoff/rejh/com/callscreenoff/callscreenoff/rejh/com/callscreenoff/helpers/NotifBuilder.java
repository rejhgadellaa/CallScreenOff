package callscreenoff.rejh.com.callscreenoff.callscreenoff.rejh.com.callscreenoff.helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.StrictMode;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by P Daddy on 2015-10-18.
 */
public class NotifBuilder {

    // ===================================================================
    // Objects and variables..

    private String APPTAG = "CallScreenOff";

    private SharedPreferences sett;
    private SharedPreferences.Editor settEditor;

    private Context context;
    private String packageName;

    // Defaults..
    private String defTitle = "CallScreenOff";
    private String defMessage = "Ongoing notification";
    private String defSmallIcon = null;
    private String defColor = null;

    // ===================================================================
    // Constructor

    public NotifBuilder(Context _context) {

        Log.i(APPTAG, "NotifBuilder.Constructor()");

        context = _context;
        packageName = context.getPackageName();

    }

    // ===================================================================
    // Build

    public Notification build(int id, JSONObject opts) {

        Log.i(APPTAG, "NotifBuilder.build(): " + id);

        try {

            // ---> PREPARE

            // Variables..
            String title = opts.has("title") ? opts.getString("title") : defTitle;
            String message = opts.has("message") ? opts.getString("message") : defMessage;
            String smallicon = opts.has("smallicon") ? opts.getString("smallicon") : defSmallIcon;
            String color = opts.has("color") ? opts.getString("color") : defColor;
            String largeicon = opts.has("largeicon") ? opts.getString("largeicon") : null;
            String ticker = opts.has("ticker") ? opts.getString("ticker") : title;
            int priority = opts.has("priority") ? getPriority(opts.getString("priority")) : NotificationCompat.PRIORITY_DEFAULT;
            boolean autoCancel = opts.has("autoCancel") ? opts.getBoolean("autoCancel") : false;
            boolean ongoing = opts.has("ongoing") ? opts.getBoolean("ongoing") : false;
            boolean alertOnce = opts.has("alertOnce") ? opts.getBoolean("alertOnce") : false;

            // Variables -> Intent, extras and actions
            JSONObject intentopts = opts.has("intent") ? opts.getJSONObject("intent") : null;
            if (intentopts==null) {
                // Defaults...?
                Log.w(APPTAG," -> Missing intent");
                return null;
            }
            JSONArray intentExtras = intentopts.has("extras") ? intentopts.getJSONArray("extras") : null;

            // Actions
            JSONArray actions = opts.has("actions") ? opts.getJSONArray("actions") : null;

            // ---> START BUILDING

            Notification notifObj;
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            // Title, message
            builder.setContentTitle(title);
            builder.setContentText(message);

            // Icon // TODO: parse string to resInt
            if (smallicon!=null) { builder.setSmallIcon(getSmallIcon(smallicon)); }

            // Intent // TODO: a lot.
            PendingIntent notifPendingIntent = createPendingIntent(intentopts, intentExtras, id);
            builder.setContentIntent(notifPendingIntent);

            // > Optionals

            // Color (5.0)
            builder.setColor(Color.parseColor(color));

            // Large icon
            if (largeicon!=null) {
                Bitmap largeIconBmp = getIcon(largeicon);
                if (largeIconBmp!=null) {
                    int scaleToSize = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height);
                    Bitmap scaledLargeIconBmp = Bitmap.createScaledBitmap(largeIconBmp, scaleToSize, scaleToSize, true);
                    builder.setLargeIcon(getCircleBitmap(scaledLargeIconBmp));
                    largeIconBmp.recycle();
                }
            }

            // Ticker
            builder.setTicker(ticker);

            // Prio
            builder.setPriority(priority);

            // Autocancel, ongoing, alertOnce
            builder.setAutoCancel(autoCancel);
            builder.setOngoing(ongoing);
            builder.setOnlyAlertOnce(alertOnce);

            // Actions
            if (actions!=null) {
                for (int i=0; i<actions.length(); i++) {
                    // Prep
                    JSONObject action = actions.getJSONObject(i);
                    int actionIcon = getSmallIcon(action.getString("icon"));
                    CharSequence actionTitle = (CharSequence) action.getString("title");
                    Log.d(APPTAG," >> "+ actionTitle);
                    JSONObject actionIntent = action.getJSONObject("intent");
                    JSONArray actionIntentExtras = actionIntent.has("extras") ? actionIntent.getJSONArray("extras") : null;
                    PendingIntent actionPendingIntent = createPendingIntent(actionIntent,actionIntentExtras,i+1+(100*id));
                    // Build..
                    NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(actionIcon, actionTitle, actionPendingIntent);
                    NotificationCompat.Action builtAction = actionBuilder.build();
                    // Add
                    builder.addAction(builtAction);
                }
            }

            // Kablooie
            notifObj = builder.build();

            // Done!
            return notifObj;

        } catch(JSONException e) {
            Log.e(APPTAG,"NotifBuilder.build().JSONException: "+e,e);
            return null;
        } catch(Exception e) {
            Log.e(APPTAG,"NotifBuilder.build().Exception: "+e,e);
            return null;
        }

    }

    // ===================================================================
    // Cancel

    public void cancel(int id) {

        // TODO: Nothing?

    }

    // ===================================================================
    // Helpers

    // > Intents
    // Create Pending Intent (with extras!)
    private PendingIntent createPendingIntent(JSONObject intentCfg, JSONArray intentExtras, int requestCode) throws JSONException {

        // New intent
        Intent notifIntent = new Intent();

        // > Figure what type of intent

        // By classname
        if (intentCfg.has("package") && intentCfg.has("classname")) {
            String intentPackage = intentCfg.getString("package");
            String intentClassName = intentCfg.getString("classname");
            Log.d(APPTAG," -> Intent by classname: "+intentClassName);
            notifIntent.setClassName(intentPackage, intentClassName);
        }

        // By action
        if (intentCfg.has("package") && intentCfg.has("action")) {
            String intentPackage = intentCfg.getString("package");
            String action = intentCfg.getString("action");
            Log.d(APPTAG," -> Intent by action: "+action);
            notifIntent.setPackage(intentPackage);
            notifIntent.setAction(action);
            Log.d(APPTAG," ---> "+notifIntent.getAction());
        }


        // Extras
        if (intentExtras!=null) {
            for (int i=0; i<intentExtras.length(); i++) {
                JSONObject intentExtra = intentExtras.getJSONObject(i);
                String type = intentExtra.getString("type").toLowerCase();
                String name = intentExtra.getString("name");
                if (type.equals("string")) {
                    Log.d(APPTAG," >> Extra: "+ type +", "+ name +", "+ intentExtra.getString("value"));
                    notifIntent.putExtra(name, intentExtra.getString("value"));
                } else if (type.equals("int")) {
                    notifIntent.putExtra(name, intentExtra.getInt("value"));
                } else if (type.equals("float") || type.equals("double")) {
                    notifIntent.putExtra(name, intentExtra.getDouble("value"));
                } else if (type.equals("boolean") || type.equals("bool")) {
                    notifIntent.putExtra(name, intentExtra.getBoolean("value"));
                }
            }
        }

        // Figure type
        String intentType = intentCfg.has("type") ? intentCfg.getString("type") : "activity";

        PendingIntent notifPendingIntent = null;
        if (intentType.equals("activity")) {
            notifPendingIntent = PendingIntent.getActivity(context, requestCode, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT); // TODO: options!
        } else if (intentType.equals("receiver")) {
            notifPendingIntent = PendingIntent.getBroadcast(context, requestCode, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT); // TODO: options!
        } else if (intentType.equals("service")) {
            notifPendingIntent = PendingIntent.getService(context, requestCode, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return notifPendingIntent;

    }

    // > Priority

    private int getPriority(String priority) {

        int res = NotificationCompat.PRIORITY_DEFAULT; // DEFAULT

        if (priority.equals("MAX")) {
            res = NotificationCompat.PRIORITY_MAX;
        } else if (priority.equals("HIGH")) {
            res = NotificationCompat.PRIORITY_HIGH;
        } else if (priority.equals("DEFAULT")) {
            res = NotificationCompat.PRIORITY_DEFAULT;
        } else if (priority.equals("LOW")) {
            res = NotificationCompat.PRIORITY_LOW;
        } else if (priority.equals("MIN")) {
            res = NotificationCompat.PRIORITY_MIN;
        }

        return res;

    }

    // > Icons
    // Ripped from LocalNotification plugin: https://github.com/katzer/cordova-plugin-local-notifications/blob/master/src/android/Options.java

    private Bitmap getIcon (String icon) {
        Bitmap bmp = null;

        if (icon.startsWith("http")) {
            bmp = getIconFromURL(icon);
        } else if (icon.startsWith("file://")) {
            bmp = getIconFromURI(icon);
        }

        if (bmp == null) {
            bmp = getIconFromRes(icon);
        }

        return bmp;
    }

    private int getSmallIcon (String iconName) {
        int resId       = 0;

        resId = getIconValue(packageName, iconName);

        if (resId == 0) {
            resId = getIconValue("android", iconName);
        }

        if (resId == 0) {
            resId = getIconValue(packageName, "icon");
        }

        return resId;
    }

    private int getIconValue (String className, String iconName) {
        int icon = 0;

        try {
            Class<?> klass  = Class.forName(className + ".R$drawable");

            icon = (Integer) klass.getDeclaredField(iconName).get(Integer.class);
        } catch (Exception e) {}

        return icon;
    }

    private Bitmap getIconFromURL (String src) {
        //Log.d(APPTAG,"getIconFromURL");
        Bitmap bmp = null;
        StrictMode.ThreadPolicy origMode = StrictMode.getThreadPolicy();

        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();

            bmp = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        StrictMode.setThreadPolicy(origMode);

        return bmp;
    }

    private Bitmap getIconFromRes (String icon) {
        //Log.d(APPTAG,"getIconFromRes");
        Resources res = context.getResources();
        int iconId = 0;

        iconId = getIconValue(packageName, icon);

        if (iconId == 0) {
            iconId = getIconValue("android", icon);
        }

        if (iconId == 0) { // fallback
            iconId = getIconValue(packageName,"web_hi_res_512_002");
        }

        if (iconId == 0) { // ultimate fallback :(
            iconId = android.R.drawable.ic_menu_info_details;
        }

        Bitmap bmp = BitmapFactory.decodeResource(res, iconId);

        return bmp;
    }

    private Bitmap getIconFromURI (String src) {
        //Log.d(APPTAG,"getIconFromURI");
        Bitmap bmp = null;

        try {
            String path = src.replace("file://", "");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bmp = BitmapFactory.decodeFile(path,options);
        } catch (Exception e) {
            Log.e(APPTAG," -> Exception: "+ src +", "+ e);
            e.printStackTrace();
        }

        return bmp;
    }

    // > Get Circle Bitmap
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Log.d(APPTAG," -> Create CircleBitmap "+ bitmap.getWidth() +", "+ bitmap.getHeight());
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }



}
